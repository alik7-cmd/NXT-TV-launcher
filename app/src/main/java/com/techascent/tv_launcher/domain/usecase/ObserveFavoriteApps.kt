package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.repository.FavoriteAppsRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteApps(
    private val repository: FavoriteAppsRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavorites()
}
