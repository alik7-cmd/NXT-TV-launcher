package com.techascent.tv_launcher.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.domain.model.WeatherCondition
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.TVLauncherTheme

@Preview(name = "Weather art - all conditions", widthDp = 960, heightDp = 540)
@Composable
private fun WeatherArtConditionsPreview() {
    TVLauncherTheme {
        Box(Modifier.fillMaxSize().launcherBackground()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WeatherCondition.entries.chunked(4).forEachIndexed { row, conditions ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        conditions.forEach { condition ->
                            val night = row == 0 && condition == WeatherCondition.PartlyCloudy
                            Column {
                                WeatherArt(condition, isNight = night, modifier = Modifier.size(220.dp, 140.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    WeatherIcon(condition, isNight = night, modifier = Modifier.size(20.dp))
                                    Text(condition.name, color = LauncherColors.TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Compact weather pill", widthDp = 240, heightDp = 64, showBackground = true, backgroundColor = 0xFF172237)
@Composable
private fun CompactWeatherPreview() {
    TVLauncherTheme {
        Box(Modifier.padding(8.dp)) {
            CompactWeather(previewWeather(WeatherCondition.Rain, isNight = false))
        }
    }
}
