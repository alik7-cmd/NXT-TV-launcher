package com.techascent.tv_launcher.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.techascent.tv_launcher.domain.repository.RecentAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Stores recently launched packages as an ordered, comma-separated list. */
class RecentAppsRepositoryImpl internal constructor(
    private val preferences: SharedPreferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RecentAppsRepository {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
    )
    private val mutex = Mutex()

    override fun observeRecentApps(): Flow<List<String>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_RECENTS) trySend(readRecents())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(readRecents())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(dispatcher)

    override suspend fun recordLaunch(packageName: String) = withContext(dispatcher) {
        mutex.withLock {
            // Keep each launched package, not just the recent row, for Recently used sorting.
            val updated = listOf(packageName) + (readRecents() - packageName)
            preferences.edit { putString(KEY_RECENTS, updated.joinToString(SEPARATOR)) }
        }
    }

    private fun readRecents(): List<String> =
        preferences.getString(KEY_RECENTS, null).orEmpty().split(SEPARATOR).filter { it.isNotEmpty() }.distinct()

    private companion object {
        const val PREFERENCES_NAME = "launcher_preferences"
        const val KEY_RECENTS = "recent_packages"
        const val SEPARATOR = ","
    }
}
