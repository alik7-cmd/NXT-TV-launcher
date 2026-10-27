package com.techascent.tv_launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.AppStyle
import com.techascent.tv_launcher.domain.model.AppOrganization
import com.techascent.tv_launcher.domain.model.AppSortOrder
import com.techascent.tv_launcher.domain.repository.AppOrganizationRepository
import com.techascent.tv_launcher.domain.usecase.ArrangeLauncherApps
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
    private val appOrganizationRepository: AppOrganizationRepository,
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
    private var launchHistory: List<String> = emptyList()
    private var organization = AppOrganization()
    private val arrangeApps = ArrangeLauncherApps()

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
                launchHistory = packages
                rebuildSections()
            }
        }
        viewModelScope.launch {
            appOrganizationRepository.observeOrganization().collect { settings ->
                organization = settings
                _uiState.update { it.copy(pinnedPackages = settings.pinnedPackages, sortOrder = settings.sortOrder) }
                rebuildSections()
            }
        }
    }

    private suspend fun observeApps() {
        observeLauncherApps().collect { result ->
            when (result) {
                LauncherAppsResult.Init -> _uiState.update {
                    it.copy(allApps = UiState.Loading)
                }
                is LauncherAppsResult.Success -> onAppsChanged(result)
                is LauncherAppsResult.Error -> {
                    val previous = result.previous
                    if (previous != null) onAppsChanged(previous)
                    else _uiState.update {
                        it.copy(allApps = UiState.Error(R.string.apps_load_error))
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
        rebuildSections()
    }

    private fun rebuildSections() {
        val apps = installedApps ?: return
        val sections = arrangeApps(apps, launchHistory, organization)
        val byPackage = mappedApps.associateBy { it.packageName }
        fun List<InstalledApp>.toUi() = map { byPackage.getValue(it.packageName) }
        _uiState.update { current ->
            current.copy(
                allApps = UiState.Success(sections.all.toUi()),
                pinnedApps = sections.pinned.toUi(),
                hiddenApps = sections.hidden.toUi(),
                // Resolve against the new list so the popup closes if its app was uninstalled.
                appOptions = current.appOptions?.let { open ->
                    byPackage[open.packageName]?.takeUnless { it.packageName in organization.hiddenPackages }
                },
            )
        }
    }

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

    fun onPinSelected(app: LauncherAppUiState) {
        onAppOptionsDismissed()
        viewModelScope.launch { appOrganizationRepository.togglePin(app.packageName) }
    }

    fun onHideSelected(app: LauncherAppUiState) {
        onAppOptionsDismissed()
        viewModelScope.launch { appOrganizationRepository.setHidden(app.packageName, true) }
    }

    fun onRestoreAppSelected(app: LauncherAppUiState) {
        viewModelScope.launch { appOrganizationRepository.setHidden(app.packageName, false) }
    }

    fun onSortOrderSelected(order: AppSortOrder) {
        viewModelScope.launch { appOrganizationRepository.setSortOrder(order) }
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

/** Emits the current time now and at the start of every minute. */
private fun minuteTicker(): Flow<ZonedDateTime> = flow {
    while (true) {
        val now = ZonedDateTime.now()
        emit(now)
        delay((60_000L - now.toInstant().toEpochMilli() % 60_000L).milliseconds)
    }
}
