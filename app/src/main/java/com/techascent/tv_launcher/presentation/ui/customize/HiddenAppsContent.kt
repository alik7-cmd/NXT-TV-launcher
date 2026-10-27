package com.techascent.tv_launcher.presentation.ui.customize

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
internal fun HiddenAppsContent(
    apps: List<LauncherAppUiState>,
    onRestoreApp: (LauncherAppUiState) -> Unit,
    onBack: () -> Unit,
) {
    val backFocus = remember { FocusRequester() }
    val appFocus = remember { FocusRequester() }
    val listState = rememberLazyListState()
    var restoreIndex by remember { mutableIntStateOf(0) }
    val focusIndex = restoreIndex.coerceIn(0, apps.lastIndex.coerceAtLeast(0))
    val shape = RoundedCornerShape(LauncherSpacing.DialogCornerRadius)
    Column(
        modifier = Modifier.width(LauncherSpacing.DialogWidth)
            .heightIn(max = 440.dp)
            .clip(shape)
            .background(LauncherColors.DialogBackground)
            .border(1.dp, LauncherColors.DialogBorder, shape)
            .padding(LauncherSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Medium),
    ) {
        Text(
            stringResource(R.string.manage_hidden_apps),
            color = LauncherColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(if (apps.isEmpty()) R.string.hidden_apps_empty else R.string.hidden_apps_hint),
            color = LauncherColors.TextSecondary,
            fontSize = 14.sp,
        )
        if (apps.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(LauncherSpacing.Small),
                verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
            ) {
                itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                    Surface(
                        onClick = {
                            restoreIndex = index
                            onRestoreApp(app)
                        },
                        modifier = Modifier.fillMaxWidth().then(
                            if (index == focusIndex) Modifier.focusRequester(appFocus) else Modifier,
                        ),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LauncherSpacing.Medium)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
                        colors = optionColors(),
                    ) {
                        Column(Modifier.padding(LauncherSpacing.Medium)) {
                            Text(stringResource(R.string.restore_app, app.label), fontSize = 16.sp)
                            Text(app.packageName, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Surface(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().focusRequester(backFocus),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LauncherSpacing.Medium)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
            colors = optionColors(),
        ) {
            Text(
                stringResource(R.string.wallpaper_back),
                fontSize = 16.sp,
                modifier = Modifier.padding(LauncherSpacing.Medium),
            )
        }
    }
    // Keep the next restore action reachable after its focused predecessor is removed.
    LaunchedEffect(apps) {
        if (apps.isEmpty()) {
            backFocus.requestFocus()
        } else {
            listState.scrollToItem(focusIndex)
            appFocus.requestFocus()
        }
    }
}
