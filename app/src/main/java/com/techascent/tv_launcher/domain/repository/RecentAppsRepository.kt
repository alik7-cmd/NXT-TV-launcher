package com.techascent.tv_launcher.domain.repository

import kotlinx.coroutines.flow.Flow

/** Apps launched from the launcher, most recent first. */
interface RecentAppsRepository {
    fun observeRecentApps(): Flow<List<String>>
    suspend fun recordLaunch(packageName: String)
}
