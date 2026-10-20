package com.techascent.tv_launcher.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/** Round icon button whose label slides out while focused. */
@Composable
fun DeviceActionButton(
    label: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(LauncherSpacing.ButtonCornerRadius)
    Row(
        modifier = Modifier
            .heightIn(min = LauncherSpacing.ButtonSize)
            .widthIn(min = LauncherSpacing.ButtonSize)
            .focusHighlight(
                isFocused = isFocused,
                shape = shape,
                unfocusedBackground = LauncherColors.ButtonBackground,
                glow = LauncherSpacing.Small,
            )
            .clip(shape)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(onClick = onClick)
            .semantics { contentDescription = label }
            .padding(horizontal = LauncherSpacing.ButtonHorizontalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(iconResId),
            contentDescription = null,
            colorFilter = ColorFilter.tint(LauncherColors.TextPrimary),
            modifier = Modifier.size(LauncherSpacing.ButtonIconSize),
        )
        AnimatedVisibility(visible = isFocused) {
            Text(
                text = label,
                color = LauncherColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = LauncherSpacing.Small)
                    .widthIn(max = LauncherSpacing.ButtonLabelMaxWidth),
            )
        }
    }
}
