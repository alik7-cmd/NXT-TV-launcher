package com.techascent.tv_launcher.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.LauncherAppUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
fun FavoritePickerPanel(
    apps: List<LauncherAppUiState>,
    onSelect: (LauncherAppUiState) -> Unit,
    onDismiss: () -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize().background(LauncherColors.BackgroundVignette)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            Column(
                Modifier.align(Alignment.CenterEnd).fillMaxWidth(0.46f).fillMaxHeight()
                    .background(LauncherColors.DialogBackground),
            ) {
                Text(
                    stringResource(R.string.select_favorite_app),
                    color = LauncherColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                        .background(LauncherColors.BackgroundTop).padding(LauncherSpacing.Large),
                )
                if (apps.isEmpty()) {
                    Text(
                        stringResource(R.string.favorites_all_added),
                        color = LauncherColors.TextSecondary,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(LauncherSpacing.Large),
                    )
                    DialogOption(
                        stringResource(R.string.cancel), onDismiss,
                        Modifier.padding(horizontal = LauncherSpacing.Large).focusRequester(firstFocus),
                    )
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                            FavoritePickerRow(
                                app = app,
                                onClick = { onSelect(app) },
                                modifier = if (index == 0) Modifier.focusRequester(firstFocus) else Modifier,
                            )
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(apps.firstOrNull()?.packageName) { firstFocus.requestFocus() }
}

@Composable
private fun FavoritePickerRow(app: LauncherAppUiState, onClick: () -> Unit, modifier: Modifier) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.fillMaxWidth()
            .background(if (focused) LauncherColors.TileFocusedBackground else LauncherColors.DialogBackground)
            .onFocusChanged { focused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(LauncherSpacing.Large),
        horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.width(LauncherSpacing.TileWidth).aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(LauncherSpacing.Small))
                .background(LauncherColors.TileFallbackIconBackground),
            contentAlignment = Alignment.Center,
        ) {
            val banner = app.banner
            if (banner != null) {
                Image(banner, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                AppIcon(app, LauncherSpacing.TileIconSize)
            }
        }
        Text(app.label, color = LauncherColors.TextPrimary, fontSize = 18.sp)
    }
}
