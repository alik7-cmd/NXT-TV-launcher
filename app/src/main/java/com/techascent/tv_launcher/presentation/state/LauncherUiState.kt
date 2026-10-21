package com.techascent.tv_launcher.presentation.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.techascent.tv_launcher.domain.model.InstalledApp

@Immutable
data class LauncherAppUiState(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: ImageBitmap?,
    val isFavorite: Boolean = false,
    val isRemovable: Boolean = false,
    val banner: ImageBitmap? = null,
) {
    companion object {
        fun from(app: InstalledApp) = LauncherAppUiState(
            packageName = app.packageName,
            activityName = app.activityName,
            label = app.label,
            icon = app.icon?.asImageBitmap(),
            isRemovable = app.isRemovable,
            banner = app.banner?.asImageBitmap(),
        )
    }
}

data class LauncherUiState(
    val header: UiState<LauncherHeaderUiState> = UiState.Loading,
    val favorites: UiState<List<LauncherAppUiState>> = UiState.Loading,
    val allApps: UiState<List<LauncherAppUiState>> = UiState.Loading,
    /** App whose options popup is open, or null when no popup is shown. */
    val appOptions: LauncherAppUiState? = null,
    val favoriteOptions: Boolean = false,
    val favoritePickerApps: List<LauncherAppUiState>? = null,
)
