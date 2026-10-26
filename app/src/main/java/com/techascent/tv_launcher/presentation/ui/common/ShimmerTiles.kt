package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

private val ShimmerBase = LauncherColors.TileFallbackIconBackground.copy(alpha = 0.45f)
private val ShimmerHighlight = Color.White.copy(alpha = 0.18f)
private val TileShape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)

/** A row of [count] rectangular placeholders sharing one shimmer animation, matching [RectangleCard]. */
@Composable
internal fun ShimmerRectangleRow(count: Int, tileWidth: Dp, modifier: Modifier = Modifier) {
    val progress = rememberShimmerProgress()
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium)) {
        repeat(count) { ShimmerTile(progress, Modifier.width(tileWidth)) }
    }
}

/** A row of [count] round placeholders matching [CircularAppItem]. */
@Composable
internal fun ShimmerCircleRow(count: Int, size: Dp, modifier: Modifier = Modifier) {
    val progress = rememberShimmerProgress()
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.CircularItemGap)) {
        repeat(count) { Spacer(Modifier.size(size).clip(CircleShape).shimmer(progress)) }
    }
}

/** [rows] rows of [columns] round placeholders, matching the circular all apps grid. */
@Composable
internal fun ShimmerCircleGrid(rows: Int, columns: Int, size: Dp, gap: Dp) {
    val progress = rememberShimmerProgress()
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        repeat(rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                repeat(columns) { Spacer(Modifier.size(size).clip(CircleShape).shimmer(progress)) }
            }
        }
    }
}

/** [rows] rows of [columns] placeholder tiles, matching the all apps grid. */
@Composable
internal fun ShimmerRectangleGrid(rows: Int, columns: Int, tileWidth: Dp) {
    val progress = rememberShimmerProgress()
    Column(verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium)) {
        repeat(rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium)) {
                repeat(columns) { ShimmerTile(progress, Modifier.width(tileWidth)) }
            }
        }
    }
}

@Composable
private fun ShimmerTile(progress: State<Float>, modifier: Modifier) {
    Column(modifier) {
        Spacer(Modifier.fillMaxWidth().aspectRatio(16f / 9f).shimmer(progress))
        Spacer(Modifier.height(LauncherSpacing.Small))
        Spacer(
            Modifier
                .align(androidx.compose.ui.Alignment.CenterHorizontally)
                .fillMaxWidth(0.6f)
                .height(12.dp)
                .shimmer(progress),
        )
    }
}

@Composable
internal fun rememberShimmerProgress(): State<Float> =
    rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer_progress",
    )

// Progress is read inside drawBehind so the animation only redraws, never recomposes.
internal fun Modifier.shimmer(progress: State<Float>): Modifier = clip(TileShape).drawBehind {
    val band = size.width
    val start = -band + (size.width + band * 2) * progress.value
    drawRect(ShimmerBase)
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color.Transparent, ShimmerHighlight, Color.Transparent),
            start = Offset(start, 0f),
            end = Offset(start + band, size.height),
        ),
    )
}
