package com.techascent.tv_launcher.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.techascent.tv_launcher.domain.model.WeatherCondition

/** Small flat glyph of the current conditions, drawn with the same shapes as [WeatherArt]. */
@Composable
fun WeatherIcon(condition: WeatherCondition, isNight: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val palette = weatherPalette(condition, isNight)
        val wet = condition.isWet
        val bodyCenter = Offset(w * 0.38f, w * 0.34f)
        val bodyRadius = w * 0.24f
        if (condition.hasCelestialBody) {
            if (isNight) drawPath(crescentPath(bodyCenter, bodyRadius), MoonColor)
            else drawCircle(SunColor, bodyRadius, bodyCenter)
        }
        if (condition.hasCloud || condition == WeatherCondition.Fog) {
            val unit = w * 0.36f
            val center = Offset(w * 0.52f, if (wet) w * 0.42f else w * 0.58f)
            drawPath(
                cloudPath(center, unit),
                // Always light, so the glyph stays legible on the translucent bar.
                Brush.verticalGradient(
                    listOf(IconCloudTop, if (condition == WeatherCondition.Thunderstorm) palette.cloudBottom else IconCloudBottom),
                    startY = center.y - unit * 0.75f,
                    endY = center.y + unit * 0.5f,
                ),
            )
            if (wet) {
                drawPrecipitation(
                    condition,
                    Rect(w * 0.2f, w * 0.72f, w * 0.85f, w * 1.0f),
                    strokeWidth = w * 0.06f,
                )
            }
        }
    }
}

private val IconCloudTop = Color(0xFFF0F4FA)
private val IconCloudBottom = Color(0xFFAEBBD0)
