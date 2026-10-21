package com.techascent.tv_launcher.presentation.state

import java.time.ZonedDateTime

data class LauncherHeaderUiState(
    val dateTime: ZonedDateTime,
) {
    companion object {
        fun from(now: ZonedDateTime) = LauncherHeaderUiState(dateTime = now)
    }
}
