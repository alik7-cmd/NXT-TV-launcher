package com.techascent.tv_launcher.presentation.state

import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherHeaderUiStateTest {
    @Test
    fun `header retains the current date and time`() {
        val now = ZonedDateTime.parse("2026-10-19T09:00:00+02:00[Europe/Oslo]")
        assertEquals(now, LauncherHeaderUiState.from(now).dateTime)
    }
}
