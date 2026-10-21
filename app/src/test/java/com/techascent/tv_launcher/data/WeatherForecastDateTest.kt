package com.techascent.tv_launcher.data

import com.techascent.tv_launcher.data.remote.dailyForecastDate
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherForecastDateTest {
    @Test
    fun `daily dates keep response offset across autumn DST instead of duplicating Sunday`() {
        val sunday = Instant.parse("2026-10-24T22:00:00Z").epochSecond
        assertEquals(LocalDate.of(2026, 10, 25), dailyForecastDate(sunday, 7200))
        assertEquals(LocalDate.of(2026, 10, 26), dailyForecastDate(sunday + 86400, 7200))
    }

    @Test
    fun `daily dates keep response offset across spring DST and negative timezone offsets`() {
        val sunday = Instant.parse("2026-03-28T23:00:00Z").epochSecond
        assertEquals(LocalDate.of(2026, 3, 29), dailyForecastDate(sunday, 3600))
        assertEquals(LocalDate.of(2026, 3, 30), dailyForecastDate(sunday + 86400, 3600))
        val midnight = Instant.parse("2026-10-21T07:00:00Z").epochSecond
        assertEquals(LocalDate.of(2026, 10, 21), dailyForecastDate(midnight, -7 * 3600))
    }
}
