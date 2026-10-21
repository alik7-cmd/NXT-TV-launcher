package com.techascent.tv_launcher.domain.model

sealed interface LauncherAppsResult {
    data object Init : LauncherAppsResult

    data class Success(
        val apps: List<InstalledApp>,
        val favoritePackages: Set<String>,
    ) : LauncherAppsResult

    data class Error(
        val cause: Throwable,
        val previous: Success? = null,
    ) : LauncherAppsResult
}
