package com.techascent.tv_launcher.presentation.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

/** Widens the content by [amount] on both sides so a row's focus padding doesn't indent its items. */
internal fun Modifier.bleed(amount: Dp) = layout { measurable, constraints ->
    val extra = amount.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra * 2))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra, 0) }
}
