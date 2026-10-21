package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class LauncherBackgroundPreviewProvider : PreviewParameterProvider<LauncherBackgroundPalette> {
    override val values = LauncherBackgroundPalette.entries.asSequence()
}

@Preview(name = "Launcher backgrounds", widthDp = 960, heightDp = 540)
@Composable
private fun LauncherBackgroundPreview(
    @PreviewParameter(LauncherBackgroundPreviewProvider::class) palette: LauncherBackgroundPalette,
) {
    Box(Modifier.fillMaxSize().launcherBackground(palette))
}
