package com.techascent.tv_launcher.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.UiState
import com.techascent.tv_launcher.presentation.ui.common.RectangleAppItem
import com.techascent.tv_launcher.presentation.ui.common.CircularAppItem
import com.techascent.tv_launcher.presentation.ui.common.ShimmerCircleRow
import com.techascent.tv_launcher.presentation.ui.common.ShimmerRectangleRow
import com.techascent.tv_launcher.presentation.ui.common.SectionTitle
import com.techascent.tv_launcher.presentation.ui.common.bleed
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

internal const val RECENT_APPS_SLOTS = 5

/** Hidden until an app has been launched; load errors are already shown in the apps section. */
@Composable
internal fun RecentApps(
    state: UiState<List<LauncherAppUiState>>,
    onAppClick: (LauncherAppUiState) -> Unit,
    onAppLongClick: (LauncherAppUiState) -> Unit,
    modifier: Modifier = Modifier,
    circular: Boolean = false,
    tileWidth: Dp,
    showAppNames: Boolean = false,
    @StringRes heading: Int = R.string.recent_apps_heading,
) {
    val apps = when (state) {
        UiState.Loading -> null
        is UiState.Error -> return
        is UiState.Success -> state.data.ifEmpty { return }
    }
    Column(verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small)) {
        SectionTitle(stringResource(heading))
        if (apps == null) {
            if (circular) {
                ShimmerCircleRow(
                    count = RECENT_APPS_SLOTS,
                    size = LauncherSpacing.CircularItemSize,
                    modifier = Modifier.padding(vertical = RECENT_FOCUS_ROOM),
                )
            } else {
                ShimmerRectangleRow(count = RECENT_APPS_SLOTS, tileWidth = tileWidth)
            }
        } else {
            LazyRow(
                modifier = modifier.bleed(RECENT_FOCUS_ROOM),
                horizontalArrangement = Arrangement.spacedBy(
                    if (circular) LauncherSpacing.CircularItemGap else LauncherSpacing.Medium,
                ),
                contentPadding = PaddingValues(horizontal = RECENT_FOCUS_ROOM, vertical = RECENT_FOCUS_ROOM),
            ) {
                items(apps, key = { it.packageName }) { app ->
                    if (circular) {
                        CircularAppItem(
                            app = app,
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) },
                            modifier = Modifier.animateItem(),
                            shouldShowAppName = showAppNames,
                        )
                    } else {
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
    }
}

private val RECENT_FOCUS_ROOM = 24.dp
