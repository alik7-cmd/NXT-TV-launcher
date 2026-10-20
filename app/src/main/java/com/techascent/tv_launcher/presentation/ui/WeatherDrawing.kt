package com.techascent.tv_launcher.presentation.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.techascent.tv_launcher.domain.model.WeatherCondition

/** Shared shapes and colours for the weather hero art and the compact weather icon. */
internal data class WeatherPalette(
    val skyInner: Color,
    val skyOuter: Color,
    val cloudTop: Color,
    val cloudBottom: Color,
)

internal fun weatherPalette(condition: WeatherCondition, isNight: Boolean): WeatherPalette {
    val whiteCloud = Color(0xFFF4F7FC) to Color(0xFFBCCBE0)
    val greyCloud = Color(0xFFB4BFD0) to Color(0xFF66738C)
    val (sky, cloud) = when (condition) {
        WeatherCondition.Clear, WeatherCondition.PartlyCloudy ->
            if (isNight) (Color(0xFF3B5D9A) to Color(0xFF22345A)) to (Color(0xFFDCE6F7) to Color(0xFF8FA3C4))
            else (Color(0xFF6FA8E8) to Color(0xFF2F5C9C)) to whiteCloud
        WeatherCondition.Cloudy ->
            if (isNight) (Color(0xFF34425C) to Color(0xFF1C2840)) to greyCloud
            else (Color(0xFF5A7090) to Color(0xFF2A3A55)) to (Color(0xFFD6DDE8) to Color(0xFF8A98AE))
        WeatherCondition.Fog -> (Color(0xFF6B7A90) to Color(0xFF2E3A4E)) to (Color(0xFFC8D2E0) to Color(0xFF8A98AE))
        WeatherCondition.Drizzle, WeatherCondition.Rain -> (Color(0xFF4A5A73) to Color(0xFF1E2838)) to greyCloud
        WeatherCondition.Snow -> (Color(0xFF8FA8C8) to Color(0xFF2A3E5C)) to whiteCloud
        WeatherCondition.Thunderstorm -> (Color(0xFF3E3F66) to Color(0xFF1A1D33)) to (Color(0xFF6A7290) to Color(0xFF363C55))
    }
    return WeatherPalette(sky.first, sky.second, cloud.first, cloud.second)
}

internal val WeatherCondition.hasCelestialBody: Boolean
    get() = this == WeatherCondition.Clear || this == WeatherCondition.PartlyCloudy

internal val WeatherCondition.hasCloud: Boolean
    get() = this != WeatherCondition.Clear && this != WeatherCondition.Fog

internal val SunColor = Color(0xFFFFF3B0)
internal val SunGlow = Color(0xFFFFD66B)
internal val MoonColor = Color(0xFFF6EBD0)
internal val MoonGlow = Color(0xFFFFF4D6)
internal val RainColor = Color(0xFF9DB4D6)
internal val BoltColor = Color(0xFFFFE27A)

/** Cartoon cloud made of overlapping puffs; [unit] is roughly half the cloud's width. */
internal fun cloudPath(center: Offset, unit: Float): Path = Path().apply {
    addOval(Rect(center.x - unit, center.y - 0.2f * unit, center.x + unit, center.y + 0.5f * unit))
    addOval(Rect(Offset(center.x - 0.38f * unit, center.y - 0.02f * unit), 0.42f * unit))
    addOval(Rect(Offset(center.x + 0.08f * unit, center.y - 0.2f * unit), 0.54f * unit))
    addOval(Rect(Offset(center.x + 0.58f * unit, center.y + 0.06f * unit), 0.34f * unit))
}

internal fun crescentPath(center: Offset, radius: Float): Path = Path().apply {
    op(
        Path().apply { addOval(Rect(center, radius)) },
        Path().apply { addOval(Rect(Offset(center.x + 0.42f * radius, center.y - 0.28f * radius), 0.9f * radius)) },
        PathOperation.Difference,
    )
}

internal fun boltPath(top: Offset, unit: Float): Path = Path().apply {
    moveTo(top.x, top.y)
    lineTo(top.x - 0.45f * unit, top.y + 0.95f * unit)
    lineTo(top.x - 0.05f * unit, top.y + 0.95f * unit)
    lineTo(top.x - 0.35f * unit, top.y + 1.9f * unit)
    lineTo(top.x + 0.45f * unit, top.y + 0.7f * unit)
    lineTo(top.x + 0.05f * unit, top.y + 0.7f * unit)
    lineTo(top.x + 0.3f * unit, top.y)
    close()
}

/** Slanted streaks (rain/drizzle) or flakes (snow) spread over [area]. */
internal fun DrawScope.drawPrecipitation(
    condition: WeatherCondition,
    area: Rect,
    strokeWidth: Float,
    alpha: Float = 1f,
) {
    val snowing = condition == WeatherCondition.Snow
    val drops = if (condition == WeatherCondition.Drizzle) PrecipitationPoints.filterIndexed { i, _ -> i % 2 == 0 }
    else PrecipitationPoints
    drops.forEach { (fx, fy) ->
        val start = Offset(area.left + fx * area.width, area.top + fy * area.height)
        if (snowing) {
            drawCircle(Color.White.copy(alpha = 0.9f * alpha), radius = strokeWidth * (1.1f + fx % 0.3f * 2f), center = start)
        } else {
            val length = area.height * 0.22f
            drawLine(
                color = RainColor.copy(alpha = 0.8f * alpha),
                start = start,
                end = Offset(start.x - length * 0.35f, start.y + length),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

private val PrecipitationPoints = listOf(
    0.05f to 0.05f, 0.25f to 0.0f, 0.45f to 0.08f, 0.65f to 0.02f, 0.85f to 0.07f,
    0.15f to 0.45f, 0.35f to 0.52f, 0.55f to 0.44f, 0.75f to 0.5f, 0.95f to 0.46f,
)

/** Fixed star field (fractions of the art's size), so the sky never shimmers between frames. */
internal val StarPoints = listOf(
    0.08f to 0.12f, 0.22f to 0.36f, 0.31f to 0.08f, 0.66f to 0.14f, 0.74f to 0.42f,
    0.88f to 0.1f, 0.95f to 0.3f, 0.52f to 0.05f, 0.42f to 0.62f, 0.9f to 0.58f,
)
