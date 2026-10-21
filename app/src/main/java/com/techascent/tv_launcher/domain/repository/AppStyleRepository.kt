package com.techascent.tv_launcher.domain.repository

import com.techascent.tv_launcher.domain.model.AppStyle
import com.techascent.tv_launcher.domain.model.LauncherWallpaper
import kotlinx.coroutines.flow.Flow

interface AppStyleRepository {
    fun observeStyle(): Flow<AppStyle>
    suspend fun setStyle(style: AppStyle)
    fun observeShowAppNames(): Flow<Boolean>
    suspend fun setShowAppNames(show: Boolean)
    fun observeWallpaper(): Flow<LauncherWallpaper>
    suspend fun setWallpaper(wallpaper: LauncherWallpaper)
}
