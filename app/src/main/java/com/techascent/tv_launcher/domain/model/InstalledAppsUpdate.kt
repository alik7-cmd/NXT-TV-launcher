package com.techascent.tv_launcher.domain.model

sealed interface InstalledAppsUpdate {
    data object Loading : InstalledAppsUpdate
    data class Loaded(val apps: List<InstalledApp>) : InstalledAppsUpdate
}
