package com.techascent.tv_launcher.domain.repository

import kotlinx.coroutines.flow.Flow

/** All distinct packages launched from the launcher, most recent first (not just the recent row). */
interface RecentAppsRepository {
    fun observeRecentApps(): Flow<List<String>>
    suspend fun recordLaunch(packageName: String)
}
