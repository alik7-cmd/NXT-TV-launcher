package com.techascent.tv_launcher.presentation.ui.favourite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.UiState
import com.techascent.tv_launcher.presentation.ui.common.AppTileV2
import com.techascent.tv_launcher.presentation.ui.common.SectionTitle
import com.techascent.tv_launcher.presentation.ui.common.ShimmerTileRow
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

internal fun LazyListScope.favouriteSection(
    state: UiState<List<LauncherAppUiState>>,
    tileWidth: Dp,
    onAppClick: (LauncherAppUiState) -> Unit,
    onAppLongClick: (LauncherAppUiState) -> Unit,
    onAddFavoriteClick: () -> Unit,
    focusRequester: FocusRequester,
    options: @Composable (LauncherAppUiState) -> Unit,
) {
    val favorites = when (state) {
        UiState.Loading -> {
            item(key = "favourite_title") { SectionTitle(stringResource(R.string.favorites_heading)) }
            item(key = "favourite_row") { ShimmerTileRow(count = 4, tileWidth = tileWidth) }
            return
        }
        is UiState.Error -> return
        is UiState.Success -> state.data
    }
    item(key = "favourite_title") { SectionTitle(stringResource(R.string.favorites_heading)) }
    item(key = "favourite_row") {
        LazyRow(
            modifier = Modifier.focusRequester(focusRequester).focusRestorer().bleed(FOCUS_ROOM),
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
            contentPadding = PaddingValues(horizontal = FOCUS_ROOM),
        ) {
            items(favorites, key = { it.packageName }) { app ->
                AppTileV2(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    modifier = Modifier.width(tileWidth),
                    options = { options(app) },
                )
            }
            item(key = "add_favourite") {
                AddTileV2(onClick = onAddFavoriteClick, modifier = Modifier.width(tileWidth))
            }
        }
    }
}

private val FOCUS_ROOM = 32.dp

/** Widens the row by [amount] on both sides so its padding doesn't indent the tiles. */
private fun Modifier.bleed(amount: Dp) = layout { measurable, constraints ->
    val extra = amount.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra * 2))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra, 0) }
}
