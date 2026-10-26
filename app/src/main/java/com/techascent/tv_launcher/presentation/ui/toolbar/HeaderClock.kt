package com.techascent.tv_launcher.presentation.ui.toolbar

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Clock pieces formatted for the device locale and its 12/24-hour setting. */
@Immutable
data class ClockText(val time: String, val period: String?, val date: String)

@Composable
fun rememberClockText(dateTime: ZonedDateTime): ClockText {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val locale = Locale.getDefault()
    return remember(dateTime, is24Hour, locale) {
        val timeSkeleton = if (is24Hour) "Hm" else "hm"
        // The AM/PM marker is shown separately in a smaller size.
        val timePattern = DateFormat.getBestDateTimePattern(locale, timeSkeleton).replace("a", "").trim()
        ClockText(
            time = dateTime.format(DateTimeFormatter.ofPattern(timePattern, locale)),
            period = if (is24Hour) null else dateTime.format(DateTimeFormatter.ofPattern("a", locale)),
            date = dateTime.format(
                DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEEMMMMd"), locale),
            ),
        )
    }
}
