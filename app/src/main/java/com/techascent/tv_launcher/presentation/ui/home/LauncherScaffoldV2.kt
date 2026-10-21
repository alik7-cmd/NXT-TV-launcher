package com.techascent.tv_launcher.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** tv-material has no Scaffold: [topBar] sits above [content], which fills the rest. */
@Composable
fun LauncherScaffoldV2(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth()) { topBar() }
        Column(Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}
