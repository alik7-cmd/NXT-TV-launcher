package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.model.LauncherAppsResult
import com.techascent.tv_launcher.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform

class ObserveLauncherApps(
    private val installedApps: InstalledAppsRepository,
) {
    operator fun invoke(): Flow<LauncherAppsResult> = flow {
        var previous: LauncherAppsResult.Success? = null
        emit(LauncherAppsResult.Init)
        installedApps.observeApps()
            .transform { update ->
                when (update) {
                    InstalledAppsUpdate.Loading -> emit(previous ?: LauncherAppsResult.Init)
                    is InstalledAppsUpdate.Loaded -> {
                        val result = LauncherAppsResult.Success(update.apps)
                        previous = result
                        emit(result)
                    }
                }
            }
            .catch { emit(LauncherAppsResult.Error(it, previous)) }
            .collect { emit(it) }
    }
}
