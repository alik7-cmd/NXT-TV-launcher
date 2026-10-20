package com.techascent.tv_launcher.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.WeatherUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/** Weather pill for the collapsed header bar. Display only, so it never takes D-pad focus. */
@Composable
fun CompactWeather(state: WeatherUiState.Content, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(LauncherSpacing.ButtonSize)
            .background(LauncherColors.ButtonBackground, RoundedCornerShape(LauncherSpacing.ButtonCornerRadius))
            .padding(horizontal = LauncherSpacing.ButtonHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeatherIcon(state.condition, state.isNight, Modifier.size(24.dp))
        Text(
            text = stringResource(R.string.weather_temperature, state.temperature),
            color = LauncherColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
        )
        state.city?.let { Text(text = it, color = LauncherColors.TextSecondary, fontSize = 14.sp) }
    }
}
