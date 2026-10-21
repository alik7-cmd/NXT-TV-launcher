package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.model.LauncherAppsResult
import com.techascent.tv_launcher.domain.repository.FavoriteAppsRepository
import com.techascent.tv_launcher.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ObserveLauncherApps(
    private val installedApps: InstalledAppsRepository,
    private val favoriteApps: FavoriteAppsRepository,
) {
    operator fun invoke(): Flow<LauncherAppsResult> = flow {
        var previous: LauncherAppsResult.Success? = null
        val installed = installedApps.observeApps()
            .map { update ->
                when (update) {
                    InstalledAppsUpdate.Loading -> LauncherAppsResult.Init
                    is InstalledAppsUpdate.Loaded -> LauncherAppsResult.Success(update.apps, emptySet())
                }
            }
            .catch { emit(LauncherAppsResult.Error(it)) }

        emit(LauncherAppsResult.Init)
        combine(installed, favoriteApps.observeFavorites()) { result, favorites ->
            when (result) {
                LauncherAppsResult.Init -> previous?.copy(favoritePackages = favorites)
                    ?: LauncherAppsResult.Init
                is LauncherAppsResult.Success -> result.copy(favoritePackages = favorites)
                is LauncherAppsResult.Error -> result.copy(
                    previous = previous?.copy(favoritePackages = favorites),
                )
            }.also { updated ->
                if (updated is LauncherAppsResult.Success) previous = updated
            }
        }
            .catch { emit(LauncherAppsResult.Error(it, previous)) }
            .collect { emit(it) }
    }
}
