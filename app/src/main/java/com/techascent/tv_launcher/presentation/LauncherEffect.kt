package com.techascent.tv_launcher.presentation

sealed interface LauncherEffect {
    data class LaunchApp(
        val packageName: String,
        val activityName: String,
    ) : LauncherEffect

    data class UninstallApp(val packageName: String) : LauncherEffect

    data object OpenInputs : LauncherEffect
    data object OpenSettings : LauncherEffect
    data object OpenPlayStore : LauncherEffect
}
