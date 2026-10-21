package com.techascent.tv_launcher.presentation.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.techascent.tv_launcher.presentation.ui.common.rememberShimmerProgress
import com.techascent.tv_launcher.presentation.ui.common.shimmer
import com.techascent.tv_launcher.ui.theme.LauncherColors
import com.techascent.tv_launcher.ui.theme.LauncherSpacing

@Composable
internal fun WeatherLoading(modifier: Modifier = Modifier, compact: Boolean = false) {
    val progress = rememberShimmerProgress()
    if (compact) {
        Row(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.size(40.dp).shimmer(progress))
            Column(verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small)) {
                Spacer(Modifier.width(100.dp).height(20.dp).shimmer(progress))
                Spacer(Modifier.width(200.dp).height(14.dp).shimmer(progress))
            }
        }
    } else {
        Row(
            modifier.fillMaxWidth()
                .background(LauncherColors.DialogBackground.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(horizontal = LauncherSpacing.Large, vertical = LauncherSpacing.Small),
            horizontalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
        ) {
            repeat(5) {
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(LauncherSpacing.Small),
                ) {
                    Spacer(Modifier.width(64.dp).height(14.dp).shimmer(progress))
                    Spacer(Modifier.size(24.dp).shimmer(progress))
                    Spacer(Modifier.width(56.dp).height(18.dp).shimmer(progress))
                    Spacer(Modifier.width(48.dp).height(12.dp).shimmer(progress))
                }
            }
        }
    }
}
