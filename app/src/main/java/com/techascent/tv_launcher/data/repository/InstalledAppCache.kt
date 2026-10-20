package com.techascent.tv_launcher.data.repository

import com.techascent.tv_launcher.domain.model.InstalledApp

/** Scoped to one app-list collector; bitmaps still in use by the UI must never be recycled. */
internal class InstalledAppCache {
    private val apps = mutableMapOf<Pair<String, String>, InstalledApp>()

    fun getOrLoad(packageName: String, activityName: String, load: () -> InstalledApp): InstalledApp =
        apps.getOrPut(packageName to activityName, load)

    fun invalidate(packageName: String) {
        apps.keys.removeAll { it.first == packageName }
    }

    fun retain(activities: Set<Pair<String, String>>) {
        apps.keys.retainAll(activities)
    }

    fun clear() = apps.clear()
}
