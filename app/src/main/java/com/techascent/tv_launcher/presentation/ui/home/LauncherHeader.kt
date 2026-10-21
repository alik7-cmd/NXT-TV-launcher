package com.techascent.tv_launcher.presentation.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.LauncherHeaderUiState
import com.techascent.tv_launcher.presentation.state.UiState
import com.techascent.tv_launcher.presentation.ui.toolbar.IconActionButton
import com.techascent.tv_launcher.presentation.ui.toolbar.rememberClockText
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
internal fun LauncherHeader(
    visible: Boolean,
    header: LauncherHeaderUiState?,
    pinnedApps: List<LauncherAppUiState>,
    circular: Boolean,
    tileWidth: Dp,
    showAppNames: Boolean,
    onAppClick: (LauncherAppUiState) -> Unit,
    onAppLongClick: (LauncherAppUiState) -> Unit,
    onInputClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCustomizeClick: () -> Unit,
    topBarFocus: FocusRequester,
    pinnedFocus: FocusRequester,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column(
            modifier = Modifier.padding(
                start = LauncherSpacing.ScreenHorizontal,
                end = LauncherSpacing.ScreenHorizontal,
                top = LauncherSpacing.Large,
            ),
            verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
        ) {
            TopBar(
                header = header,
                onInputClick = onInputClick,
                onSettingsClick = onSettingsClick,
                onCustomizeClick = onCustomizeClick,
                modifier = Modifier.focusRequester(topBarFocus).focusRestorer(),
            )
            RecentApps(
                state = UiState.Success(pinnedApps),
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                modifier = Modifier.focusRequester(pinnedFocus).focusRestorer(),
                circular = circular,
                tileWidth = tileWidth,
                showAppNames = showAppNames,
                heading = R.string.pinned_apps_heading,
            )
        }
    }
}

@Composable
private fun TopBar(
    header: LauncherHeaderUiState?,
    onInputClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCustomizeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier.background(color = Color.Black.copy(alpha = 0.25f), shape = RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.ExtraSmall),
        ) {
            IconActionButton(stringResource(R.string.customize), R.drawable.ic_customize, onCustomizeClick)
            IconActionButton(stringResource(R.string.inputs), R.drawable.ic_input, onInputClick)
            IconActionButton(stringResource(R.string.settings), R.drawable.ic_settings, onSettingsClick)
        }
        if (header != null) {
            val clock = rememberClockText(header.dateTime)
            Text(
                text = listOfNotNull(clock.time, clock.period).joinToString(" "),
                color = LauncherColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
