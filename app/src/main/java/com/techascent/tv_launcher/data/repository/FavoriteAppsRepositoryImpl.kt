package com.techascent.tv_launcher.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.techascent.tv_launcher.domain.repository.FavoriteAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class FavoriteAppsRepositoryImpl(
    context: Context,
) : FavoriteAppsRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun observeFavorites(): Flow<Set<String>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_FAVORITES) trySend(readFavorites())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(readFavorites())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(Dispatchers.IO)

    override suspend fun toggleFavorite(packageName: String) = withContext(Dispatchers.IO) {
        val favorites = readFavorites()
        val updated = if (packageName in favorites) favorites - packageName else favorites + packageName
        preferences.edit { putStringSet(KEY_FAVORITES, updated) }
    }

    // Copy, because the returned set must not be modified.
    private fun readFavorites(): Set<String> =
        preferences.getStringSet(KEY_FAVORITES, emptySet()).orEmpty().toSet()

    private companion object {
        const val PREFERENCES_NAME = "launcher_preferences"
        const val KEY_FAVORITES = "favorite_packages"
    }
}
