package com.techascent.tv_launcher.presentation.ui.common

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.LauncherWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalResources

@DrawableRes
internal fun LauncherWallpaper.thumbnailRes(): Int = when (this) {
    LauncherWallpaper.One -> R.drawable.launcher_thumbnail_1
    LauncherWallpaper.Two -> R.drawable.launcher_thumbnail_2
    LauncherWallpaper.Three -> R.drawable.launcher_thumbnail_3
}

@DrawableRes
private fun LauncherWallpaper.fullRes(): Int = when (this) {
    LauncherWallpaper.One -> R.drawable.launcher_background_1
    LauncherWallpaper.Two -> R.drawable.launcher_background_2
    LauncherWallpaper.Three -> R.drawable.launcher_background_3
}

/** Decodes off the UI thread, sampling for the actual display size, not its dp density. */
@Composable
internal fun WallpaperBackground(wallpaper: LauncherWallpaper) {
    if (LocalInspectionMode.current) {
        Image(painterResource(wallpaper.thumbnailRes()), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        return
    }
    val resources = LocalResources.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.coerceAtLeast(1)
        val height = constraints.maxHeight.coerceAtLeast(1)
        // produceState retains the previous image until its replacement has finished decoding.
        val bitmap by produceState<ImageBitmap?>(null, wallpaper, width, height) {
            value = withContext(Dispatchers.IO) {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(resources, wallpaper.fullRes(), options)
                var sample = 1
                while (options.outWidth / (sample * 2) >= width &&
                    options.outHeight / (sample * 2) >= height
                ) {
                    sample *= 2
                }
                options.inJustDecodeBounds = false
                options.inSampleSize = sample
                options.inScaled = false
                requireNotNull(BitmapFactory.decodeResource(resources, wallpaper.fullRes(), options)) {
                    "Unable to decode wallpaper $wallpaper"
                }.asImageBitmap()
            }
        }
        bitmap?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
    }
}
