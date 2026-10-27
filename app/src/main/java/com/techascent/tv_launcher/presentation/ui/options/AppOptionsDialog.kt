package com.techascent.tv_launcher.presentation.ui.options

import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
import com.techascent.tv_launcher.ui.theme.TVLauncherTheme

/** App actions; Back dismisses without changing anything. */
@Composable
fun AppOptionsDialog(
    app: LauncherAppUiState,
    onOpen: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit,
    isPinned: Boolean,
    onPin: () -> Unit,
    onHide: () -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    // The long press that opened the dialog must not also click its first button.
    val confirmKeyGuard = remember { ConfirmKeyPressGuard() }
    Dialog(onDismissRequest = onDismiss) {
        AppOptionsContent(
            app = app,
            onOpen = onOpen,
            onUninstall = onUninstall,
            isPinned = isPinned,
            onPin = onPin,
            onHide = onHide,
            firstFocus = firstFocus,
            modifier = Modifier.onPreviewKeyEvent { event ->
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
        )
    }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }
}

@Composable
private fun AppOptionsContent(
    app: LauncherAppUiState,
    onOpen: () -> Unit,
    onUninstall: () -> Unit,
    firstFocus: FocusRequester,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    onPin: () -> Unit = {},
    onHide: () -> Unit = {},
) {
    val shape = RoundedCornerShape(LauncherSpacing.DialogCornerRadius)
    Column(
        modifier = modifier
            .width(LauncherSpacing.DialogWidth)
            .clip(shape)
            .background(LauncherColors.DialogBackground)
            .border(1.dp, LauncherColors.DialogBorder, shape)
            .padding(LauncherSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
    ) {
        AppHeader(app)
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = LauncherSpacing.Small)
                .height(1.dp)
                .background(LauncherColors.DialogDivider),
        )
        DialogOption(
            text = stringResource(R.string.open_app),
            iconRes = R.drawable.ic_open_app,
            onClick = onOpen,
            modifier = Modifier.focusRequester(firstFocus),
        )
        DialogOption(
            text = stringResource(if (isPinned) R.string.unpin_app else R.string.pin_app),
            iconRes = R.drawable.ic_pin,
            onClick = onPin,
        )
        DialogOption(
            text = stringResource(R.string.hide_app),
            iconRes = R.drawable.ic_hide,
            onClick = onHide,
        )
        DialogOption(
            text = stringResource(R.string.uninstall),
            supportingText = if (app.isRemovable) null else stringResource(R.string.uninstall_unavailable),
            iconRes = R.drawable.ic_uninstall,
            onClick = onUninstall,
            enabled = app.isRemovable,
            destructive = true,
        )
    }
}

@Composable
private fun AppHeader(app: LauncherAppUiState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(LauncherSpacing.DialogIconSize)
                .clip(CircleShape)
                .background(LauncherColors.TileFallbackIconBackground),
            contentAlignment = Alignment.Center,
        ) {
            val icon = app.icon
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(LauncherSpacing.DialogIconSize),
                )
            } else {
                Text(
                    text = app.label.take(1).uppercase(),
                    color = LauncherColors.TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(LauncherSpacing.ExtraSmall)) {
            Text(
                text = app.label,
                color = LauncherColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                color = LauncherColors.TextTertiary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Full-width row; focus inverts it to a light pill so the selection reads from across the room. */
@Composable
private fun DialogOption(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LauncherSpacing.Medium)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = LauncherColors.TextPrimary,
            focusedContainerColor = if (destructive) LauncherColors.DestructiveFocused else LauncherColors.DialogOptionFocused,
            focusedContentColor = if (destructive) LauncherColors.DestructiveFocusedText else LauncherColors.DialogOptionFocusedText,
            pressedContainerColor = if (destructive) LauncherColors.DestructiveFocused else LauncherColors.DialogOptionFocused,
            pressedContentColor = if (destructive) LauncherColors.DestructiveFocusedText else LauncherColors.DialogOptionFocusedText,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = LauncherColors.TextTertiary,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = LauncherSpacing.Medium, vertical = LauncherSpacing.DialogOptionVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val color = LocalContentColor.current
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color),
                modifier = Modifier.size(LauncherSpacing.ButtonIconSize),
            )
            Column {
                Text(text = text, color = color, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                if (supportingText != null) {
                    Text(text = supportingText, color = color, fontSize = 13.sp)
                }
            }
        }
    }
}

private val previewApp = LauncherAppUiState("com.netflix.ninja", "", "Netflix", null, isRemovable = true)

@Preview(name = "App options", widthDp = 960, heightDp = 540, backgroundColor = 0xFF080D16, showBackground = true)
@Composable
private fun AppOptionsPreview() {
    TVLauncherTheme {
        Box(contentAlignment = Alignment.Center) {
            AppOptionsContent(previewApp, onOpen = {}, onUninstall = {}, firstFocus = remember { FocusRequester() })
        }
    }
}

@Preview(name = "App options - system app", widthDp = 960, heightDp = 540, backgroundColor = 0xFF080D16, showBackground = true)
@Composable
private fun AppOptionsSystemAppPreview() {
    TVLauncherTheme {
        Box(contentAlignment = Alignment.Center) {
            AppOptionsContent(
                previewApp.copy(label = "Settings", packageName = "com.android.tv.settings", isRemovable = false),
                onOpen = {},
                onUninstall = {},
                firstFocus = remember { FocusRequester() },
            )
        }
    }
}
