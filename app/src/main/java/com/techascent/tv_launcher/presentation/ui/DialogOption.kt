package com.techascent.tv_launcher.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
internal fun DialogOption(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(LauncherSpacing.ButtonCornerRadius)
    Text(
        text = text,
        color = LauncherColors.TextPrimary,
        fontSize = 18.sp,
        fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isFocused) LauncherColors.TileFocusedBackground else Color.Transparent,
                shape,
            )
            .border(
                width = LauncherSpacing.FocusBorderWidth,
                color = if (isFocused) LauncherColors.FocusBorder else Color.Transparent,
                shape = shape,
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(
                horizontal = LauncherSpacing.Medium,
                vertical = LauncherSpacing.DialogOptionVerticalPadding,
            ),
    )
}
