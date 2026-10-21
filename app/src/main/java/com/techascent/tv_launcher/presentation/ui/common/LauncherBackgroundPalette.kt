package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.ui.graphics.Color
import com.techascent.tv_launcher.ui.theme.LauncherColors
import kotlin.random.Random

enum class LauncherBackgroundPalette(
    val top: Color,
    val middle: Color,
    val bottom: Color,
    val glow: Color,
) {
    Navy(
        LauncherColors.BackgroundTop,
        LauncherColors.BackgroundMiddle,
        LauncherColors.BackgroundBottom,
        LauncherColors.BackgroundGlow,
    ),
    Forest(Color(0xFF162D29), Color(0xFF0B1916), Color(0xFF070F0D), Color(0x554BA58A)),
    Plum(Color(0xFF2B1D36), Color(0xFF180F20), Color(0xFF0E0913), Color(0x55916ABA)),
    Slate(Color(0xFF293039), Color(0xFF151B22), Color(0xFF0B1015), Color(0x557C99AF)),
    Ember(Color(0xFF35251F), Color(0xFF1D1511), Color(0xFF110C09), Color(0x55BA8055)),
}

class LauncherBackgroundPicker(private val random: Random = Random.Default) {
    fun pick(): LauncherBackgroundPalette = LauncherBackgroundPalette.entries.random(random)
}
