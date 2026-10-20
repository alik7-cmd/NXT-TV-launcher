package com.techascent.tv_launcher.domain.model

import java.time.LocalTime

data class Weather(
    val city: String?,
    val temperatureC: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val highC: Double,
    val lowC: Double,
    /** First hour in the next 12 hours that is likely to bring rain, when it isn't raining now. */
    val rainExpectedAt: LocalTime?,
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
