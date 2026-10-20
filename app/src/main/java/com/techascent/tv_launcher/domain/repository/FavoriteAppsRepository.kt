package com.techascent.tv_launcher.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteAppsRepository {
    fun observeFavorites(): Flow<Set<String>>
    suspend fun toggleFavorite(packageName: String)
}
