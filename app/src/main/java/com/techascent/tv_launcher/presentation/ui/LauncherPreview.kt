package com.techascent.tv_launcher.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import com.techascent.tv_launcher.ui.theme.TVLauncherTheme
import com.techascent.tv_launcher.presentation.LauncherAppUiState
import com.techascent.tv_launcher.presentation.LauncherUiState
import com.techascent.tv_launcher.presentation.AppsUiState
import com.techascent.tv_launcher.presentation.LauncherHeaderUiState
import com.techascent.tv_launcher.presentation.WeatherUiState
import com.techascent.tv_launcher.domain.model.WeatherCondition
import java.time.ZonedDateTime

private val previewApps = listOf(
    LauncherAppUiState("example.netflix", "", "Netflix", null, isFavorite = true),
    LauncherAppUiState("example.youtube", "", "YouTube", null, isFavorite = true),
    LauncherAppUiState("example.disney", "", "Disney+", null),
    LauncherAppUiState("example.plex", "", "Plex", null),
    LauncherAppUiState("example.settings", "", "Settings", null),
)

@Preview(
    name = "TV Launcher - favourites",
    showBackground = true,
    widthDp = 960,
    heightDp = 540,
)
@Composable
private fun LauncherHomePreview() {
    TVLauncherTheme {
        Box(modifier = Modifier.fillMaxSize().launcherBackground()) {
            LauncherHome(
                headerState = LauncherHeaderUiState.from(ZonedDateTime.parse("2026-10-19T15:00:00+02:00[Europe/Oslo]")),
                uiState = LauncherUiState(
                    appsState = AppsUiState.Content(
                        apps = previewApps,
                        favorites = previewApps.filter { it.isFavorite },
                    ),
                ),
                onAppClick = {},
                onAppLongClick = {},
                onInputClick = {},
                onSettingsClick = {},
            )
        }
    }
}

@Preview(
    name = "TV Launcher - weather night",
    showBackground = true,
    widthDp = 960,
    heightDp = 540,
)
@Composable
private fun LauncherHomeWeatherPreview() {
    TVLauncherTheme {
        Box(modifier = Modifier.fillMaxSize().launcherBackground()) {
            LauncherHome(
                headerState = LauncherHeaderUiState.from(ZonedDateTime.parse("2026-10-19T23:28:00+02:00[Europe/Oslo]")),
                weatherState = previewWeather(WeatherCondition.PartlyCloudy, isNight = true),
                uiState = LauncherUiState(
                    appsState = AppsUiState.Content(
                        apps = previewApps,
                        favorites = previewApps.filter { it.isFavorite },
                    ),
                ),
                onAppClick = {},
                onAppLongClick = {},
                onInputClick = {},
                onSettingsClick = {},
            )
        }
    }
}

internal fun previewWeather(condition: WeatherCondition, isNight: Boolean) = WeatherUiState.Content(
    temperature = 7,
    condition = condition,
    isNight = isNight,
    conditionResId = com.techascent.tv_launcher.R.string.weather_partly_cloudy,
    city = "Oslo",
    high = 9,
    low = 4,
    rainExpectedAt = java.time.LocalTime.of(3, 0),
)
