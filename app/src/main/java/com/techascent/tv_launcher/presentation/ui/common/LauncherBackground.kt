package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.techascent.tv_launcher.ui.theme.LauncherColors

/** Dark gradient with a soft coloured glow behind the app row and a subtle edge vignette. */
fun Modifier.launcherBackground(
    palette: LauncherBackgroundPalette = LauncherBackgroundPalette.Navy,
): Modifier = drawWithCache {
    val base = Brush.verticalGradient(
        colors = listOf(
            palette.top,
            palette.middle,
            palette.bottom,
        ),
    )
    val glow = Brush.radialGradient(
        colors = listOf(palette.glow, Color.Transparent),
        center = Offset(size.width * 0.35f, size.height * 0.8f),
        radius = size.maxDimension * 0.55f,
    )
    val vignette = Brush.radialGradient(
        colors = listOf(Color.Transparent, LauncherColors.BackgroundVignette),
        center = size.center,
        radius = size.maxDimension * 0.75f,
    )
    onDrawBehind {
        drawRect(base)
        drawRect(glow)
        drawRect(vignette)
    }
}
