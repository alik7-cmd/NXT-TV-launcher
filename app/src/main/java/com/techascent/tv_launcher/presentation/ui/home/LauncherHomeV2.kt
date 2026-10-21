package com.techascent.tv_launcher.presentation.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateMapOf
import com.techascent.tv_launcher.presentation.ui.toolbar.LauncherHeader
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Dp
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.UiState
import com.techascent.tv_launcher.presentation.state.LauncherUiState
import com.techascent.tv_launcher.presentation.state.WeatherUiState
import com.techascent.tv_launcher.presentation.ui.options.AppOptionsPopup
import com.techascent.tv_launcher.presentation.ui.common.gridTileLayout
import com.techascent.tv_launcher.presentation.ui.allapps.allAppsSection
import com.techascent.tv_launcher.presentation.ui.favourite.favouriteSection
import com.techascent.tv_launcher.presentation.ui.weather.weatherSection
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
import kotlinx.coroutines.launch

/**
 * Home screen as one lazy list, like the stock launcher's single vertical grid: the toolbar row,
 * favourites and all apps are items of the same [LazyColumn].
 */
@Composable
fun LauncherHomeV2(
    uiState: LauncherUiState,
    weatherState: WeatherUiState,
    onAppClick: (LauncherAppUiState) -> Unit,
    onAppLongClick: (LauncherAppUiState) -> Unit,
    onFavoriteLongClick: (LauncherAppUiState) -> Unit,
    onAddFavoriteClick: () -> Unit,
    onRemoveFavorite: (LauncherAppUiState) -> Unit,
    onUninstall: (LauncherAppUiState) -> Unit,
    onOptionsDismiss: () -> Unit,
    onInputClick: () -> Unit,
    onSettingsClick: () -> Unit,
    forecastState: WeatherUiState = weatherState,
) {
    val optionsFor: @Composable (LauncherAppUiState, Boolean) -> Unit = { app, favorite ->
        if (uiState.appOptions?.packageName == app.packageName && uiState.favoriteOptions == favorite) {
            AppOptionsPopup(
                app = app,
                favorite = favorite,
                onOpen = { onAppClick(app) },
                onRemove = { onRemoveFavorite(app) },
                onUninstall = { onUninstall(app) },
                onDismiss = onOptionsDismiss,
            )
        }
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val favouritesFocus = remember { FocusRequester() }
    // Back from a scrolled list returns to the top, like the stock launcher.
    BackHandler(enabled = listState.canScrollBackward) {
        scope.launch {
            listState.animateScrollToItem(0)
            // Throws if the favourites row isn't composed (e.g. still loading).
            runCatching { favouritesFocus.requestFocus() }
        }
    }

    // Driven by focus, not scroll offset: hiding the header resizes the list, which would flip an
    // offset-based trigger back and forth.
    val focusedRows = remember { mutableStateMapOf<Int, Boolean>() }
    val headerVisible = focusedRows.values.none { it }
    val header = (uiState.header as? UiState.Success)?.data
    LauncherScaffoldV2(
        topBar = {
            AnimatedVisibility(
                visible = headerVisible && header != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                if (header != null) LauncherHeader(
                    state = header,
                    weather = weatherState,
                    onInputClick = onInputClick,
                    onSettingsClick = onSettingsClick,
                    modifier = Modifier.padding(
                        horizontal = LauncherSpacing.ScreenHorizontal,
                        vertical = LauncherSpacing.Medium,
                    ),
                )
            }
        },
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val (columns, tileWidth) = gridTileLayout(maxWidth - LauncherSpacing.ScreenHorizontal * 2)

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = LauncherSpacing.ScreenHorizontal,
                    vertical = LauncherSpacing.Medium,
                ),
                verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
            ) {
                favouriteSection(
                    state = uiState.favorites,
                    tileWidth = tileWidth,
                    onAppClick = onAppClick,
                    onAppLongClick = onFavoriteLongClick,
                    onAddFavoriteClick = onAddFavoriteClick,
                    focusRequester = favouritesFocus,
                    options = { optionsFor(it, true) },
                )
                allAppsSection(
                    state = uiState.allApps,
                    columns = columns,
                    tileWidth = tileWidth,
                    onAppClick = onAppClick,
                    onAppLongClick = onAppLongClick,
                    onRowFocusChanged = { row, focused ->
                        if (focused) focusedRows[row] = true else focusedRows.remove(row)
                    },
                    options = { optionsFor(it, false) },
                )

                weatherSection(state = forecastState)
            }
        }
    }
}

private fun LazyListScope.spacer(
    size: Dp = LauncherSpacing.Small
) {
    item {
        Spacer(modifier = Modifier.size(size))
    }
}
