package com.techascent.tv_launcher.data

import com.techascent.tv_launcher.data.model.HourlyPrecipitationDto
import com.techascent.tv_launcher.data.model.WeatherDto
import com.techascent.tv_launcher.data.model.DailyForecastDto
import com.techascent.tv_launcher.domain.model.WeatherCondition
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherDtoTest {
    private val now = LocalDateTime.of(2026, 10, 19, 23, 15)

    private fun dto(code: Int = 2, rain: List<Pair<Int, Int>> = emptyList()) = WeatherDto(
        city = "Oslo",
        time = now,
        temperatureC = 6.6,
        weatherCode = code,
        isDay = false,
        highC = 9.2,
        lowC = 3.8,
        hourlyPrecipitation = rain.map { (hoursAhead, probability) ->
            HourlyPrecipitationDto(now.withMinute(0).plusHours(hoursAhead.toLong()), probability)
        },
    )

    @Test
    fun `maps WMO codes to conditions`() {
        val expected = mapOf(
            0 to WeatherCondition.Clear,
            2 to WeatherCondition.PartlyCloudy,
            3 to WeatherCondition.Cloudy,
            45 to WeatherCondition.Fog,
            53 to WeatherCondition.Drizzle,
            63 to WeatherCondition.Rain,
            81 to WeatherCondition.Rain,
            75 to WeatherCondition.Snow,
            86 to WeatherCondition.Snow,
            95 to WeatherCondition.Thunderstorm,
        )
        expected.forEach { (code, condition) -> assertEquals("code $code", condition, dto(code).toDomain().condition) }
    }

    @Test
    fun `rain time is the first likely hour after now within twelve hours`() {
        val weather = dto(rain = listOf(0 to 90, 2 to 30, 4 to 60, 5 to 80)).toDomain()
        assertEquals(LocalTime.of(3, 0), weather.rainExpectedAt)
    }

    @Test
    fun `no rain time when it is already wet or rain is too far away`() {
        assertNull(dto(code = 63, rain = listOf(2 to 90)).toDomain().rainExpectedAt)
        assertNull(dto(rain = listOf(13 to 90)).toDomain().rainExpectedAt)
    }

    @Test
    fun `daily forecast maps conditions temperatures and valid rain probabilities`() {
        val weather = dto().copy(
            dailyForecast = listOf(
                DailyForecastDto(now.toLocalDate().plusDays(1), 63, 10.5, 4.0, 75),
                DailyForecastDto(now.toLocalDate().plusDays(2), null, null, null, 120),
            ),
        ).toDomain()

        assertEquals(2, weather.dailyForecast.size)
        val first = weather.dailyForecast.first()
        assertEquals(WeatherCondition.Rain, first.condition)
        assertEquals(10.5, first.highC)
        assertEquals(4.0, first.lowC)
        assertEquals(75, first.maxPrecipitationProbability)
        val missing = weather.dailyForecast.last()
        assertNull(missing.condition)
        assertNull(missing.highC)
        assertNull(missing.lowC)
        assertNull(missing.maxPrecipitationProbability)
    }
}
