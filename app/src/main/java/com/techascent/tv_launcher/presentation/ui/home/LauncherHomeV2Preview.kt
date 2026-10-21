package com.techascent.tv_launcher.presentation.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.techascent.tv_launcher.domain.model.WeatherCondition
import com.techascent.tv_launcher.presentation.state.UiState
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.LauncherHeaderUiState
import com.techascent.tv_launcher.presentation.state.LauncherUiState
import com.techascent.tv_launcher.presentation.state.WeatherUiState
import com.techascent.tv_launcher.presentation.state.DailyForecastUiState
import com.techascent.tv_launcher.presentation.ui.common.launcherBackground
import com.techascent.tv_launcher.presentation.ui.common.LauncherBackgroundPalette
import com.techascent.tv_launcher.presentation.ui.common.LauncherBackgroundPreviewProvider
import com.techascent.tv_launcher.presentation.ui.toolbar.WeatherChipV2
import com.techascent.tv_launcher.presentation.ui.weather.WeatherArtV2
import com.techascent.tv_launcher.presentation.ui.weather.weatherSection
import com.techascent.tv_launcher.presentation.ui.weather.toArtSpec
import com.techascent.tv_launcher.ui.theme.TVLauncherTheme
import java.time.ZonedDateTime
import java.time.LocalDate

private val apps = listOf(
    LauncherAppUiState("example.netflix", "", "Netflix", null, isFavorite = true),
    LauncherAppUiState("example.youtube", "", "YouTube", null, isFavorite = true),
    LauncherAppUiState("example.disney", "", "Disney+", null),
    LauncherAppUiState("example.plex", "", "Plex", null),
    LauncherAppUiState("example.settings", "", "Settings", null),
)

@Preview(name = "V2 launcher - weather night", showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun LauncherHomeV2Preview(
    @PreviewParameter(LauncherBackgroundPreviewProvider::class) palette: LauncherBackgroundPalette,
) {
    TVLauncherTheme {
        Box(Modifier.fillMaxSize().launcherBackground(palette)) {
            LauncherHomeV2(
                uiState = LauncherUiState(
                    header = UiState.Success(
                        LauncherHeaderUiState.from(ZonedDateTime.parse("2026-10-19T23:28:00+02:00[Europe/Oslo]")),
                    ),
                    favorites = UiState.Success(apps.filter { it.isFavorite }),
                    allApps = UiState.Success(apps),
                ),
                weatherState = previewWeather(WeatherCondition.Drizzle, isNight = true),
                onAppClick = {},
                onAppLongClick = {},
                onFavoriteLongClick = {},
                onAddFavoriteClick = {},
                onRemoveFavorite = {},
                onUninstall = {},
                onOptionsDismiss = {},
                onInputClick = {},
                onSettingsClick = {},
            )
        }
    }
}

@Preview(name = "V2 weather - all conditions", widthDp = 960, heightDp = 540)
@Composable
private fun WeatherV2ConditionsPreview() {
    TVLauncherTheme {
        Box(Modifier.fillMaxSize().launcherBackground()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WeatherCondition.entries.chunked(4).forEachIndexed { row, conditions ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        conditions.forEach { condition ->
                            val weather = previewWeather(condition, isNight = row == 0)
                            Column {
                                WeatherArtV2(weather.toArtSpec(), Modifier.size(220.dp, 140.dp))
                                WeatherChipV2(weather, expanded = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "V2 weather chip", showBackground = true, widthDp = 480, heightDp = 120)
@Composable
private fun WeatherChipV2Preview() {
    TVLauncherTheme {
        Box(Modifier.fillMaxSize().launcherBackground().padding(16.dp)) {
            WeatherChipV2(previewWeather(WeatherCondition.Rain, isNight = false), expanded = true)
        }

    }
}

@Preview(name = "Five-day weather forecast", showBackground = true, widthDp = 960, heightDp = 320)
@Composable
private fun WeatherForecastPreview() {
    TVLauncherTheme {
        Box(Modifier.fillMaxSize().launcherBackground().padding(24.dp)) {
            LazyColumn {
                weatherSection(previewWeather(WeatherCondition.PartlyCloudy, isNight = false))
            }
        }
    }
}

private fun previewWeather(condition: WeatherCondition, isNight: Boolean) = WeatherUiState.Content(
    temperature = 7,
    condition = condition,
    isNight = isNight,
    conditionResId = com.techascent.tv_launcher.R.string.weather_partly_cloudy,
    city = "Oslo",
    high = 9,
    low = 4,
    rainExpectedAt = java.time.LocalTime.of(3, 0),
    nextDays = List(5) { index ->
        DailyForecastUiState(
            date = LocalDate.of(2026, 10, 23).plusDays(index.toLong()),
            condition = listOf(
                WeatherCondition.Clear, WeatherCondition.PartlyCloudy, WeatherCondition.Rain,
                WeatherCondition.Cloudy, WeatherCondition.Clear,
            )[index],
            high = 9 + index,
            low = 3 + index,
            maxPrecipitationProbability = if (index == 2) 80 else 10,
        )
    },
)
