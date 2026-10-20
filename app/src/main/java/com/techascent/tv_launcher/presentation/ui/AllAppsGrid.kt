package com.techascent.tv_launcher.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.techascent.tv_launcher.presentation.LauncherAppUiState
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/** Column count and tile width that fill [availableWidth], shared by the favourites row. */
internal fun gridTileLayout(availableWidth: Dp): Pair<Int, Dp> {
    val width = availableWidth.coerceAtLeast(LauncherSpacing.TileWidth)
    val gap = LauncherSpacing.Medium
    val columns = ((width + gap) / (LauncherSpacing.TileWidth + gap)).toInt().coerceAtLeast(1)
    return columns to (width - gap * (columns - 1)) / columns
}

/**
 * Non-lazy grid: a TV's app list is small, so every tile is composed once and D-pad scrolling
 * only moves pixels instead of disposing and rebuilding rows as they leave and re-enter view.
 */
@Composable
fun AllAppsGrid(
    apps: List<LauncherAppUiState>,
    tile: @Composable (app: LauncherAppUiState, modifier: Modifier) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val (columns, tileWidth) = gridTileLayout(maxWidth)
        Column(verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium)) {
            apps.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium)) {
                    row.forEach { app ->
                        key(app.packageName) { tile(app, Modifier.width(tileWidth)) }
                    }
                }
            }
        }
    }
}
