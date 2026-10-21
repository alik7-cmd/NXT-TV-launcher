package com.techascent.tv_launcher.presentation.ui.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.LauncherHeaderUiState
import com.techascent.tv_launcher.presentation.state.WeatherUiState
import com.techascent.tv_launcher.presentation.ui.weather.WeatherLoading
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/**
 * Top row of the home list, like the stock launcher's: clock and weather on the left, Inputs and
 * Settings on the right. It is a normal list item, so it scrolls away with the content and nothing
 * floats, animates or re-measures the list.
 */

internal fun LazyListScope.headerSection(
    state: LauncherHeaderUiState,
    weather: WeatherUiState,
    onInputClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
){
    item(key = "launcher_header", contentType = "launcher_header") {
        LauncherHeader(
            state = state,
            weather = weather,
            onInputClick = onInputClick,
            onSettingsClick = onSettingsClick,
        )
    }
}
@Composable
fun LauncherHeader(
    state: LauncherHeaderUiState,
    weather: WeatherUiState,
    onInputClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clock = rememberClockText(state.dateTime)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClockV2(clock)
        /*if (weather is WeatherUiState.Content) {
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = LauncherSpacing.Medium),
                verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
                horizontalAlignment = Alignment.End,
            ) {
                WeatherChipV2(weather, expanded = true)
                if (weather.nextDays.isNotEmpty()) {
                    HeaderForecastStrip(weather.nextDays)
                }
            }
        }*/
        Row(
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (weather is WeatherUiState.Content) {
                WeatherChipV2(weather, expanded = true, Modifier.padding(end = LauncherSpacing.Medium))
            } else if (weather == WeatherUiState.Loading) {
                WeatherLoading(Modifier.padding(end = LauncherSpacing.Medium), compact = true)
            }
            DeviceActionButton(stringResource(R.string.inputs), R.drawable.ic_input, onInputClick)
            DeviceActionButton(stringResource(R.string.settings), R.drawable.ic_settings, onSettingsClick)
        }
    }
}

/** Large light clock with a small AM/PM marker and the date; no greeting line. */
@Composable
private fun ClockV2(clock: ClockText) {
    Column(modifier = Modifier.padding(top = LauncherSpacing.Medium)) {
        Row(horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small)) {
            Text(
                text = clock.time,
                color = LauncherColors.TextPrimary,
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.alignByBaseline(),
            )
            clock.period?.let {
                Text(it, color = LauncherColors.TextSecondary, fontSize = 22.sp, modifier = Modifier.alignByBaseline())
            }
        }
        Text(clock.date, color = LauncherColors.TextSecondary, fontSize = 18.sp)
    }
}
