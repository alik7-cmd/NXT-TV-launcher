package com.techascent.tv_launcher.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.techascent.tv_launcher.R
import kotlin.math.abs
import com.techascent.tv_launcher.presentation.LauncherHeaderUiState
import com.techascent.tv_launcher.presentation.WeatherUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/**
 * Header drawn over the scrolling content. At the top it shows the large clock; as the content
 * scrolls it collapses into a compact, translucent bar (small clock, date and actions).
 *
 * The collapse progress is read only inside draw and layer lambdas, so scrolling never
 * recomposes the header — important for smooth D-pad scrolling on low-end TVs.
 */
@Composable
fun CollapsingLauncherHeader(
    state: LauncherHeaderUiState,
    scrollState: ScrollState,
    contentFocus: FocusRequester,
    onInputClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onActionsFocused: () -> Unit,
    onHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    weather: WeatherUiState.Content? = null,
) {
    val density = LocalDensity.current
    val collapsedPx = with(density) { LauncherSpacing.HeaderCollapsedHeight.toPx() }
    val scrimPx = with(density) { (LauncherSpacing.HeaderCollapsedHeight + LauncherSpacing.HeaderEdgeFade).toPx() }
    // Expanded actions sit level with the greeting, collapsed ones are centred in the bar.
    val actionsShiftPx = with(density) {
        (LauncherSpacing.HeaderTopPadding - (LauncherSpacing.HeaderCollapsedHeight - LauncherSpacing.ButtonSize) / 2).toPx()
    }
    var expandedPx by remember { mutableIntStateOf(0) }
    val progress = rememberHeaderCollapseProgress(scrollState) { expandedPx }
    val clock = rememberClockText(state.dateTime)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged {
                expandedPx = it.height
                onHeightChanged(it.height)
            }
            .drawBehind {
                val p = progress()
                if (p > 0f) {
                    // Translucent scrim: content stays visible beneath the bar but never competes with the text.
                    drawRect(
                        Brush.verticalGradient(
                            0f to LauncherColors.BackgroundTop.copy(alpha = 0.92f * p),
                            collapsedPx / scrimPx to LauncherColors.BackgroundTop.copy(alpha = 0.72f * p),
                            1f to LauncherColors.BackgroundTop.copy(alpha = 0f),
                            endY = scrimPx,
                        ),
                        size = Size(size.width, scrimPx),
                    )
                }
            },
    ) {
        ExpandedClock(
            greeting = stringResource(state.greetingResId),
            clock = clock,
            modifier = Modifier
                .padding(
                    start = LauncherSpacing.ScreenHorizontal,
                    top = LauncherSpacing.HeaderTopPadding,
                    bottom = LauncherSpacing.Medium,
                )
                .graphicsLayer {
                    val p = progress()
                    alpha = (1f - p * 1.6f).coerceAtLeast(0f)
                    scaleX = 1f - 0.12f * p
                    scaleY = scaleX
                    translationY = -0.3f * p * expandedPx
                    transformOrigin = TransformOrigin(0f, 0f)
                },
        )

        Box(
            Modifier
                .height(LauncherSpacing.HeaderCollapsedHeight)
                .padding(start = LauncherSpacing.ScreenHorizontal)
                .clearAndSetSemantics { }
                .graphicsLayer {
                    val p = ((progress() - 0.45f) / 0.55f).coerceIn(0f, 1f)
                    alpha = p
                    translationY = (1f - p) * 12.dp.toPx()
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            CompactClock(clock)
        }

        Row(
            Modifier
                .align(Alignment.TopEnd)
                .height(LauncherSpacing.HeaderCollapsedHeight)
                .padding(end = LauncherSpacing.ScreenHorizontal)
                .graphicsLayer { translationY = (1f - progress()) * actionsShiftPx }
                // The scrolling content sits underneath this bar, so geometric focus search
                // can't find it; send Down to the first row explicitly.
                .focusProperties { down = contentFocus }
                .onFocusChanged { if (it.hasFocus) onActionsFocused() },
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (weather != null) {
                CompactWeather(
                    state = weather,
                    modifier = Modifier
                        .padding(end = LauncherSpacing.Small)
                        .graphicsLayer { alpha = ((progress() - 0.45f) / 0.55f).coerceIn(0f, 1f) },
                )
            }
            DeviceActionButton(
                label = stringResource(R.string.inputs),
                iconResId = R.drawable.ic_input,
                onClick = onInputClick,
            )
            DeviceActionButton(
                label = stringResource(R.string.settings),
                iconResId = R.drawable.ic_settings,
                onClick = onSettingsClick,
            )
        }
    }
}

/**
 * Collapse progress of the header: 0 = fully expanded, 1 = compact bar. Returned as a lambda so
 * it is read only in draw/layer blocks and scrolling never triggers recomposition.
 */
@Composable
fun rememberHeaderCollapseProgress(scrollState: ScrollState, expandedHeightPx: () -> Int): () -> Float {
    val density = LocalDensity.current
    val collapsedPx = with(density) { LauncherSpacing.HeaderCollapsedHeight.toPx() }
    val delayPx = with(density) { LauncherSpacing.HeaderCollapseDelay.toPx() }
    val currentExpandedHeight by rememberUpdatedState(expandedHeightPx)
    return remember(scrollState, collapsedPx, delayPx) {
        {
            val range = (currentExpandedHeight() - collapsedPx).coerceAtLeast(1f)
            ((scrollState.value - delayPx) / range).coerceIn(0f, 1f)
        }
    }
}

/**
 * TV-style pivot scrolling (focused row ~30% into the area below the compact bar) that never
 * rests with the header half-collapsed: positions inside the collapse range snap open or closed.
 * Tiny corrections for an already visible item are ignored to keep sideways moves still.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberCollapsingHeaderScrollSpec(scrollState: ScrollState, headerHeightPx: Int): BringIntoViewSpec {
    val density = LocalDensity.current
    val topInset = with(density) { LauncherSpacing.HeaderCollapsedHeight.toPx() }
    val snapRange = (with(density) { LauncherSpacing.HeaderCollapseDelay.toPx() } + headerHeightPx - topInset)
        .coerceAtLeast(0f)
    val deadBand = with(density) { SCROLL_DEAD_BAND.toPx() }
    return remember(scrollState, snapRange, topInset, deadBand) {
        object : BringIntoViewSpec {
            override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
                val visible = containerSize - topInset
                val pivot = topInset + PIVOT_FRACTION * visible
                val target = if (size <= visible && containerSize - pivot < size) containerSize - size else pivot
                val resting = scrollState.value + offset - target
                val snapped = when {
                    resting <= snapRange / 2 -> 0f
                    resting < snapRange -> snapRange
                    else -> resting
                }
                val distance = snapped - scrollState.value
                // The focus zoom (graphicsLayer scale) changes the tile's measured bounds while it
                // animates. Without this, every Left/Right press nudged the page down and back up.
                // Snap targets (header fully open or closed) are fixed, so they always finish exactly.
                val fullyVisible = offset >= topInset && offset + size <= containerSize
                val snapping = snapped == 0f || snapped == snapRange
                return if (!snapping && fullyVisible && abs(distance) < deadBand) 0f else distance
            }
        }
    }
}

private const val PIVOT_FRACTION = 0.3f
private val SCROLL_DEAD_BAND = 24.dp
