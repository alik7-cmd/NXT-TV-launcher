package com.techascent.tv_launcher.presentation.ui.favourite

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.presentation.ui.common.TileV2
import com.techascent.tv_launcher.ui.theme.LauncherColors

/** The trailing "Add app to favourite" tile. */
@Composable
fun AddTileV2(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TileV2(
        label = stringResource(R.string.add_favorite_tile),
        onClick = onClick,
        modifier = modifier,
    ) {
        Text("+", color = LauncherColors.TextSecondary, fontSize = 56.sp)
    }
}
