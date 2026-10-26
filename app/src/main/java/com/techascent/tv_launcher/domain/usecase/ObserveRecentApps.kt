package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.repository.RecentAppsRepository

class ObserveRecentApps(
    private val repository: RecentAppsRepository,
) {
    operator fun invoke() = repository.observeRecentApps()
}
