package com.techascent.tv_launcher.presentation.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.WeatherUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Large temperature, condition and a details line, shown over the weather art. */
@Composable
fun WeatherReadout(state: WeatherUiState.Content, modifier: Modifier = Modifier) {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val rainTime = remember(state.rainExpectedAt, is24Hour) {
        state.rainExpectedAt?.let {
            val locale = Locale.getDefault()
            it.format(DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, if (is24Hour) "Hm" else "ha"), locale))
        }
    }
    val details = listOfNotNull(
        state.city,
        stringResource(R.string.weather_high_low, state.high, state.low),
        rainTime?.let { stringResource(R.string.weather_rain_at, it) },
    ).joinToString(DETAIL_SEPARATOR)
    // A soft shadow keeps the text readable on top of bright skies and clouds.
    val shadow = TextStyle(shadow = Shadow(Color(0xCC0B111D), Offset(0f, 3f), blurRadius = 14f))

    Column(modifier, horizontalAlignment = Alignment.End) {
        Text(
            text = stringResource(R.string.weather_temperature, state.temperature),
            color = LauncherColors.TextPrimary,
            fontSize = 60.sp,
            fontWeight = FontWeight.ExtraLight,
            style = shadow,
        )
        Text(
            text = stringResource(state.conditionResId),
            color = LauncherColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            style = shadow,
        )
        Text(text = details, color = LauncherColors.TextSecondary, fontSize = 14.sp, style = shadow)
    }
}

private const val DETAIL_SEPARATOR = "  ·  "
