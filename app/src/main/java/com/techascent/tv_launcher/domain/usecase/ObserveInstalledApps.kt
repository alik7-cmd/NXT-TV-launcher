package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.flow.Flow

class ObserveInstalledApps(
    private val repository: InstalledAppsRepository,
) {
    operator fun invoke(): Flow<InstalledAppsUpdate> = repository.observeApps()
}
