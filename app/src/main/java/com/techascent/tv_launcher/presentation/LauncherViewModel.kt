package com.techascent.tv_launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.usecase.ObserveFavoriteApps
import com.techascent.tv_launcher.domain.usecase.ObserveInstalledApps
import com.techascent.tv_launcher.domain.usecase.ToggleFavoriteApp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds

class LauncherViewModel(
    observeInstalledApps: ObserveInstalledApps,
    observeFavoriteApps: ObserveFavoriteApps,
    private val toggleFavoriteApp: ToggleFavoriteApp,
    private val currentTime: () -> ZonedDateTime = { ZonedDateTime.now() },
) : ViewModel() {
    val headerState: StateFlow<LauncherHeaderUiState> = flow {
        while (true) {
            val now = currentTime()
            emit(LauncherHeaderUiState.from(now))
            // Recalculate on each minute boundary, including day-period transitions.
            delay((60_000L - now.toInstant().toEpochMilli() % 60_000L).milliseconds)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(0),
        LauncherHeaderUiState.from(currentTime()),
    )

    private data class AppSelection(val packageName: String, val fromFavorites: Boolean)
    private val selectedAppPackage = MutableStateFlow<AppSelection?>(null)
    private val favoritePickerOpen = MutableStateFlow(false)

    // Icons are wrapped once per app-list change, so toggling a favourite reuses the same bitmaps.
    private val appUiStates = observeInstalledApps()
        .map<InstalledAppsUpdate, AppsUiState> { update ->
            when (update) {
                InstalledAppsUpdate.Loading -> AppsUiState.Loading
                is InstalledAppsUpdate.Loaded -> {
                    if (update.apps.isEmpty()) AppsUiState.Empty
                    else AppsUiState.Content(update.apps.map(LauncherAppUiState::from))
                }
            }
        }
        .catch { emit(AppsUiState.Error(R.string.apps_load_error)) }
        .runningFold<AppsUiState, AppsUiState>(AppsUiState.Loading) { previous, next ->
            when {
                previous is AppsUiState.Content && next == AppsUiState.Loading ->
                    previous.copy(isRefreshing = true, refreshErrorResId = null)
                previous is AppsUiState.Content && next is AppsUiState.Error ->
                    previous.copy(isRefreshing = false, refreshErrorResId = next.messageResId)
                else -> next
            }
        }

    private val appsState =
        combine(appUiStates, observeFavoriteApps()) { state, favorites ->
            if (state is AppsUiState.Content) {
                val apps = state.apps.map { it.copy(isFavorite = it.packageName in favorites) }
                state.copy(apps = apps, favorites = apps.filter { it.isFavorite })
            } else state
        }
            .catch { emit(AppsUiState.Error(R.string.apps_load_error)) }

    val uiState: StateFlow<LauncherUiState> =
        combine(appsState, selectedAppPackage, favoritePickerOpen) { apps, selected, pickerOpen ->
            LauncherUiState(
                appsState = apps,
                // Resolve against the latest list so the popup closes if the app gets uninstalled.
                appOptions = (apps as? AppsUiState.Content)?.apps?.firstOrNull { it.packageName == selected?.packageName },
                favoriteOptions = selected?.fromFavorites == true,
                favoritePickerApps = if (pickerOpen) {
                    (apps as? AppsUiState.Content)?.apps.orEmpty().filterNot { it.isFavorite }
                } else null,
            )
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherUiState())

    private val effects = Channel<LauncherEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    fun onAppSelected(app: LauncherAppUiState) {
        selectedAppPackage.value = null
        sendEffect(LauncherEffect.LaunchApp(app.packageName, app.activityName))
    }

    fun onAppLongPressed(app: LauncherAppUiState) {
        selectedAppPackage.value = AppSelection(app.packageName, fromFavorites = false)
    }

    fun onFavoriteLongPressed(app: LauncherAppUiState) {
        selectedAppPackage.value = AppSelection(app.packageName, fromFavorites = true)
    }

    fun onAddFavoriteSelected() {
        favoritePickerOpen.value = true
    }

    fun onFavoritePickerDismissed() {
        favoritePickerOpen.value = false
    }

    fun onFavoriteAppPicked(app: LauncherAppUiState) {
        val available = uiState.value.favoritePickerApps.orEmpty()
        if (available.none { it.packageName == app.packageName }) return
        favoritePickerOpen.value = false
        viewModelScope.launch { toggleFavoriteApp(app.packageName) }
    }

    fun onAppOptionsDismissed() {
        selectedAppPackage.value = null
    }

    fun onToggleFavoriteSelected(app: LauncherAppUiState) {
        selectedAppPackage.value = null
        viewModelScope.launch { toggleFavoriteApp(app.packageName) }
    }

    fun onUninstallSelected(app: LauncherAppUiState) {
        selectedAppPackage.value = null
        if (app.isRemovable) sendEffect(LauncherEffect.UninstallApp(app.packageName))
    }

    fun onInputSelected() = sendEffect(LauncherEffect.OpenInputs)

    fun onSettingsSelected() = sendEffect(LauncherEffect.OpenSettings)

    fun onPlayStoreSelected() = sendEffect(LauncherEffect.OpenPlayStore)

    private fun sendEffect(effect: LauncherEffect) {
        viewModelScope.launch { effects.send(effect) }
    }
}
