package com.techascent.tv_launcher.presentation.ui.allapps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.UiState
import com.techascent.tv_launcher.presentation.ui.common.RectangleAppItem
import com.techascent.tv_launcher.presentation.ui.common.CircularAppItem
import com.techascent.tv_launcher.presentation.ui.common.ShimmerCircleGrid
import com.techascent.tv_launcher.presentation.ui.common.ShimmerRectangleGrid
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
import kotlin.collections.chunked

internal fun LazyListScope.allAppsSection(
    state: UiState<List<LauncherAppUiState>>,
    columns: Int,
    tileWidth: Dp,
    onAppClick: (LauncherAppUiState) -> Unit,
    onAppLongClick: (LauncherAppUiState) -> Unit,
    onRowFocusChanged: (row: Int, focused: Boolean) -> Unit,
    circular: Boolean = false,
    showAppNames: Boolean = false,
) {
    when (state) {
        UiState.Loading -> item(key = "all_apps_loading") {
            if (circular) {
                ShimmerCircleGrid(
                    rows = 2,
                    columns = columns,
                    size = LauncherSpacing.CircularItemSize,
                    gap = LauncherSpacing.CircularItemGap,
                )
            } else {
                ShimmerRectangleGrid(rows = 2, columns = columns, tileWidth = tileWidth)
            }
        }
        is UiState.Error -> item(key = "all_apps_error") {
            StatusText(stringResource(state.messageResId), LauncherColors.TextError)
        }
        is UiState.Success -> if (state.data.isEmpty()) {
            item(key = "all_apps_empty") { StatusText(stringResource(R.string.apps_empty)) }
        } else {
            allAppsContent(state.data, columns, tileWidth, onAppClick, onAppLongClick, onRowFocusChanged, circular, showAppNames)
        }
    }
}

private fun LazyListScope.allAppsContent(
    apps: List<LauncherAppUiState>,
    columns: Int,
    tileWidth: Dp,
    onAppClick: (LauncherAppUiState) -> Unit,
    onAppLongClick: (LauncherAppUiState) -> Unit,
    onRowFocusChanged: (row: Int, focused: Boolean) -> Unit,
    circular: Boolean,
    showAppNames: Boolean,
) {
    itemsIndexed(apps.chunked(columns), key = { index, _ -> "all_apps_row_$index" }) { index, row ->
        DisposableEffect(index) { onDispose { onRowFocusChanged(index, false) } }
        Row(
            modifier = Modifier.onFocusChanged { onRowFocusChanged(index, it.hasFocus) },
            horizontalArrangement = Arrangement.spacedBy(
                if (circular) LauncherSpacing.CircularItemGap else LauncherSpacing.Medium,
            ),
        ) {
            if (circular) {
                row.forEach { app ->
                    Box(Modifier.animateItem()) {
                        CircularAppItem(
                            app = app,
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) },
                            shouldShowAppName = showAppNames,
                        )
                    }
                }
                return@Row
            }
            row.forEach { app ->
                RectangleAppItem(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    modifier = Modifier.width(tileWidth).animateItem(),
                    shouldShowAppName = showAppNames,
                )
            }
        }
    }
}

@Composable
private fun StatusText(text: String, color: Color = LauncherColors.TextSecondary, size: Int = 18) {
    Text(
        text = text,
        color = color,
        fontSize = size.sp,
        modifier = Modifier.padding(vertical = LauncherSpacing.Small),
    )
}