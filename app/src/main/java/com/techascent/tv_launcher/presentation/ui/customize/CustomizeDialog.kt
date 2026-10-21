package com.techascent.tv_launcher.presentation.ui.customize

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.AppStyle
import com.techascent.tv_launcher.domain.model.LauncherWallpaper
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.ui.common.AppIcon
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
import com.techascent.tv_launcher.ui.theme.TVLauncherTheme

/**
 * Lets the user pick how apps look. Each choice previews [previewApp], and picking one applies it
 * straight away behind the dialog; Back or Done closes it.
 */
@Composable
fun CustomizeDialog(
    selected: AppStyle,
    previewApp: LauncherAppUiState?,
    onSelect: (AppStyle) -> Unit,
    onDismiss: () -> Unit,
    showAppNames: Boolean,
    onShowAppNamesSelected: (Boolean) -> Unit,
    wallpaper: LauncherWallpaper,
    onWallpaperApplied: (LauncherWallpaper) -> Unit,
) {
    val bannerFocus = remember { FocusRequester() }
    val circleFocus = remember { FocusRequester() }
    var choosingWallpaper by rememberSaveable { mutableStateOf(false) }
    Dialog(onDismissRequest = { if (choosingWallpaper) choosingWallpaper = false else onDismiss() }) {
        if (choosingWallpaper) {
            WallpaperPicker(
                selected = wallpaper,
                onApply = onWallpaperApplied,
                onBack = { choosingWallpaper = false },
            )
        } else {
            CustomizeContent(
                selected, previewApp ?: SamplePreviewApp, onSelect, onDismiss, bannerFocus, circleFocus,
                showAppNames, onShowAppNamesSelected,
                onWallpaperClick = { choosingWallpaper = true },
            )
        }
    }
    LaunchedEffect(choosingWallpaper) {
        if (!choosingWallpaper) (if (selected == AppStyle.Circle) circleFocus else bannerFocus).requestFocus()
    }
}

@Composable
private fun CustomizeContent(
    selected: AppStyle,
    app: LauncherAppUiState,
    onSelect: (AppStyle) -> Unit,
    onDone: () -> Unit,
    bannerFocus: FocusRequester,
    circleFocus: FocusRequester,
    showAppNames: Boolean,
    onShowAppNamesSelected: (Boolean) -> Unit,
    onWallpaperClick: () -> Unit,
) {
    val shape = RoundedCornerShape(LauncherSpacing.DialogCornerRadius)
    Column(
        modifier = Modifier
            .width(LauncherSpacing.DialogWidth)
            .clip(shape)
            .background(LauncherColors.DialogBackground)
            .border(1.dp, LauncherColors.DialogBorder, shape)
            .padding(LauncherSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(LauncherSpacing.ExtraSmall)) {
            Text(
                text = stringResource(R.string.customize),
                color = LauncherColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.customize_subtitle),
                color = LauncherColors.TextTertiary,
                fontSize = 13.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium)) {
            StyleCard(
                label = stringResource(R.string.style_banner),
                style = AppStyle.Banner,
                app = app,
                isSelected = selected == AppStyle.Banner,
                showAppNames = showAppNames,
                onClick = { onSelect(AppStyle.Banner) },
                modifier = Modifier.weight(1f).focusRequester(bannerFocus),
            )
            StyleCard(
                label = stringResource(R.string.style_circle),
                style = AppStyle.Circle,
                app = app,
                isSelected = selected == AppStyle.Circle,
                showAppNames = showAppNames,
                onClick = { onSelect(AppStyle.Circle) },
                modifier = Modifier.weight(1f).focusRequester(circleFocus),
            )
        }
        Surface(
            onClick = { onShowAppNamesSelected(!showAppNames) },
            modifier = Modifier.fillMaxWidth().semantics {
                role = Role.Switch
                toggleableState = ToggleableState(showAppNames)
            },
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LauncherSpacing.Medium)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
            colors = optionColors(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(LauncherSpacing.Medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.show_app_names), fontSize = 16.sp, modifier = Modifier.weight(1f))
                AppNamesSwitch(showAppNames)
            }
        }
        Surface(
            onClick = onWallpaperClick,
            modifier = Modifier.fillMaxWidth(),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LauncherSpacing.Medium)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
            colors = optionColors(),
        ) {
            Text(
                stringResource(R.string.wallpaper_heading),
                fontSize = 16.sp,
                modifier = Modifier.padding(LauncherSpacing.Medium),
            )
        }
        Surface(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LauncherSpacing.Medium)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
            colors = optionColors(),
        ) {
            Text(
                text = stringResource(R.string.done),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LauncherSpacing.DialogOptionVerticalPadding),
            )
        }
    }
}

