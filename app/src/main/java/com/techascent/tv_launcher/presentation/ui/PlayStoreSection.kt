package com.techascent.tv_launcher.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
fun PlayStoreSection(onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(LauncherSpacing.TileCornerRadius)
    val headingColor by animateColorAsState(
        if (focused) LauncherColors.TextPrimary else LauncherColors.TextSecondary,
        label = "store heading",
    )
    val descriptionColor by animateColorAsState(
        if (focused) LauncherColors.TextSecondary else LauncherColors.TextTertiary,
        label = "store description",
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = LauncherSpacing.Medium)
            .border(
                LauncherSpacing.StoreBorderWidth, LauncherColors.StorePanelBorder, shape,
            )
            .padding(LauncherSpacing.Large),
        horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LauncherSpacing.ExtraSmall),
        ) {
            Text(stringResource(R.string.store_heading), color = headingColor, fontSize = 22.sp)
            Text(stringResource(R.string.store_description), color = descriptionColor, fontSize = 14.sp)
        }
        Row(
            modifier = Modifier
                .focusHighlight(
                    focused,
                    RoundedCornerShape(LauncherSpacing.StoreButtonCornerRadius),
                    unfocusedBackground = LauncherColors.ButtonBackground,
                    glow = LauncherSpacing.Small,
                )
                .onFocusChanged { focused = it.isFocused }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = LauncherSpacing.Medium, vertical = LauncherSpacing.Small),
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painterResource(R.drawable.ic_play_store), contentDescription = null,
                modifier = Modifier.size(LauncherSpacing.StoreIconSize))
            Text(stringResource(R.string.store_open), color = headingColor, fontSize = 14.sp)
        }
    }
}
