package com.techascent.tv_launcher.domain.model

import java.time.LocalTime
import java.time.LocalDate
import java.time.ZoneId

data class Weather(
    val city: String?,
    val temperatureC: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val highC: Double,
    val lowC: Double,
    /** First hour in the next 12 hours that is likely to bring rain, when it isn't raining now. */
    val rainExpectedAt: LocalTime?,
    val timezone: ZoneId = ZoneId.of("UTC"),
    val dailyForecast: List<DailyForecast> = emptyList(),
)

data class DailyForecast(
    val date: LocalDate,
    val condition: WeatherCondition?,
    val highC: Double?,
    val lowC: Double?,
    val maxPrecipitationProbability: Int?,
)

enum class WeatherCondition {
    Clear,
    PartlyCloudy,
    Cloudy,
    Fog,
    Drizzle,
    Rain,
    Snow,
    Thunderstorm,
    ;

    val isWet: Boolean get() = this == Drizzle || this == Rain || this == Snow || this == Thunderstorm
}
