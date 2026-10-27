package com.techascent.tv_launcher.domain.repository

import com.techascent.tv_launcher.domain.model.AppOrganization
import com.techascent.tv_launcher.domain.model.AppSortOrder
import kotlinx.coroutines.flow.Flow

interface AppOrganizationRepository {
    fun observeOrganization(): Flow<AppOrganization>
    suspend fun togglePin(packageName: String)
    suspend fun setHidden(packageName: String, hidden: Boolean)
    suspend fun setSortOrder(order: AppSortOrder)
}
