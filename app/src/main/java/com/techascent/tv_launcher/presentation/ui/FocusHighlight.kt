package com.techascent.tv_launcher.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/**
 * Card background, border and blue glow that fade in only while focused, so unfocused items
 * stay visually quiet.
 */
@Composable
fun Modifier.focusHighlight(
    isFocused: Boolean,
    shape: Shape,
    unfocusedBackground: Color = Color.Transparent,
    glow: Dp = LauncherSpacing.TileFocusGlow,
): Modifier {
    val background by animateColorAsState(
        targetValue = if (isFocused) LauncherColors.TileFocusedBackground else unfocusedBackground,
        label = "focus background",
    )
    val border by animateColorAsState(
        targetValue = if (isFocused) LauncherColors.FocusBorder else Color.Transparent,
        label = "focus border",
    )
    return this
        .shadow(
            elevation = if (isFocused) glow else 0.dp,
            shape = shape,
            ambientColor = LauncherColors.FocusGlow,
            spotColor = LauncherColors.FocusGlow,
            clip = false,
        )
        .background(background, shape)
        .border(LauncherSpacing.FocusBorderWidth, border, shape)
}
