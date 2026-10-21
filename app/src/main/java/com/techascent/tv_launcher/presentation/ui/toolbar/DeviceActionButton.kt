package com.techascent.tv_launcher.presentation.ui.toolbar

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
fun DeviceActionButton(
    label: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .defaultMinSize(minWidth = LauncherSpacing.ButtonSize)
            .height(LauncherSpacing.ButtonSize)
            .semantics { contentDescription = label },
        colors = ButtonDefaults.colors(
            containerColor = LauncherColors.ButtonBackground,
            contentColor = LauncherColors.TextPrimary,
            focusedContainerColor = LauncherColors.TileFocusedBackground,
            focusedContentColor = LauncherColors.TextPrimary,
        ),
        glow = ButtonDefaults.glow(
            focusedGlow = Glow(LauncherColors.FocusGlow, LauncherSpacing.TileFocusGlow),
        ),
        border = ButtonDefaults.border(
            focusedBorder = Border(
                BorderStroke(LauncherSpacing.FocusBorderWidth, MaterialTheme.colorScheme.border),
            ),
        ),
        contentPadding = PaddingValues(horizontal = LauncherSpacing.ButtonHorizontalPadding),
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = label,
            modifier = Modifier.size(LauncherSpacing.ButtonIconSize),
        )
        AnimatedVisibility(visible = isFocused) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = LauncherSpacing.ExtraSmall)
                    .widthIn(max = LauncherSpacing.ButtonLabelMaxWidth),
            )
        }
    }
}
