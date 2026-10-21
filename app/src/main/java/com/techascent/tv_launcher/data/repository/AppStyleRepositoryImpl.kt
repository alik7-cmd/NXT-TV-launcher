package com.techascent.tv_launcher.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.techascent.tv_launcher.domain.model.AppStyle
import com.techascent.tv_launcher.domain.model.LauncherWallpaper
import com.techascent.tv_launcher.domain.repository.AppStyleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class AppStyleRepositoryImpl(
    context: Context,
) : AppStyleRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun observeStyle(): Flow<AppStyle> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_STYLE) trySend(readStyle())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(readStyle())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(Dispatchers.IO)

    override suspend fun setStyle(style: AppStyle) = withContext(Dispatchers.IO) {
        preferences.edit { putString(KEY_STYLE, style.name) }
    }

    override fun observeShowAppNames(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_SHOW_NAMES) trySend(preferences.getBoolean(KEY_SHOW_NAMES, false))
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(preferences.getBoolean(KEY_SHOW_NAMES, false))
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(Dispatchers.IO)

    override suspend fun setShowAppNames(show: Boolean) = withContext(Dispatchers.IO) {
        preferences.edit { putBoolean(KEY_SHOW_NAMES, show) }
    }

    private fun readStyle(): AppStyle =
        AppStyle.entries.firstOrNull { it.name == preferences.getString(KEY_STYLE, null) } ?: AppStyle.Banner

    override fun observeWallpaper(): Flow<LauncherWallpaper> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_WALLPAPER) trySend(readWallpaper())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(readWallpaper())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(Dispatchers.IO)

    override suspend fun setWallpaper(wallpaper: LauncherWallpaper) = withContext(Dispatchers.IO) {
        preferences.edit { putString(KEY_WALLPAPER, wallpaper.name) }
    }

    private fun readWallpaper(): LauncherWallpaper =
        LauncherWallpaper.entries.firstOrNull { it.name == preferences.getString(KEY_WALLPAPER, null) }
            ?: LauncherWallpaper.Three

    private companion object {
        const val PREFERENCES_NAME = "launcher_preferences"
        const val KEY_STYLE = "app_style"
        const val KEY_SHOW_NAMES = "show_app_names"
        const val KEY_WALLPAPER = "wallpaper"
    }
}
