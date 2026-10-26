package com.techascent.tv_launcher.presentation.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.LauncherHeaderUiState
import com.techascent.tv_launcher.presentation.state.LauncherUiState
import com.techascent.tv_launcher.presentation.state.UiState
import com.techascent.tv_launcher.ui.theme.TVLauncherTheme
import java.time.ZonedDateTime

private val previewApps = listOf("Netflix", "YouTube", "Disney+", "Plex", "Spotify", "Prime Video", "Settings")
    .map { LauncherAppUiState("example.${it.lowercase()}", "", it, null) }

@Preview(name = "Launcher", widthDp = 960, heightDp = 540)
@Composable
private fun LauncherHomePreview() {
    LauncherHomePreview(
        LauncherUiState(
            header = UiState.Success(LauncherHeaderUiState.from(ZonedDateTime.parse("2026-10-19T09:56:00+02:00"))),
            recentApps = UiState.Success(previewApps.take(RECENT_APPS_SLOTS)),
            allApps = UiState.Success(previewApps),
        ),
    )
}

@Preview(name = "Launcher - loading", widthDp = 960, heightDp = 540)
@Composable
private fun LauncherHomeLoadingPreview() = LauncherHomePreview(LauncherUiState())

@Composable
private fun LauncherHomePreview(uiState: LauncherUiState) {
    TVLauncherTheme {
        LauncherHome(
            uiState = uiState,
            onAppClick = {},
            onAppLongClick = {},
            onUninstall = {},
            onOptionsDismiss = {},
            onInputClick = {},
            onSettingsClick = {},
            onCustomizeClick = {},
            onAppStyleSelected = {},
            onCustomizeDismiss = {},
            onShowAppNamesSelected = {},
            onWallpaperApplied = {},
        )
    }
}
