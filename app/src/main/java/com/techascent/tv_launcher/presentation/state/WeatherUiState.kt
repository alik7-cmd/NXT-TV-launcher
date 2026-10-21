package com.techascent.tv_launcher.presentation.state

import androidx.annotation.StringRes
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.Weather
import com.techascent.tv_launcher.domain.model.WeatherCondition
import java.time.LocalTime
import java.time.LocalDate
import kotlin.math.roundToInt

sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    /** Weather is disabled or unavailable. */
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
        /** Whether the weather art should move; false draws it as a still image. */
        val animateArt: Boolean = true,
        val showTemperatureChart: Boolean = false,
        val nextDays: List<DailyForecastUiState> = emptyList(),
    ) : WeatherUiState

    companion object {
        fun from(
            weather: Weather,
            animateArt: Boolean = true,
            today: LocalDate = LocalDate.now(weather.timezone),
            showTemperatureChart: Boolean = false,
        ) = Content(
            temperature = weather.temperatureC.roundToInt(),
            condition = weather.condition,
            isNight = !weather.isDay,
            conditionResId = weather.condition.descriptionResId(isNight = !weather.isDay),
            city = weather.city,
            high = weather.highC.roundToInt(),
            low = weather.lowC.roundToInt(),
            rainExpectedAt = weather.rainExpectedAt,
            animateArt = animateArt,
            showTemperatureChart = showTemperatureChart,
            nextDays = weather.dailyForecast
                .filter { it.date.isAfter(today) }
                .sortedBy { it.date }
                .distinctBy { it.date }
                .take(5)
                .map {
                    DailyForecastUiState(
                        date = it.date,
                        condition = it.condition,
                        high = it.highC?.roundToInt(),
                        low = it.lowC?.roundToInt(),
                        maxPrecipitationProbability = it.maxPrecipitationProbability,
                    )
                },
        )
    }
}

data class DailyForecastUiState(
    val date: LocalDate,
    val condition: WeatherCondition?,
    val high: Int?,
    val low: Int?,
    val maxPrecipitationProbability: Int?,
)

@StringRes
internal fun WeatherCondition.descriptionResId(isNight: Boolean = false): Int = when (this) {
    WeatherCondition.Clear -> if (isNight) R.string.weather_clear else R.string.weather_sunny
    WeatherCondition.PartlyCloudy -> R.string.weather_partly_cloudy
    WeatherCondition.Cloudy -> R.string.weather_cloudy
    WeatherCondition.Fog -> R.string.weather_fog
    WeatherCondition.Drizzle -> R.string.weather_drizzle
    WeatherCondition.Rain -> R.string.weather_rain
    WeatherCondition.Snow -> R.string.weather_snow
    WeatherCondition.Thunderstorm -> R.string.weather_thunderstorm
}
