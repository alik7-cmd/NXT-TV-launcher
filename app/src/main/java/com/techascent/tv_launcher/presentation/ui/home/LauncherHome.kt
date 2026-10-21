package com.techascent.tv_launcher.presentation.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.AppStyle
import com.techascent.tv_launcher.domain.model.LauncherWallpaper
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.LauncherUiState
import com.techascent.tv_launcher.presentation.state.UiState
import com.techascent.tv_launcher.presentation.ui.allapps.allAppsSection
import com.techascent.tv_launcher.presentation.ui.common.WallpaperBackground
import com.techascent.tv_launcher.presentation.ui.common.circularGridColumns
import com.techascent.tv_launcher.presentation.ui.common.gridTileLayout
import com.techascent.tv_launcher.presentation.ui.common.SectionTitle
import com.techascent.tv_launcher.presentation.ui.customize.CustomizeDialog
import com.techascent.tv_launcher.presentation.ui.options.AppOptionsDialog
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
import kotlinx.coroutines.launch

/** Photo-background home with recent apps above the installed-app grid. */
@Composable
fun LauncherHome(
    uiState: LauncherUiState,
    onAppClick: (LauncherAppUiState) -> Unit,
    onAppLongClick: (LauncherAppUiState) -> Unit,
    onUninstall: (LauncherAppUiState) -> Unit,
    onOptionsDismiss: () -> Unit,
    onInputClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCustomizeClick: () -> Unit,
    onAppStyleSelected: (AppStyle) -> Unit,
    onCustomizeDismiss: () -> Unit,
    onShowAppNamesSelected: (Boolean) -> Unit,
    onWallpaperApplied: (LauncherWallpaper) -> Unit,
) {
    val circular = uiState.appStyle == AppStyle.Circle
    if (uiState.customizeOpen) {
        CustomizeDialog(
            selected = uiState.appStyle,
            previewApp = (uiState.recentApps as? UiState.Success)?.data?.firstOrNull()
                ?: (uiState.allApps as? UiState.Success)?.data?.firstOrNull(),
            onSelect = onAppStyleSelected,
            onDismiss = onCustomizeDismiss,
            showAppNames = uiState.showAppNames,
            onShowAppNamesSelected = onShowAppNamesSelected,
            wallpaper = uiState.wallpaper,
            onWallpaperApplied = onWallpaperApplied,
        )
    }
    uiState.appOptions?.let { app ->
        AppOptionsDialog(
            app = app,
            onOpen = { onAppClick(app) },
            onUninstall = { onUninstall(app) },
            onDismiss = onOptionsDismiss,
        )
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val recentFocus = remember { FocusRequester() }
    val topBarFocus = remember { FocusRequester() }
    // Keep the header available from the first apps row; hide it once focus moves further down.
    val focusedRows = remember { mutableStateMapOf<Int, Boolean>() }
    val headerVisible = focusedRows.isEmpty()
    BackHandler(enabled = listState.canScrollBackward || !headerVisible) {
        focusedRows.clear()
        scope.launch {
            listState.animateScrollToItem(0)
            // The recent row may not be composed when it is empty or loading.
            runCatching { recentFocus.requestFocus() }.onFailure { runCatching { topBarFocus.requestFocus() } }
        }
    }

    Box(Modifier.fillMaxSize()) {
        WallpaperBackground(uiState.wallpaper)
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))),
        )
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val contentWidth = maxWidth - LauncherSpacing.ScreenHorizontal * 2
            val (columns, tileWidth) = if (circular) {
                circularGridColumns(
                    contentWidth,
                    LauncherSpacing.CircularItemSize,
                    LauncherSpacing.CircularItemGap,
                ) to LauncherSpacing.CircularItemSize
            } else {
                gridTileLayout(contentWidth)
            }
            val recentTileWidth = if (circular) {
                LauncherSpacing.CircularItemSize
            } else {
                (contentWidth - LauncherSpacing.Medium * (RECENT_APPS_SLOTS - 1)) / RECENT_APPS_SLOTS
            }

            LauncherScaffold(
                topBar = {
                    LauncherHeader(
                        visible = headerVisible,
                        header = (uiState.header as? UiState.Success)?.data,
                        recentApps = uiState.recentApps,
                        circular = circular,
                        tileWidth = recentTileWidth,
                        showAppNames = uiState.showAppNames,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onInputClick = onInputClick,
                        onSettingsClick = onSettingsClick,
                        onCustomizeClick = onCustomizeClick,
                        topBarFocus = topBarFocus,
                        recentFocus = recentFocus,
                    )
                },
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = LauncherSpacing.ScreenHorizontal,
                        vertical = LauncherSpacing.Medium,
                    ),
                    verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
                ) {
                    stickyHeader {
                        SectionTitle(stringResource(R.string.apps_heading))
                    }
                    allAppsSection(
                        state = uiState.allApps,
                        columns = columns,
                        tileWidth = tileWidth,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onRowFocusChanged = { row, focused ->
                            if (focused && row > 0) focusedRows[row] = true else focusedRows.remove(row)
                        },
                        circular = circular,
                        showAppNames = uiState.showAppNames,
                    )
                }
            }
        }
    }
}
