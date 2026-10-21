package com.techascent.tv_launcher.presentation.ui.options

import android.view.KeyEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

private class AppMenuPosition : PopupPositionProvider {
    var anchorOnLeft by mutableStateOf(true)
    var below by mutableStateOf(true)

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        anchorOnLeft = anchorBounds.center.x <= windowSize.width / 2
        below = anchorBounds.bottom + popupContentSize.height <= windowSize.height
        val x = if (anchorOnLeft) anchorBounds.left else anchorBounds.right - popupContentSize.width
        val y = if (below) anchorBounds.bottom else anchorBounds.top - popupContentSize.height
        return IntOffset(
            x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
            y.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)),
        )
    }
}

@Composable
fun AppOptionsPopup(
    app: LauncherAppUiState,
    favorite: Boolean,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    val confirmKeyGuard = remember { ConfirmKeyPressGuard() }
    val position = remember { AppMenuPosition() }
    Popup(
        popupPositionProvider = position,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            Modifier.width(LauncherSpacing.AppMenuWidth).onPreviewKeyEvent { event ->
                val key = event.nativeKeyEvent
                when (key.keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER ->
                        confirmKeyGuard.shouldConsume(
                            key.keyCode,
                            isDown = key.action == KeyEvent.ACTION_DOWN,
                            repeatCount = key.repeatCount,
                        )
                    else -> false
                }
            },
        ) {
            if (position.below) MenuPointer(position.anchorOnLeft, true)
            Column(
                Modifier.background(
                    LauncherColors.AppMenuBackground,
                    RoundedCornerShape(LauncherSpacing.Small),
                ).clip(RoundedCornerShape(LauncherSpacing.Small)),
            ) {
                MenuOption(
                    stringResource(R.string.open_app), onOpen,
                    iconRes = R.drawable.ic_open_app,
                    modifier = Modifier.focusRequester(firstFocus),
                )
                if (favorite) {
                    MenuOption(
                        stringResource(R.string.remove_from_favorites), onRemove,
                        iconRes = R.drawable.ic_remove_favourite,
                    )
                } else {
                    MenuOption(
                        stringResource(if (app.isRemovable) R.string.uninstall else R.string.uninstall_unavailable),
                        onUninstall, iconRes = R.drawable.ic_uninstall, enabled = app.isRemovable,
                    )
                }
            }
            if (!position.below) MenuPointer(position.anchorOnLeft, false)
        }
    }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }
}

@Composable
private fun MenuPointer(onLeft: Boolean, below: Boolean) {
    Canvas(Modifier.fillMaxWidth().height(LauncherSpacing.Small)) {
        val x = if (onLeft) size.width * 0.12f else size.width * 0.88f
        val path = Path().apply {
            moveTo(x, if (below) 0f else size.height)
            lineTo(x - size.height, if (below) size.height else 0f)
            lineTo(x + size.height, if (below) size.height else 0f)
            close()
        }
        drawPath(path, LauncherColors.AppMenuBackground)
    }
}

@Composable
private fun MenuOption(
    text: String,
    onClick: () -> Unit,
    iconRes: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    val color = if (enabled) LauncherColors.AppMenuText else LauncherColors.AppMenuDisabled
    Row(
        modifier = modifier.fillMaxWidth()
            .background(if (focused) LauncherColors.AppMenuFocused else LauncherColors.AppMenuBackground)
            .onFocusChanged { focused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ).padding(LauncherSpacing.Medium),
        horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(iconRes), null,
            modifier = Modifier.size(LauncherSpacing.ButtonIconSize),
            colorFilter = ColorFilter.tint(color),
        )
        Text(text, color = color, fontSize = 18.sp)
    }
}
