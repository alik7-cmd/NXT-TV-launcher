package com.techascent.tv_launcher.data.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import com.techascent.tv_launcher.domain.model.InstalledApp

data class InstalledAppDto(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable,
    val isRemovable: Boolean,
    val banner: Drawable? = null,
) {
    fun toDomain() = InstalledApp(
        packageName = packageName,
        activityName = activityName,
        label = label,
        icon = icon.toFullBleedBitmap(ICON_SIZE_PX),
        isRemovable = isRemovable,
        banner = banner?.toBitmap(width = 320, height = 180),
    )

    private companion object {
        const val ICON_SIZE_PX = 192
    }
}

/**
 * Adaptive icons are drawn without the system mask so the artwork fills the whole square and the
 * UI can clip it to any shape. Layers are 108dp with a 72dp visible area, so they are inset by a
 * quarter on each side like [AdaptiveIconDrawable] does. Legacy icons are drawn as they are.
 */
private fun Drawable.toFullBleedBitmap(size: Int): Bitmap {
    if (this !is AdaptiveIconDrawable) return toBitmap(size, size)
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val inset = size / 4
    listOfNotNull(background, foreground).forEach { layer ->
        layer.setBounds(-inset, -inset, size + inset, size + inset)
        layer.draw(canvas)
    }
    return bitmap
}
