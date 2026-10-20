package com.techascent.tv_launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techascent.tv_launcher.domain.usecase.ObserveWeather
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class WeatherViewModel(
    observeWeather: ObserveWeather,
) : ViewModel() {
    // Refreshing pauses while another app is in front and resumes when the launcher returns.
    val uiState: StateFlow<WeatherUiState> = observeWeather()
        .map<_, WeatherUiState>(WeatherUiState::from)
        .catch { emit(WeatherUiState.Hidden) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WeatherUiState.Hidden)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