/** Focus inverts the card to a light surface, matching the options dialog; a ring marks the chosen style. */
@Composable
private fun StyleCard(
    label: String,
    style: AppStyle,
    app: LauncherAppUiState,
    isSelected: Boolean,
    showAppNames: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(LauncherSpacing.Medium)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = label
            selected = isSelected
        },
        shape = ClickableSurfaceDefaults.shape(shape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        colors = optionColors(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) Modifier.border(2.dp, LauncherColors.TextPrimary, shape) else Modifier,
                )
                .padding(vertical = LauncherSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
        ) {
            Box(Modifier.height(PreviewAreaHeight), contentAlignment = Alignment.Center) {
                AppStylePreview(app, style)
            }
            if (showAppNames) {
                Text(
                    app.label,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = LauncherSpacing.Small),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionDot(isSelected)
            }
        }
    }
}

/** Radio-style marker that follows the card's content colour, so it reads on dark and light cards. */
@Composable
private fun SelectionDot(selected: Boolean) {
    val color = androidx.tv.material3.LocalContentColor.current
    Box(Modifier.size(16.dp).border(2.dp, color, CircleShape), contentAlignment = Alignment.Center) {
        if (selected) Box(Modifier.size(8.dp).background(color, CircleShape))
    }
}

/** Visual only: the parent row owns focus, switch semantics and remote clicks. */
@Composable
private fun AppNamesSwitch(checked: Boolean) {
    val progress by animateFloatAsState(if (checked) 1f else 0f, label = "app_names_switch")
    val contentColor = androidx.tv.material3.LocalContentColor.current
    Canvas(Modifier.size(width = 40.dp, height = 24.dp)) {
        drawRoundRect(
            color = if (checked) LauncherColors.TileFallbackIconBackground else contentColor.copy(alpha = 0.3f),
            cornerRadius = CornerRadius(size.height / 2),
        )
        val radius = 9.dp.toPx()
        val inset = size.height / 2
        drawCircle(
            color = if (checked) LauncherColors.TextPrimary else contentColor,
            radius = radius,
            center = Offset(inset + (size.width - inset * 2) * progress, inset),
        )
    }
}

/** Static copy of how [app] is drawn for [style] on the home screen. */
@Composable
private fun AppStylePreview(app: LauncherAppUiState, style: AppStyle) {
    val banner = app.banner
    when (style) {
        AppStyle.Banner -> Box(
            modifier = Modifier
                .size(width = 112.dp, height = 63.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LauncherColors.TileFallbackIconBackground),
            contentAlignment = Alignment.Center,
        ) {
            if (banner != null) {
                Image(banner, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                AppIcon(app, 40.dp)
            }
        }
        AppStyle.Circle -> Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(LauncherColors.TileFallbackIconBackground),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(app, 64.dp)
        }
    }
}

@Composable
internal fun optionColors() = ClickableSurfaceDefaults.colors(
    containerColor = Color.White.copy(alpha = 0.06f),
    contentColor = LauncherColors.TextPrimary,
    focusedContainerColor = LauncherColors.DialogOptionFocused,
    focusedContentColor = LauncherColors.DialogOptionFocusedText,
    pressedContainerColor = LauncherColors.DialogOptionFocused,
    pressedContentColor = LauncherColors.DialogOptionFocusedText,
)

private val PreviewAreaHeight = 72.dp

private val SamplePreviewApp = LauncherAppUiState("example.app", "", "App", null)

@Preview(name = "Customize", widthDp = 960, heightDp = 540, backgroundColor = 0xFF080D16, showBackground = true)
@Composable
private fun CustomizePreview() {
    TVLauncherTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CustomizeContent(
                selected = AppStyle.Circle,
                app = SamplePreviewApp.copy(label = "Netflix"),
                onSelect = {},
                onDone = {},
                bannerFocus = remember { FocusRequester() },
                circleFocus = remember { FocusRequester() },
                showAppNames = true,
                onShowAppNamesSelected = {},
                onWallpaperClick = {},
            )
        }
    }
}
