package com.techascent.tv_launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techascent.tv_launcher.LauncherConfig
import com.techascent.tv_launcher.domain.usecase.ObserveWeather
import com.techascent.tv_launcher.presentation.state.WeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class WeatherViewModel(
    observeWeather: ObserveWeather,
    config: LauncherConfig = LauncherConfig(),
) : ViewModel() {
    val uiState: StateFlow<WeatherUiState> = if (config.useWeatherHero) {
        observeWeather()
            .map<_, WeatherUiState> {
                WeatherUiState.from(
                    it,
                    animateArt = config.animateWeatherArt,
                    showTemperatureChart = config.showWeatherTemperatureChart,
                )
            }
            .catch { emit(WeatherUiState.Hidden) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WeatherUiState.Loading)
    } else {
        MutableStateFlow(WeatherUiState.Hidden)
    }

    val forecastState: StateFlow<WeatherUiState> = if (config.showWeatherForecastSection) {
        uiState
    } else {
        MutableStateFlow(WeatherUiState.Hidden)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
