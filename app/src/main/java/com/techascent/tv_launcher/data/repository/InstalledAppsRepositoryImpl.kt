package com.techascent.tv_launcher.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.ContextCompat
import com.techascent.tv_launcher.data.model.InstalledAppDto
import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.model.InstalledApp
import com.techascent.tv_launcher.domain.repository.InstalledAppsRepository
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform

class InstalledAppsRepositoryImpl(
    private val context: Context,
) : InstalledAppsRepository {
    private val packageManager: PackageManager = context.packageManager

    override fun observeApps(): Flow<InstalledAppsUpdate> = flow {
        val cache = InstalledAppCache()
        var configuration = Configuration(context.resources.configuration)
        emitAll(
            packageChanges().transform { changedPackage ->
                val current = Configuration(context.resources.configuration)
                if (current != configuration) {
                    cache.clear()
                    configuration = current
                }
                changedPackage?.let(cache::invalidate)
                emit(InstalledAppsUpdate.Loading)
                emit(InstalledAppsUpdate.Loaded(loadApps(cache)))
            },
        )
    }.flowOn(Dispatchers.IO)

    // Do not conflate package names: every changed package must invalidate its cached images.
    private fun packageChanges(): Flow<String?> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(intent.data?.schemeSpecificPart)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        trySend(null)
        awaitClose { context.unregisterReceiver(receiver) }
    }.buffer(Channel.UNLIMITED)

    private fun loadApps(cache: InstalledAppCache): List<InstalledApp> {
        val activities = queryLauncherActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),
        )
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .filterNot { it.activityInfo.packageName in HIDDEN_PACKAGES }
            .distinctBy { it.activityInfo.packageName }
            .toList()
        cache.retain(activities.map { it.activityInfo.packageName to it.activityInfo.name }.toSet())
        return activities
            .map { info ->
                cache.getOrLoad(info.activityInfo.packageName, info.activityInfo.name) {
                    InstalledAppDto(
                        packageName = info.activityInfo.packageName,
                        activityName = info.activityInfo.name,
                        label = info.loadLabel(packageManager).toString(),
                        icon = info.loadIcon(packageManager),
                        banner = info.activityInfo.loadBanner(packageManager)
                            ?: info.activityInfo.applicationInfo.loadBanner(packageManager),
                        // Pre-installed system apps cannot be uninstalled by the user.
                        isRemovable = info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0,
                    ).toDomain()
                }
            }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun queryLauncherActivities(intent: Intent): List<ResolveInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

    private companion object {
        /** Setup and developer utilities that clutter a living-room launcher. */
        val HIDDEN_PACKAGES = setOf(
            "com.android.traceur",
            "com.android.provision",
            "com.google.android.setupwizard",
            "com.google.android.tungsten.setupwraith",
        )
    }
}
