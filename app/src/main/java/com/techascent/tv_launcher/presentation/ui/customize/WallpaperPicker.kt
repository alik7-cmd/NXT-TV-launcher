package com.techascent.tv_launcher.presentation.ui.customize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.LauncherWallpaper
import com.techascent.tv_launcher.presentation.ui.common.thumbnailRes
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/** Only tiny thumbnails are decoded here. The real background changes after Apply closes the dialog. */
@Composable
internal fun WallpaperPicker(
    selected: LauncherWallpaper,
    onApply: (LauncherWallpaper) -> Unit,
    onBack: () -> Unit,
) {
    var pending by rememberSaveable { mutableStateOf(selected) }
    val initialFocus = remember { FocusRequester() }
    val shape = RoundedCornerShape(LauncherSpacing.DialogCornerRadius)
    Column(
        modifier = Modifier.width(LauncherSpacing.DialogWidth)
            .clip(shape)
            .background(LauncherColors.DialogBackground)
            .border(1.dp, LauncherColors.DialogBorder, shape)
            .padding(LauncherSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
    ) {
        Text(
            stringResource(R.string.wallpaper_heading),
            color = LauncherColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.wallpaper_hint),
            color = LauncherColors.TextTertiary,
            fontSize = 13.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small)) {
            LauncherWallpaper.entries.forEachIndexed { index, wallpaper ->
                val label = stringResource(R.string.wallpaper_number, index + 1)
                val thumbnailShape = RoundedCornerShape(LauncherSpacing.Small)
                Surface(
                    onClick = { pending = wallpaper },
                    modifier = Modifier.weight(1f)
                        .then(if (wallpaper == selected) Modifier.focusRequester(initialFocus) else Modifier)
                        .semantics {
                            contentDescription = label
                            this.selected = pending == wallpaper
                            role = Role.RadioButton
                        },
                    shape = ClickableSurfaceDefaults.shape(thumbnailShape),
                    colors = optionColors(),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
                ) {
                    Column(
                        Modifier.fillMaxWidth()
                            .then(
                                if (pending == wallpaper) Modifier.border(2.dp, LauncherColors.TextPrimary, thumbnailShape)
                                else Modifier,
                            )
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
                    ) {
                        Image(
                            painterResource(wallpaper.thumbnailRes()),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(thumbnailShape),
                        )
                        Text(label, fontSize = 13.sp)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small)) {
            PickerButton(stringResource(R.string.wallpaper_back), onBack, Modifier.weight(1f))
            PickerButton(stringResource(R.string.wallpaper_apply), { onApply(pending) }, Modifier.weight(1f))
        }
    }
    LaunchedEffect(Unit) { initialFocus.requestFocus() }
}

@Composable
private fun PickerButton(label: String, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LauncherSpacing.Medium)),
        colors = optionColors(),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
    ) {
        Text(label, fontSize = 16.sp, modifier = Modifier.padding(LauncherSpacing.Medium))
    }
}
