package com.techascent.tv_launcher.presentation.state

import com.techascent.tv_launcher.domain.model.DailyForecast
import com.techascent.tv_launcher.domain.model.Weather
import com.techascent.tv_launcher.domain.model.WeatherCondition
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyForecastUiStateTest {
    private val today = LocalDate.of(2026, 10, 22)
    private val weather = Weather(
        city = "Oslo",
        temperatureC = 7.0,
        condition = WeatherCondition.Cloudy,
        isDay = true,
        highC = 10.0,
        lowC = 3.0,
        rainExpectedAt = null,
        timezone = ZoneId.of("Europe/Oslo"),
    )

    @Test
    fun `shows exactly five future days in order excluding today and old dates`() {
        val forecast = (-1L..7L).reversed().map { day ->
            DailyForecast(today.plusDays(day), WeatherCondition.Rain, 9.6, 3.2, 60)
        }
        val result = WeatherUiState.from(weather.copy(dailyForecast = forecast), today = today)

        assertEquals((1L..5L).map(today::plusDays), result.nextDays.map { it.date })
        assertEquals(10, result.nextDays.first().high)
        assertEquals(3, result.nextDays.first().low)
        assertEquals(60, result.nextDays.first().maxPrecipitationProbability)
    }

    @Test
    fun `duplicate dates are not shown twice and missing values remain missing`() {
        val day = DailyForecast(today.plusDays(1), null, null, null, null)
        val result = WeatherUiState.from(weather.copy(dailyForecast = listOf(day, day)), today = today)

        assertEquals(1, result.nextDays.size)
        assertNull(result.nextDays.single().condition)
        assertNull(result.nextDays.single().high)
        assertNull(result.nextDays.single().low)
        assertNull(result.nextDays.single().maxPrecipitationProbability)
    }

    @Test
    fun `old cached forecasts are not presented as upcoming weather`() {
        val old = DailyForecast(today.minusDays(1), WeatherCondition.Clear, 10.0, 3.0, 0)
        assertEquals(emptyList<DailyForecastUiState>(), WeatherUiState.from(
            weather.copy(dailyForecast = listOf(old)), today = today,
        ).nextDays)
    }

    @Test
    fun `today is determined in the forecast location timezone`() {
        val zone = ZoneId.of("Pacific/Kiritimati")
        val localToday = LocalDate.now(zone)
        val forecast = listOf(
            DailyForecast(localToday, WeatherCondition.Clear, 10.0, 3.0, 0),
            DailyForecast(localToday.plusDays(1), WeatherCondition.Clear, 10.0, 3.0, 0),
        )
        val result = WeatherUiState.from(weather.copy(timezone = zone, dailyForecast = forecast))
        assertEquals(listOf(localToday.plusDays(1)), result.nextDays.map { it.date })
    }
}
