package com.techascent.tv_launcher.presentation.ui.toolbar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

/** Plain icon that only shows a round background while focused. */
@Composable
fun IconActionButton(
    label: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(LauncherSpacing.ButtonSize),
        colors = IconButtonDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = LauncherColors.TextPrimary,
            focusedContainerColor = LauncherColors.TextPrimary,
            focusedContentColor = Color.Black,
        ),
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = label,
            modifier = Modifier.size(LauncherSpacing.ButtonIconSize),
        )
    }
}
