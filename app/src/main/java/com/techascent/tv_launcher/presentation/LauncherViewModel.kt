package com.techascent.tv_launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.AppStyle
import com.techascent.tv_launcher.domain.model.LauncherWallpaper
import com.techascent.tv_launcher.domain.model.LauncherAppsResult
import com.techascent.tv_launcher.domain.repository.AppStyleRepository
import com.techascent.tv_launcher.domain.model.InstalledApp
import com.techascent.tv_launcher.domain.usecase.ObserveLauncherApps
import com.techascent.tv_launcher.domain.usecase.ObserveRecentApps
import com.techascent.tv_launcher.domain.usecase.RecordAppLaunch
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.LauncherHeaderUiState
import com.techascent.tv_launcher.presentation.state.LauncherUiState
import com.techascent.tv_launcher.presentation.state.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds

class LauncherViewModel(
    private val observeLauncherApps: ObserveLauncherApps,
    private val observeRecentApps: ObserveRecentApps,
    private val recordAppLaunch: RecordAppLaunch,
    private val appStyleRepository: AppStyleRepository,
    private val clock: Flow<ZonedDateTime> = minuteTicker(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LauncherUiState(header = UiState.Success(LauncherHeaderUiState.from(ZonedDateTime.now()))),
    )
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LauncherEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var installedApps: List<InstalledApp>? = null
    private var mappedApps: List<LauncherAppUiState> = emptyList()
    private var recentPackages: List<String> = emptyList()

    init {
        viewModelScope.launch {
            clock.collect { now -> _uiState.update { it.copy(header = UiState.Success(LauncherHeaderUiState.from(now))) } }
        }
        viewModelScope.launch { observeApps() }
        viewModelScope.launch {
            appStyleRepository.observeStyle().collect { style -> _uiState.update { it.copy(appStyle = style) } }
        }
        viewModelScope.launch {
            appStyleRepository.observeWallpaper().collect { wallpaper ->
                _uiState.update { it.copy(wallpaper = wallpaper) }
            }
        }
        viewModelScope.launch {
            appStyleRepository.observeShowAppNames().collect { show ->
                _uiState.update { it.copy(showAppNames = show) }
            }
        }
        viewModelScope.launch {
            observeRecentApps().collect { packages ->
                recentPackages = packages
                _uiState.update { if (it.allApps is UiState.Success) it.copy(recentApps = UiState.Success(recentApps())) else it }
            }
        }
    }

    private suspend fun observeApps() {
        observeLauncherApps().collect { result ->
            when (result) {
                LauncherAppsResult.Init -> _uiState.update {
                    it.copy(allApps = UiState.Loading, recentApps = UiState.Loading)
                }
                is LauncherAppsResult.Success -> onAppsChanged(result)
                is LauncherAppsResult.Error -> {
                    val previous = result.previous
                    if (previous != null) onAppsChanged(previous)
                    else _uiState.update {
                        val error = UiState.Error(R.string.apps_load_error)
                        it.copy(allApps = error, recentApps = error)
                    }
                }
            }
        }
    }

    private fun onAppsChanged(result: LauncherAppsResult.Success) {
        if (installedApps !== result.apps) {
            installedApps = result.apps
            mappedApps = result.apps.map(LauncherAppUiState::from)
        }
        _uiState.update { current ->
            current.copy(
                allApps = UiState.Success(mappedApps),
                recentApps = UiState.Success(recentApps()),
                // Resolve against the new list so the popup closes if its app was uninstalled.
                appOptions = current.appOptions?.let { open -> mappedApps.firstOrNull { it.packageName == open.packageName } },
            )
        }
    }

    private fun recentApps() = recentPackages
        .mapNotNull { packageName -> mappedApps.firstOrNull { it.packageName == packageName } }
        .take(RECENT_APPS_LIMIT)

    fun onAppSelected(app: LauncherAppUiState) {
        onAppOptionsDismissed()
        sendEffect(LauncherEffect.LaunchApp(app.packageName, app.activityName))
        viewModelScope.launch { recordAppLaunch(app.packageName) }
    }

    fun onAppLongPressed(app: LauncherAppUiState) {
        _uiState.update { it.copy(appOptions = app) }
    }

    fun onAppOptionsDismissed() {
        _uiState.update { it.copy(appOptions = null) }
    }

    fun onUninstallSelected(app: LauncherAppUiState) {
        onAppOptionsDismissed()
        if (app.isRemovable) sendEffect(LauncherEffect.UninstallApp(app.packageName))
    }

    fun onCustomizeSelected() {
        _uiState.update { it.copy(customizeOpen = true) }
    }

    fun onCustomizeDismissed() {
        _uiState.update { it.copy(customizeOpen = false) }
    }

    fun onAppStyleSelected(style: AppStyle) {
        viewModelScope.launch { appStyleRepository.setStyle(style) }
    }

    fun onShowAppNamesSelected(show: Boolean) {
        viewModelScope.launch { appStyleRepository.setShowAppNames(show) }
    }

    fun onWallpaperApplied(wallpaper: LauncherWallpaper) {
        onCustomizeDismissed()
        viewModelScope.launch { appStyleRepository.setWallpaper(wallpaper) }
    }


    fun onInputSelected() = sendEffect(LauncherEffect.OpenInputs)

    fun onSettingsSelected() = sendEffect(LauncherEffect.OpenSettings)

    private fun sendEffect(effect: LauncherEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

private const val RECENT_APPS_LIMIT = 5

/** Emits the current time now and at the start of every minute. */
private fun minuteTicker(): Flow<ZonedDateTime> = flow {
    while (true) {
        val now = ZonedDateTime.now()
        emit(now)
        delay((60_000L - now.toInstant().toEpochMilli() % 60_000L).milliseconds)
    }
}
