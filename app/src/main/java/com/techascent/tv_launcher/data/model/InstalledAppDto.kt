package com.techascent.tv_launcher.data.model

import android.graphics.drawable.Drawable
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
        icon = icon.toBitmap(width = ICON_SIZE_PX, height = ICON_SIZE_PX),
        isRemovable = isRemovable,
        banner = banner?.toBitmap(width = 320, height = 180),
    )

    private companion object {
        const val ICON_SIZE_PX = 144
    }
}
