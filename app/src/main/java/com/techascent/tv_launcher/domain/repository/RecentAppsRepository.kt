package com.techascent.tv_launcher.domain.repository

import kotlinx.coroutines.flow.Flow

/** Local launch history for Recently used sorting: all distinct packages, most recent first. */
interface RecentAppsRepository {
    fun observeRecentApps(): Flow<List<String>>
    suspend fun recordLaunch(packageName: String)
}
