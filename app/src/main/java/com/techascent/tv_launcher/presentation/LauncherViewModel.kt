package com.techascent.tv_launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.LauncherAppsResult
import com.techascent.tv_launcher.domain.model.InstalledApp
import com.techascent.tv_launcher.domain.usecase.ObserveLauncherApps
import com.techascent.tv_launcher.domain.usecase.ToggleFavoriteApp
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
    private val toggleFavoriteApp: ToggleFavoriteApp,
    private val clock: Flow<ZonedDateTime> = minuteTicker(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LauncherUiState(header = UiState.Success(LauncherHeaderUiState.from(ZonedDateTime.now()))),
    )
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LauncherEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /** Latest installed apps with favourite flags, empty until the first list arrives. */
    private var apps: List<LauncherAppUiState> = emptyList()
    private var installedApps: List<InstalledApp>? = null
    private var mappedApps: List<LauncherAppUiState> = emptyList()

    init {
        viewModelScope.launch {
            clock.collect { now -> _uiState.update { it.copy(header = UiState.Success(LauncherHeaderUiState.from(now))) } }
        }
        viewModelScope.launch { observeApps() }
    }

    private suspend fun observeApps() {
        observeLauncherApps().collect { result ->
            when (result) {
                LauncherAppsResult.Init -> _uiState.update {
                    it.copy(allApps = UiState.Loading, favorites = UiState.Loading)
                }
                is LauncherAppsResult.Success -> onAppsChanged(result)
                is LauncherAppsResult.Error -> {
                    val previous = result.previous
                    if (previous != null) onAppsChanged(previous)
                    else _uiState.update {
                        val error = UiState.Error(R.string.apps_load_error)
                        it.copy(allApps = error, favorites = error)
                    }
                }
            }
        }
    }

    private fun onAppsChanged(result: LauncherAppsResult.Success) {
        // Wrap icons only when the installed list changes, not on favourite changes.
        if (installedApps !== result.apps) {
            installedApps = result.apps
            mappedApps = result.apps.map(LauncherAppUiState::from)
        }
        apps = mappedApps.map { it.copy(isFavorite = it.packageName in result.favoritePackages) }
        _uiState.update { current ->
            current.copy(
                allApps = UiState.Success(apps),
                favorites = UiState.Success(apps.filter { it.isFavorite }),
                // Resolve against the new list so the popup closes if its app was uninstalled.
                appOptions = current.appOptions?.let { open -> apps.firstOrNull { it.packageName == open.packageName } },
                favoritePickerApps = current.favoritePickerApps?.let { pickerApps() },
            )
        }
    }

    private fun pickerApps() = apps.filterNot { it.isFavorite }

    fun onAppSelected(app: LauncherAppUiState) {
        onAppOptionsDismissed()
        sendEffect(LauncherEffect.LaunchApp(app.packageName, app.activityName))
    }

    fun onAppLongPressed(app: LauncherAppUiState) {
        _uiState.update { it.copy(appOptions = app, favoriteOptions = false) }
    }

    fun onFavoriteLongPressed(app: LauncherAppUiState) {
        _uiState.update { it.copy(appOptions = app, favoriteOptions = true) }
    }

    fun onAppOptionsDismissed() {
        _uiState.update { it.copy(appOptions = null) }
    }

    fun onAddFavoriteSelected() {
        _uiState.update { it.copy(favoritePickerApps = pickerApps()) }
    }

    fun onFavoritePickerDismissed() {
        _uiState.update { it.copy(favoritePickerApps = null) }
    }

    fun onFavoriteAppPicked(app: LauncherAppUiState) {
        val available = _uiState.value.favoritePickerApps.orEmpty()
        if (available.none { it.packageName == app.packageName }) return
        onFavoritePickerDismissed()
        viewModelScope.launch { toggleFavoriteApp(app.packageName) }
    }

    fun onToggleFavoriteSelected(app: LauncherAppUiState) {
        onAppOptionsDismissed()
        viewModelScope.launch { toggleFavoriteApp(app.packageName) }
    }

    fun onUninstallSelected(app: LauncherAppUiState) {
        onAppOptionsDismissed()
        if (app.isRemovable) sendEffect(LauncherEffect.UninstallApp(app.packageName))
    }

    fun onInputSelected() = sendEffect(LauncherEffect.OpenInputs)

    fun onSettingsSelected() = sendEffect(LauncherEffect.OpenSettings)

    fun onPlayStoreSelected() = sendEffect(LauncherEffect.OpenPlayStore)

    private fun sendEffect(effect: LauncherEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

/** Emits the current time now and at the start of every minute. */
private fun minuteTicker(): Flow<ZonedDateTime> = flow {
    while (true) {
        val now = ZonedDateTime.now()
        emit(now)
        delay((60_000L - now.toInstant().toEpochMilli() % 60_000L).milliseconds)
    }
}
