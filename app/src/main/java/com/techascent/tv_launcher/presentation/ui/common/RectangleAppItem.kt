package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
fun RectangleAppItem(
    app: LauncherAppUiState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    shouldShowAppName: Boolean = false,
) {
    RectangleCard(
        label = app.label,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shouldShowAppName = shouldShowAppName,
    ) {
        val banner = app.banner
        if (banner != null) {
            Image(
                bitmap = banner,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AppIcon(app, LauncherSpacing.DockTileIconSize)
        }
    }
}
