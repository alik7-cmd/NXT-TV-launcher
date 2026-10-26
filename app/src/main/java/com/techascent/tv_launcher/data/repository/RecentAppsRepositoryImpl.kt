package com.techascent.tv_launcher.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.techascent.tv_launcher.domain.repository.RecentAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/** Stores recently launched packages as an ordered, comma-separated list. */
class RecentAppsRepositoryImpl(
    context: Context,
) : RecentAppsRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun observeRecentApps(): Flow<List<String>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_RECENTS) trySend(readRecents())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(readRecents())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(Dispatchers.IO)

    override suspend fun recordLaunch(packageName: String) = withContext(Dispatchers.IO) {
        val updated = (listOf(packageName) + (readRecents() - packageName)).take(MAX_STORED)
        preferences.edit { putString(KEY_RECENTS, updated.joinToString(SEPARATOR)) }
    }

    private fun readRecents(): List<String> =
        preferences.getString(KEY_RECENTS, null).orEmpty().split(SEPARATOR).filter { it.isNotEmpty() }

    private companion object {
        const val PREFERENCES_NAME = "launcher_preferences"
        const val KEY_RECENTS = "recent_packages"
        const val SEPARATOR = ","
        // More than shown, so uninstalled apps don't leave the row short.
        const val MAX_STORED = 10
    }
}
