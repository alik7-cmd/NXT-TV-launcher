package com.techascent.tv_launcher.presentation.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.DailyForecastUiState
import com.techascent.tv_launcher.presentation.state.WeatherUiState
import com.techascent.tv_launcher.presentation.state.descriptionResId
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
import java.time.format.DateTimeFormatter

internal fun LazyListScope.weatherSection(
    state: WeatherUiState
){
    when(state){
        WeatherUiState.Loading -> item { WeatherLoading() }
        is WeatherUiState.Hidden -> {}
        is WeatherUiState.Content -> item {
            WeatherForecastPanel(
                weather = state,
            )
        }
    }

}

@Composable
private fun WeatherForecastPanel(
    weather: WeatherUiState.Content,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LauncherColors.DialogBackground.copy(alpha = 0.25f), shape)
            .border(1.dp, LauncherColors.StorePanelBorder.copy(alpha = 0.25f), shape)
            .padding(horizontal = LauncherSpacing.Large, vertical = LauncherSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
    ) {
        if (weather.showTemperatureChart) {
            ForecastTemperatureChart(weather.nextDays)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
        ) {
            weather.nextDays.forEach { day ->
                ForecastDay(day, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ForecastDay(day: DailyForecastUiState, modifier: Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val weekday = remember(locale) { DateTimeFormatter.ofPattern("EEE", locale) }
    val date = remember(locale) { DateTimeFormatter.ofPattern("MMM d", locale) }
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.ExtraSmall),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.ExtraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(day.date.format(weekday), color = LauncherColors.TextPrimary.copy(alpha = 0.75f), fontSize = 14.sp)
            Text(day.date.format(date), color = LauncherColors.TextTertiary.copy(alpha = 0.65f), fontSize = 12.sp)
        }
        val condition = day.condition
        if (condition != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.ExtraSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WeatherIcon(condition, isNight = false, Modifier.size(24.dp))
                Text(
                    stringResource(condition.descriptionResId()),
                    color = LauncherColors.TextSecondary.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Text(
                stringResource(R.string.weather_unavailable_value),
                color = LauncherColors.TextSecondary.copy(alpha = 0.65f),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                day.high?.let { stringResource(R.string.weather_temperature, it) }
                    ?: stringResource(R.string.weather_unavailable_value),
                color = LauncherColors.TextPrimary.copy(alpha = 0.75f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                day.low?.let { stringResource(R.string.weather_temperature, it) }
                    ?: stringResource(R.string.weather_unavailable_value),
                color = LauncherColors.TextSecondary.copy(alpha = 0.65f),
                fontSize = 14.sp,
            )
        }
        day.maxPrecipitationProbability?.let {
            Text(
                stringResource(R.string.weather_rain_chance, it),
                color = LauncherColors.Accent.copy(alpha = 0.65f),
                fontSize = 12.sp,
            )
        }
    }
}
