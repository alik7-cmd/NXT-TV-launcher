package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.ui.unit.Dp
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/** Column count and tile width that fill [availableWidth], shared by the favourites row. */
internal fun gridTileLayout(availableWidth: Dp): Pair<Int, Dp> {
    val width = availableWidth.coerceAtLeast(LauncherSpacing.TileWidth)
    val gap = LauncherSpacing.Medium
    val columns = ((width + gap) / (LauncherSpacing.TileWidth + gap)).toInt().coerceAtLeast(1)
    return columns to (width - gap * (columns - 1)) / columns
}
