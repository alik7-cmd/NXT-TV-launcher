package com.techascent.tv_launcher.presentation.state

import androidx.annotation.StringRes
import com.techascent.tv_launcher.R
import java.time.ZonedDateTime

data class LauncherHeaderUiState(
    val dateTime: ZonedDateTime,
    @param:StringRes val greetingResId: Int,
) {
    companion object {
        fun from(now: ZonedDateTime) = LauncherHeaderUiState(
            dateTime = now,
            greetingResId = when (now.hour) {
                in 5..11 -> R.string.greeting_morning
                in 12..17 -> R.string.greeting_afternoon
                in 18..21 -> R.string.greeting_evening
                else -> R.string.greeting_night
            },
        )
    }
}
