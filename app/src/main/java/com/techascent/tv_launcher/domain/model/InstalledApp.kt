package com.techascent.tv_launcher.domain.model

import android.graphics.Bitmap

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    /** Decoded once in the data layer so the UI never re-decodes icons. */
    val icon: Bitmap?,
    val isRemovable: Boolean,
    val banner: Bitmap? = null,
)
