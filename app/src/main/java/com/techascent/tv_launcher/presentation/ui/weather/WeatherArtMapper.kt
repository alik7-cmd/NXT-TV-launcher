package com.techascent.tv_launcher.presentation.ui.weather

import androidx.compose.runtime.Immutable
import com.techascent.tv_launcher.domain.model.WeatherCondition
import com.techascent.tv_launcher.presentation.state.WeatherUiState

/** Everything [WeatherArtV2] needs to draw one scene, so the drawing code has no condition checks. */
@Immutable
internal data class WeatherArtSpec(
    val palette: WeatherPalette,
    val isNight: Boolean,
    val showBody: Boolean,
    val showCloud: Boolean,
    val showFog: Boolean,
    val showBolt: Boolean,
    /** Set when rain, drizzle or snow should fall. */
    val precipitation: WeatherCondition?,
    /** Whether the scene moves; false draws a still image. */
    val animated: Boolean,
)

internal fun WeatherUiState.Content.toArtSpec() = WeatherArtSpec(
    palette = weatherPalette(condition, isNight),
    isNight = isNight,
    showBody = condition.hasCelestialBody,
    showCloud = condition.hasCloud,
    showFog = condition == WeatherCondition.Fog,
    showBolt = condition == WeatherCondition.Thunderstorm,
    precipitation = condition.takeIf { it.isWet },
    animated = animateArt,
)
