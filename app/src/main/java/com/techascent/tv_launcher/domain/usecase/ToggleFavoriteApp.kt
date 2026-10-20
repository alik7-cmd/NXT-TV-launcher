package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.repository.FavoriteAppsRepository

class ToggleFavoriteApp(
    private val repository: FavoriteAppsRepository,
) {
    suspend operator fun invoke(packageName: String) = repository.toggleFavorite(packageName)
}
