package com.techascent.tv_launcher.presentation.ui

import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.techascent.tv_launcher.domain.model.WeatherCondition

/**
 * Atmospheric weather scene (sky glow, sun or moon, clouds, rain/snow/fog) that fades into the
 * launcher background on its left and bottom edges, like the stock launcher's hero art.
 *
 * Drawing objects are cached; one offscreen layer isolates the edge masks and applies the
 * scrolling fade without an additional intermediate alpha layer.
 */
@Composable
fun WeatherArt(
    condition: WeatherCondition,
    isNight: Boolean,
    modifier: Modifier = Modifier,
    collapseProgress: () -> Float = { 0f },
) {
    Spacer(
        modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                val progress = collapseProgress()
                alpha = (1f - progress * 1.4f).coerceAtLeast(0f)
                translationY = -0.35f * progress * size.height
            }
            .drawWithCache {
                val w = size.width
                val h = size.height
                val palette = weatherPalette(condition, isNight)
                val bodyCenter = Offset(w * 0.44f, h * 0.27f)
                val bodyRadius = w * 0.08f
                val cloudCenter = Offset(w * 0.54f, h * 0.4f)
                val cloudUnit = w * 0.15f

                val skyBrush = Brush.radialGradient(
                    0f to palette.skyInner,
                    0.5f to palette.skyOuter.copy(alpha = 0.75f),
                    1f to Color.Transparent,
                    center = Offset(w * 0.55f, h * 0.25f),
                    radius = w * 0.75f,
                )
                val glowBrush = Brush.radialGradient(
                    0f to (if (isNight) MoonGlow else SunGlow).copy(alpha = if (isNight) 0.5f else 0.7f),
                    0.4f to (if (isNight) Color(0xFFC9D8FF) else SunGlow).copy(alpha = 0.18f),
                    1f to Color.Transparent,
                    center = bodyCenter,
                    radius = bodyRadius * 3.2f,
                )
                val crescent = crescentPath(bodyCenter, bodyRadius)

                // Platform paints for soft (blurred) edges, which Compose can't do before API 31.
                fun softPaint(blur: Float, top: Color, bottom: Color, fromY: Float, toY: Float, alpha: Float) =
                    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
                        shader = LinearGradient(0f, fromY, 0f, toY, top.toArgb(), bottom.toArgb(), Shader.TileMode.CLAMP)
                        this.alpha = (alpha * 255).toInt()
                    }

                val backClouds = Path().apply {
                    addOval(Rect(Offset(w * 0.3f, h * 0.55f), w * 0.22f).copy(top = h * 0.48f, bottom = h * 0.62f))
                    addOval(Rect(Offset(w * 0.8f, h * 0.5f), w * 0.26f).copy(top = h * 0.42f, bottom = h * 0.58f))
                }.asAndroidPath()
                val backPaint = softPaint(24.dp.toPx(), palette.cloudBottom, palette.cloudBottom, 0f, h, 0.55f)

                // A second, dimmer cloud behind the main one adds depth to overcast scenes.
                val secondCloudCenter = Offset(w * 0.34f, h * 0.3f)
                val secondCloud = cloudPath(secondCloudCenter, cloudUnit * 0.7f).asAndroidPath()
                val secondPaint = softPaint(
                    blur = 8.dp.toPx(),
                    top = palette.cloudTop,
                    bottom = palette.cloudBottom,
                    fromY = secondCloudCenter.y - cloudUnit * 0.5f,
                    toY = secondCloudCenter.y + cloudUnit * 0.35f,
                    alpha = 0.6f,
                )
                val frontCloud = cloudPath(cloudCenter, cloudUnit).asAndroidPath()
                val frontPaint = softPaint(
                    blur = 6.dp.toPx(),
                    top = palette.cloudTop,
                    bottom = palette.cloudBottom,
                    fromY = cloudCenter.y - cloudUnit * 0.75f,
                    toY = cloudCenter.y + cloudUnit * 0.5f,
                    alpha = 0.95f,
                )
                val fogBands = Path().apply {
                    listOf(0.32f, 0.44f, 0.56f, 0.68f).forEachIndexed { i, fy ->
                        val left = w * (0.15f + 0.08f * (i % 2))
                        addRoundRect(
                            androidx.compose.ui.geometry.RoundRect(
                                left, h * fy, w * 0.98f, h * fy + h * 0.05f,
                                androidx.compose.ui.geometry.CornerRadius(h * 0.025f),
                            ),
                        )
                    }
                }.asAndroidPath()
                val fogPaint = softPaint(14.dp.toPx(), palette.cloudTop, palette.cloudTop, 0f, h, 0.4f)
                val bolt = boltPath(Offset(cloudCenter.x + cloudUnit * 0.1f, cloudCenter.y + cloudUnit * 0.45f), cloudUnit * 0.45f)
                val precipitationArea = Rect(
                    cloudCenter.x - cloudUnit * 0.8f,
                    cloudCenter.y + cloudUnit * 0.6f,
                    cloudCenter.x + cloudUnit * 0.9f,
                    cloudCenter.y + cloudUnit * 1.7f,
                )
                val fadeLeft = Brush.horizontalGradient(0f to Color.Transparent, 0.4f to Color.Black)
                val fadeBottom = Brush.verticalGradient(0.5f to Color.Black, 1f to Color.Transparent)

                onDrawBehind {
                    drawRect(skyBrush)
                    if (isNight && condition != WeatherCondition.Fog) {
                        val starAlpha = if (condition.hasCelestialBody) 0.75f else 0.25f
                        StarPoints.forEachIndexed { i, (fx, fy) ->
                            drawCircle(Color.White.copy(alpha = starAlpha * (0.5f + 0.5f * (i % 3) / 2f)), 1.2.dp.toPx(), Offset(w * fx, h * fy))
                        }
                    }
                    if (condition.hasCelestialBody) {
                        drawCircle(glowBrush, radius = bodyRadius * 3.2f, center = bodyCenter)
                        if (isNight) drawPath(crescent, MoonColor) else drawCircle(SunColor, bodyRadius, bodyCenter)
                    }
                    drawIntoCanvas { canvas ->
                        if (condition.hasCloud && condition != WeatherCondition.PartlyCloudy) {
                            canvas.nativeCanvas.drawPath(backClouds, backPaint)
                            canvas.nativeCanvas.drawPath(secondCloud, secondPaint)
                        }
                        if (condition.hasCloud) canvas.nativeCanvas.drawPath(frontCloud, frontPaint)
                        if (condition == WeatherCondition.Fog) canvas.nativeCanvas.drawPath(fogBands, fogPaint)
                    }
                    if (condition.isWet) {
                        drawPrecipitation(condition, precipitationArea, strokeWidth = 2.dp.toPx())
                    }
                    if (condition == WeatherCondition.Thunderstorm) {
                        drawCircle(BoltColor.copy(alpha = 0.25f), cloudUnit * 0.6f, bolt.getBounds().center)
                        drawPath(bolt, BoltColor)
                    }
                    // Blend the scene into the launcher background.
                    drawRect(fadeLeft, blendMode = BlendMode.DstIn)
                    drawRect(fadeBottom, blendMode = BlendMode.DstIn)
                }
            },
    )
}
