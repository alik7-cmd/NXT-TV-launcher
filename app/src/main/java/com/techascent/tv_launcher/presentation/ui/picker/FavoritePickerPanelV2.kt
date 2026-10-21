package com.techascent.tv_launcher.presentation.ui.picker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.ui.common.AppIcon
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/** Side panel listing the apps that can still be added to favourites; one lazy list, stock TV rows. */
@Composable
fun FavoritePickerPanelV2(
    apps: List<LauncherAppUiState>,
    onSelect: (LauncherAppUiState) -> Unit,
    onDismiss: () -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.4f)
                    .fillMaxHeight()
                    .background(LauncherColors.DialogBackground),
            ) {
                Text(
                    text = stringResource(R.string.select_favorite_app),
                    color = LauncherColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(LauncherSpacing.Large),
                )
                LazyColumn(Modifier.weight(1f).focusRequester(firstFocus)) {
                    if (apps.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.favorites_all_added),
                                color = LauncherColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = LauncherSpacing.Large),
                            )
                        }
                        item { PickerRow(stringResource(R.string.cancel), null, onDismiss) }
                    } else {
                        items(apps, key = { it.packageName }) { app ->
                            PickerRow(app.label, app, onClick = { onSelect(app) })
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { runCatching { firstFocus.requestFocus() } }
}

@Composable
private fun PickerRow(label: String, app: LauncherAppUiState?, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (focused) LauncherColors.TileFocusedBackground else LauncherColors.DialogBackground)
            .onFocusChanged { focused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = LauncherSpacing.Large, vertical = LauncherSpacing.Medium),
        horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (app != null) {
            val banner = app.banner
            if (banner != null) {
                Image(banner, null, Modifier.size(width = 64.dp, height = 36.dp))
            } else {
                AppIcon(app, 36.dp)
            }
        }
        Text(label, color = LauncherColors.TextPrimary, fontSize = 18.sp)
    }
}
