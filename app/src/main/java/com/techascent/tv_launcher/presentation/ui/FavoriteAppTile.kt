package com.techascent.tv_launcher.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Text
import com.techascent.tv_launcher.presentation.LauncherAppUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
fun FavoriteAppTile(
    app: LauncherAppUiState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    optionsOpen: Boolean = false,
    options: @Composable () -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    val highlighted = focused || optionsOpen
    val scale by animateFloatAsState(
        if (highlighted) 1.12f else 1f,
        animationSpec = tween(180),
        label = "app banner scale",
    )
    val elevation by animateFloatAsState(
        if (highlighted) LauncherSpacing.BannerFocusedElevation.value else 0f,
        animationSpec = tween(180),
        label = "app banner elevation",
    )
    val shape = RoundedCornerShape(LauncherSpacing.BannerCornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .zIndex(if (highlighted) 1f else 0f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .onFocusChanged { focused = it.isFocused }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
    ) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                .shadow(elevation.dp, shape, clip = false)
                .focusHighlight(highlighted, shape, glow = 0.dp)
                .clip(shape)
                .background(LauncherColors.TileFallbackIconBackground),
            contentAlignment = Alignment.Center,
        ) {
            val banner = app.banner
            if (banner != null) {
                Image(
                    bitmap = banner,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                AppIcon(app, LauncherSpacing.DockTileIconSize)
            }
        }
        Text(
            app.label,
            color = if (highlighted) LauncherColors.TextPrimary else LauncherColors.TextSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        options()
    }
}
