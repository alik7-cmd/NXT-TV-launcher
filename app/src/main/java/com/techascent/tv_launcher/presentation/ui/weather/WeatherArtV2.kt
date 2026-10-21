package com.techascent.tv_launcher.presentation.ui.weather

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.techascent.tv_launcher.domain.model.WeatherCondition
import kotlin.math.PI
import kotlin.math.sin

/**
 * Simplified hero scene: sky glow, sun or moon, one cloud, precipitation, fog and bolt, fading into
 * the background on the left and bottom. Plain gradients (no blur masks) and a single offscreen
 * layer keep it cheap; [alpha] is read in the layer so fading never recomposes.
 *
 * With [WeatherArtSpec.animated] it moves gently (drifting cloud, pulsing glow, twinkling stars, falling rain or
 * snow) from one looping phase that is only read while drawing, and stops once [alpha] reaches 0.
 * Otherwise it draws a still scene.
 */
@Composable
internal fun WeatherArtV2(
    spec: WeatherArtSpec,
    modifier: Modifier = Modifier,
    alpha: () -> Float = { 1f },
) {
    val visible by remember { derivedStateOf { alpha() > 0f } }
    val phase: State<Float> = if (spec.animated && visible) {
        rememberInfiniteTransition(label = "weatherArt").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(PERIOD_MS, easing = LinearEasing), RepeatMode.Restart),
            label = "weatherArtPhase",
        )
    } else {
        remember { mutableStateOf(0f) }
    }
    Spacer(
        modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                this.alpha = alpha()
            }
            .drawWithCache {
                val w = size.width
                val h = size.height
                val palette = spec.palette
                val bodyCenter = Offset(w * 0.44f, h * 0.27f)
                val bodyRadius = w * 0.08f
                val cloudCenter = Offset(w * 0.54f, h * 0.4f)
                val cloudUnit = w * 0.15f

                val sky = Brush.radialGradient(
                    0f to palette.skyInner,
                    0.5f to palette.skyOuter.copy(alpha = 0.75f),
                    1f to Color.Transparent,
                    center = Offset(w * 0.55f, h * 0.25f),
                    radius = w * 0.75f,
                )
                val glowColor = if (spec.isNight) MoonGlow else SunGlow
                val glow = Brush.radialGradient(
                    0f to glowColor.copy(alpha = if (spec.isNight) 0.5f else 0.7f),
                    1f to Color.Transparent,
                    center = bodyCenter,
                    radius = bodyRadius * 3.2f,
                )
                val cloudBrush = Brush.verticalGradient(
                    colors = listOf(palette.cloudTop, palette.cloudBottom),
                    startY = cloudCenter.y - cloudUnit * 0.75f,
                    endY = cloudCenter.y + cloudUnit * 0.5f,
                )
                val cloud = cloudPath(cloudCenter, cloudUnit)
                val crescent = crescentPath(bodyCenter, bodyRadius)
                val bolt = boltPath(
                    Offset(
                        cloudCenter.x + cloudUnit * 0.1f,
                        cloudCenter.y + cloudUnit * 0.45f
                    ), cloudUnit * 0.45f
                )
                val rainArea = Rect(
                    cloudCenter.x - cloudUnit * 0.8f,
                    cloudCenter.y + cloudUnit * 0.6f,
                    cloudCenter.x + cloudUnit * 0.9f,
                    cloudCenter.y + cloudUnit * 1.7f,
                )
                val fadeLeft =
                    Brush.horizontalGradient(0f to Color.Transparent, 0.4f to Color.Black)
                val fadeBottom =
                    Brush.verticalGradient(0.5f to Color.Black, 1f to Color.Transparent)

                onDrawBehind {
                    val t = phase.value
                    drawRect(sky)
                    if (spec.isNight && !spec.showFog) {
                        StarPoints.forEachIndexed { i, (fx, fy) ->
                            val twinkle = if (spec.animated) 0.65f + 0.35f * wave(t * 3f + i * 0.17f) else 1f
                            drawCircle(
                                Color.White.copy(alpha = (if (spec.showBody) 0.7f else 0.25f) * twinkle),
                                1.2.dp.toPx(),
                                Offset(w * fx, h * fy)
                            )
                        }
                    }
                    if (spec.showBody) {
                        drawCircle(glow, radius = bodyRadius * 3.2f, center = bodyCenter, alpha = if (spec.animated) 0.85f + 0.15f * wave(t * 2f) else 1f)
                        if (spec.isNight) drawPath(crescent, MoonColor) else drawCircle(
                            SunColor,
                            bodyRadius,
                            bodyCenter
                        )
                    }
                    if (spec.showFog) {
                        listOf(0.32f, 0.44f, 0.56f, 0.68f).forEachIndexed { i, fy ->
                            val left = w * (0.15f + 0.08f * (i % 2))
                            drawRect(
                                palette.cloudTop.copy(alpha = 0.35f),
                                topLeft = Offset(left, h * fy),
                                size = androidx.compose.ui.geometry.Size(
                                    w * 0.98f - left,
                                    h * 0.05f
                                ),
                            )
                        }
                    }
                    translate(left = if (spec.animated) cloudUnit * 0.12f * wave(t) else 0f) {
                        if (spec.showCloud) drawPath(cloud, cloudBrush)
                        spec.precipitation?.let {
                            val fall = (if (it == WeatherCondition.Snow) t * 2f else t * 6f) % 1f
                            drawPrecipitation(it, rainArea, strokeWidth = 2.dp.toPx(), progress = fall.takeIf { spec.animated })
                        }
                        if (spec.showBolt) drawPath(bolt, BoltColor)
                    }
                    drawRect(fadeLeft, blendMode = BlendMode.DstIn)
                    drawRect(fadeBottom, blendMode = BlendMode.DstIn)
                }
            },
    )
}

private const val PERIOD_MS = 12_000

/** Smooth -1..1 oscillation completing one cycle per unit of [x]. */
private fun wave(x: Float): Float = sin(x * 2f * PI.toFloat())
