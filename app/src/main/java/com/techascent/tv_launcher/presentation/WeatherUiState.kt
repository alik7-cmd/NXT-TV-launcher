package com.techascent.tv_launcher.presentation

import androidx.annotation.StringRes
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.Weather
import com.techascent.tv_launcher.domain.model.WeatherCondition
import java.time.LocalTime
import kotlin.math.roundToInt

sealed interface WeatherUiState {
    /** Nothing to show yet (loading, offline or the services are unreachable). */
    data object Hidden : WeatherUiState

    data class Content(
        val temperature: Int,
        val condition: WeatherCondition,
        val isNight: Boolean,
        @param:StringRes val conditionResId: Int,
        val city: String?,
        val high: Int,
        val low: Int,
        val rainExpectedAt: LocalTime?,
    ) : WeatherUiState

    companion object {
        fun from(weather: Weather) = Content(
            temperature = weather.temperatureC.roundToInt(),
            condition = weather.condition,
            isNight = !weather.isDay,
            conditionResId = when (weather.condition) {
                WeatherCondition.Clear -> if (weather.isDay) R.string.weather_sunny else R.string.weather_clear
                WeatherCondition.PartlyCloudy -> R.string.weather_partly_cloudy
                WeatherCondition.Cloudy -> R.string.weather_cloudy
                WeatherCondition.Fog -> R.string.weather_fog
                WeatherCondition.Drizzle -> R.string.weather_drizzle
                WeatherCondition.Rain -> R.string.weather_rain
                WeatherCondition.Snow -> R.string.weather_snow
                WeatherCondition.Thunderstorm -> R.string.weather_thunderstorm
            },
            city = weather.city,
            high = weather.highC.roundToInt(),
            low = weather.lowC.roundToInt(),
            rainExpectedAt = weather.rainExpectedAt,
        )
    }
}
