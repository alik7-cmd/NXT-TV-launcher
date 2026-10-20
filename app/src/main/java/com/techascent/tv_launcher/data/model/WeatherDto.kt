package com.techascent.tv_launcher.data.model

import com.techascent.tv_launcher.domain.model.Weather
import com.techascent.tv_launcher.domain.model.WeatherCondition
import java.time.LocalDateTime

data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val city: String?,
)

/** Open-Meteo forecast; times are local to the forecast location. */
data class WeatherDto(
    val city: String?,
    val time: LocalDateTime,
    val temperatureC: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val highC: Double,
    val lowC: Double,
    val hourlyPrecipitation: List<HourlyPrecipitationDto>,
) {
    fun toDomain(): Weather {
        val condition = conditionFor(weatherCode)
        return Weather(
            city = city,
            temperatureC = temperatureC,
            condition = condition,
            isDay = isDay,
            highC = highC,
            lowC = lowC,
            rainExpectedAt = if (condition.isWet) null else hourlyPrecipitation
                .firstOrNull {
                    it.time.isAfter(time) &&
                        !it.time.isAfter(time.plusHours(RAIN_LOOKAHEAD_HOURS)) &&
                        it.probabilityPercent >= RAIN_PROBABILITY_PERCENT
                }
                ?.time
                ?.toLocalTime(),
        )
    }

    private companion object {
        const val RAIN_LOOKAHEAD_HOURS = 12L
        const val RAIN_PROBABILITY_PERCENT = 50

        /** WMO weather interpretation codes, as documented by Open-Meteo. */
        fun conditionFor(code: Int) = when (code) {
            0 -> WeatherCondition.Clear
            1, 2 -> WeatherCondition.PartlyCloudy
            45, 48 -> WeatherCondition.Fog
            in 51..57 -> WeatherCondition.Drizzle
            in 61..67, in 80..82 -> WeatherCondition.Rain
            in 71..77, 85, 86 -> WeatherCondition.Snow
            in 95..99 -> WeatherCondition.Thunderstorm
            else -> WeatherCondition.Cloudy
        }
    }
}

data class HourlyPrecipitationDto(
    val time: LocalDateTime,
    val probabilityPercent: Int,
)
