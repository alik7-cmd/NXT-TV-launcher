package com.techascent.tv_launcher.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddFavoriteTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFocusChanged: (Boolean) -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(LauncherSpacing.BannerCornerRadius)
    Column(
        modifier = modifier
            .onFocusChanged {
                focused = it.isFocused
                onFocusChanged(it.isFocused)
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
    ) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                .background(LauncherColors.TileFallbackIconBackground, shape)
                .focusHighlight(focused, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text("+", color = LauncherColors.TextSecondary, fontSize = 56.sp)
        }
        Text(
            stringResource(R.string.add_favorite_tile),
            color = if (focused) LauncherColors.TextPrimary else LauncherColors.TextSecondary,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = if (focused) TextOverflow.Clip else TextOverflow.Ellipsis,
            // Scroll only while selected: an always-running marquee redraws the whole screen
            // every frame, which makes D-pad scrolling stutter on low-end TVs.
            modifier = Modifier.fillMaxWidth().then(
                if (focused) Modifier.basicMarquee(iterations = Int.MAX_VALUE) else Modifier,
            ),
        )
    }
}
