package com.techascent.tv_launcher.presentation.ui.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.WeatherUiState
import com.techascent.tv_launcher.presentation.ui.weather.WeatherIcon
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/** Display-only weather summary: icon, temperature and, when expanded, condition and city. */
@Composable
fun WeatherChipV2(state: WeatherUiState.Content, expanded: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeatherIcon(state.condition, state.isNight, Modifier.size(if (expanded) 40.dp else 28.dp))
        Text(
            text = stringResource(R.string.weather_temperature, state.temperature),
            color = LauncherColors.TextPrimary,
            fontSize = if (expanded) 32.sp else 22.sp,
            fontWeight = FontWeight.Light,
        )
        if (expanded) {
            Column {
                Text(stringResource(state.conditionResId), color = LauncherColors.TextPrimary, fontSize = 16.sp)
                Text(rememberDetails(state), color = LauncherColors.TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

/** City, high/low and, when rain is forecast, the time it starts, in the device's 12/24 h style. */
@Composable
private fun rememberDetails(state: WeatherUiState.Content): String {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val rainTime = remember(state.rainExpectedAt, is24Hour) {
        state.rainExpectedAt?.let {
            val locale = Locale.getDefault()
            it.format(DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, if (is24Hour) "Hm" else "ha"), locale))
        }
    }
    return listOfNotNull(
        state.city,
        stringResource(R.string.weather_high_low, state.high, state.low),
        rainTime?.let { stringResource(R.string.weather_rain_at, it) },
    ).joinToString("  ·  ")
}
