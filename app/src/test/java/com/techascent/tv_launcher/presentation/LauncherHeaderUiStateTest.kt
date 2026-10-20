package com.techascent.tv_launcher.presentation

import com.techascent.tv_launcher.R
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherHeaderUiStateTest {
    @Test
    fun `greetings cover every hour including period boundaries`() {
        val expected = listOf(
            R.string.greeting_night to 0..4,
            R.string.greeting_morning to 5..11,
            R.string.greeting_afternoon to 12..17,
            R.string.greeting_evening to 18..21,
            R.string.greeting_night to 22..23,
        )
        val date = ZonedDateTime.parse("2026-10-19T00:00:00+02:00[Europe/Oslo]")
        expected.forEach { (greeting, hours) ->
            hours.forEach { hour ->
                val now = date.withHour(hour)
                val state = LauncherHeaderUiState.from(now)
                assertEquals("Greeting at $hour", greeting, state.greetingResId)
                assertEquals(now, state.dateTime)
            }
        }
    }
}
