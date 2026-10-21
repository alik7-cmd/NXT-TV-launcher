package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.techascent.tv_launcher.ui.theme.LauncherColors

@Composable
internal fun SectionTitle(text: String) {
    Text(text, color = LauncherColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
}
