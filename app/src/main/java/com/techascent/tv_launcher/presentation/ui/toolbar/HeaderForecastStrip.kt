package com.techascent.tv_launcher.presentation.ui.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.DailyForecastUiState
import com.techascent.tv_launcher.presentation.ui.weather.WeatherIcon
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
import java.time.format.DateTimeFormatter

@Composable
internal fun HeaderForecastStrip(days: List<DailyForecastUiState>) {
    val locale = LocalConfiguration.current.locales[0]
    val weekday = remember(locale) { DateTimeFormatter.ofPattern("EEE", locale) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.ExtraSmall),
    ) {
        days.take(5).forEach { day ->
            Column(
                modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {},
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(day.date.format(weekday), color = LauncherColors.TextSecondary, fontSize = 11.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    day.condition?.let { WeatherIcon(it, isNight = false, Modifier.size(16.dp)) }
                    Text(
                        day.high?.let { stringResource(R.string.weather_temperature, it) }
                            ?: stringResource(R.string.weather_unavailable_value),
                        color = LauncherColors.TextPrimary.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                    Text(
                        day.low?.let { stringResource(R.string.weather_temperature, it) }
                            ?: stringResource(R.string.weather_unavailable_value),
                        color = LauncherColors.TextTertiary,
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
