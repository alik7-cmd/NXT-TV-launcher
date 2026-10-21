package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.CardContainerDefaults
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.dp
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/** Card with a 16:9 [image] and its label centred below, like the stock Google TV launcher. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RectangleCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    shouldShowAppName: Boolean = false,
    image: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    StandardCardContainer(
        modifier = modifier,
        interactionSource = interactionSource,
        contentColor = CardContainerDefaults.contentColor(
            contentColor = LauncherColors.TextSecondary,
            focusedContentColor = LauncherColors.TextPrimary,
        ),
        imageCard = { source ->
            Card(
                onClick = onClick,
                onLongClick = onLongClick,
                interactionSource = source,
                glow = CardDefaults.glow(
                    focusedGlow = Glow(LauncherColors.FocusGlow, LauncherSpacing.TileFocusGlow),
                ),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(LauncherColors.TileFallbackIconBackground),
                    contentAlignment = Alignment.Center,
                ) { image() }
            }
        },
        title = {
            if(shouldShowAppName){
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = if (focused) TextOverflow.Clip else TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = LauncherSpacing.Small)
                        // Scrolls only while focused; an always-running marquee redraws every frame.
                        .then(if (focused) Modifier.basicMarquee(iterations = Int.MAX_VALUE) else Modifier),
                )
            }else Spacer(modifier = Modifier.padding(0.dp))
        },
    )
}
