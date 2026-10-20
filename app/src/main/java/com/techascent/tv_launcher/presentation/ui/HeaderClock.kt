package com.techascent.tv_launcher.presentation.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
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

/** Accent greeting, large light clock with a small AM/PM marker, and the date. */
@Composable
fun ExpandedClock(greeting: String, clock: ClockText, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = greeting.uppercase(Locale.getDefault()),
            color = LauncherColors.Accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(LauncherSpacing.ExtraSmall))
        Row(horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small)) {
            Text(
                text = clock.time,
                color = LauncherColors.TextPrimary,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.alignByBaseline(),
            )
            clock.period?.let {
                Text(
                    text = it,
                    color = LauncherColors.TextSecondary,
                    fontSize = 24.sp,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
        Text(text = clock.date, color = LauncherColors.TextSecondary, fontSize = 18.sp)
    }
}

/** Single-line clock for the collapsed header bar. */
@Composable
fun CompactClock(clock: ClockText, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small)) {
        Text(
            text = clock.time,
            color = LauncherColors.TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.alignByBaseline(),
        )
        clock.period?.let {
            Text(it, color = LauncherColors.TextSecondary, fontSize = 14.sp, modifier = Modifier.alignByBaseline())
        }
        Text("•", color = LauncherColors.TextTertiary, fontSize = 16.sp, modifier = Modifier.alignByBaseline())
        Text(clock.date, color = LauncherColors.TextSecondary, fontSize = 16.sp, modifier = Modifier.alignByBaseline())
    }
}
