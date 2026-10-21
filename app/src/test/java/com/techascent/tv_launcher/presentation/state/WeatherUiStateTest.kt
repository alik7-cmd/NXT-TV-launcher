package com.techascent.tv_launcher.presentation.state

import com.techascent.tv_launcher.domain.model.DailyForecast
import com.techascent.tv_launcher.domain.model.HourlyForecast
import com.techascent.tv_launcher.domain.model.Weather
import com.techascent.tv_launcher.domain.model.WeatherCondition
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherUiStateTest {
    private val zone = ZoneId.of("Europe/Oslo")
    private fun content() = WeatherUiState.from(
        Weather("Oslo", 7.0, WeatherCondition.Cloudy, false, 9.0, 3.0, null, timezone = zone),
    )

    @Test
    fun `next twelve hours cross location midnight and keep missing hours unavailable`() {
        val now = Instant.parse("2026-10-20T21:15:00Z")
        val midnight = Instant.parse("2026-10-20T22:00:00Z")
        val state = content().copy(hourlyForecast = listOf(
            HourlyForecast(now.minusSeconds(900), 99),
            HourlyForecast(midnight, 0),
            HourlyForecast(midnight.plusSeconds(7200), 70),
        ))
        val hours = state.nextHours(now)
        assertEquals(12, hours.size)
        assertEquals(midnight, hours.first().time)
        assertEquals(LocalDate.of(2026, 10, 21), hours.first().time.atZone(zone).toLocalDate())
        assertEquals(0, hours.first().probabilityPercent)
        assertNull(hours[1].probabilityPercent)
        assertEquals(70, hours[2].probabilityPercent)
        assertEquals(midnight.plusSeconds(11 * 3600), hours.last().time)
    }

    @Test
    fun `spring DST skips nonexistent local hour without losing a slot`() {
        val hours = content().nextHours(Instant.parse("2026-03-29T00:30:00Z"))
        assertEquals(12, hours.size)
        assertEquals(listOf(3, 4, 5), hours.take(3).map { it.time.atZone(zone).hour })
        assertEquals(3600L, hours[1].time.epochSecond - hours[0].time.epochSecond)
    }

    @Test
    fun `autumn DST retains both repeated hours as distinct instants`() {
        val hours = content().nextHours(Instant.parse("2026-10-24T23:30:00Z"))
        assertEquals(listOf(2, 2, 3), hours.take(3).map { it.time.atZone(zone).hour })
        assertEquals(listOf(7200, 3600), hours.take(2).map { it.time.atZone(zone).offset.totalSeconds })
        assertEquals(12, hours.map { it.time }.distinct().size)
    }

    @Test
    fun `daily columns start on forecast date not device or UTC date`() {
        val today = LocalDate.of(2026, 10, 21)
        val first = DailyForecast(today, WeatherCondition.Rain, 10.5, 3.5, 75)
        val state = content().copy(dailyForecast = listOf(first))
        val days = state.nextDays(Instant.parse("2026-10-20T22:15:00Z"))
        assertEquals(7, days.size)
        assertEquals(first, days.first())
        assertEquals(today.plusDays(6), days.last().date)
        assertNull(days[1].maxPrecipitationProbability)
        assertNull(days[1].condition)
    }

    @Test
    fun `stale forecasts have unavailable future slots rather than old data or zeros`() {
        val now = Instant.parse("2026-10-20T12:00:00Z")
        val state = content().copy(hourlyForecast = listOf(HourlyForecast(now.minusSeconds(3600), 0)))
        assertEquals(List<Int?>(12) { null }, state.nextHours(now).map { it.probabilityPercent })
    }
}
