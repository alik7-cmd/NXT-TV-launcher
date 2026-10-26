package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.repository.RecentAppsRepository

class RecordAppLaunch(
    private val repository: RecentAppsRepository,
) {
    suspend operator fun invoke(packageName: String) = repository.recordLaunch(packageName)
}
