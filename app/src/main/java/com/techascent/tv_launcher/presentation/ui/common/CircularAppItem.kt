package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing
import com.techascent.tv_launcher.ui.theme.TVLauncherTheme

/** Circular app icon with an optional, single-line name below it. */
@Composable
fun CircularAppItem(
    app: LauncherAppUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    size: Dp = 75.dp,
    shouldShowAppName: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    Column(
        modifier = modifier.width(size),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
    ) {
        Surface(
            onClick = onClick,
            onLongClick = onLongClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .size(size)
                .semantics { contentDescription = app.label },
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = LauncherColors.TileFallbackIconBackground,
                focusedContainerColor = LauncherColors.TileFocusedBackground,
                pressedContainerColor = LauncherColors.TileFocusedBackground,
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(LauncherSpacing.FocusBorderWidth, LauncherColors.FocusBorder),
                    shape = CircleShape,
                ),
            ),
            glow = ClickableSurfaceDefaults.glow(
                focusedGlow = Glow(LauncherColors.FocusGlow, LauncherSpacing.TileFocusGlow),
            ),
        ) {
            val icon = app.icon
            if (icon != null) {
                // Icons are full-bleed (see InstalledAppDto), so cropping fills the whole circle.
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Text(
                    text = app.label.take(1).uppercase(),
                    color = LauncherColors.TextPrimary,
                    fontSize = (size.value * 0.36f).sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        if (shouldShowAppName) {
            Text(
                text = app.label,
                color = if (focused) LauncherColors.TextPrimary else LauncherColors.TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = if (focused) TextOverflow.Clip else TextOverflow.Ellipsis,
                modifier = Modifier.width(size)
                    .then(if (focused) Modifier.basicMarquee(iterations = Int.MAX_VALUE) else Modifier),
            )
        }
    }
}

@Preview(name = "Circular app row", widthDp = 960, heightDp = 200)
@Composable
private fun CircularAppItemPreview() {
    val apps = listOf("Netflix", "YouTube", "Disney+", "Plex", "Spotify")
        .map { LauncherAppUiState("example.${it.lowercase()}", "", it, null) }
    TVLauncherTheme {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Large),
            // Leaves room for the focus scale and glow so they aren't clipped by the row.
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = LauncherSpacing.Large),
        ) {
            items(apps, key = { it.packageName }) { app -> CircularAppItem(app = app, onClick = {}) }
        }
    }
}
