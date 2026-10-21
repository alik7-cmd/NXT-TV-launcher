package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
fun AppTileV2(
    app: LauncherAppUiState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    options: @Composable () -> Unit = {},
) {
    // The popup anchors to this Box, i.e. the whole tile including its label.
    Box(modifier) {
        TileV2(label = app.label, onClick = onClick, onLongClick = onLongClick) {
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
        options()
    }
}
