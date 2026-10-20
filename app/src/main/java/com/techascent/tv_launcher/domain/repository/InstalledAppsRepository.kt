package com.techascent.tv_launcher.domain.repository

import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import kotlinx.coroutines.flow.Flow

interface InstalledAppsRepository {
    /** Emits loading and the resulting list initially and on package changes. */
    fun observeApps(): Flow<InstalledAppsUpdate>
}
