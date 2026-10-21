package com.techascent.tv_launcher.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.techascent.tv_launcher.domain.model.AppOrganization
import com.techascent.tv_launcher.domain.model.AppSortOrder
import com.techascent.tv_launcher.domain.repository.AppOrganizationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AppOrganizationRepositoryImpl internal constructor(
    private val preferences: SharedPreferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AppOrganizationRepository {
    constructor(context: Context) : this(
        context.getSharedPreferences("launcher_preferences", Context.MODE_PRIVATE),
    )

    private val mutex = Mutex()

    override fun observeOrganization() = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || key in KEYS) trySend(read())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(read())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged().flowOn(dispatcher)

    override suspend fun togglePin(packageName: String) = update { it.togglePin(packageName) }

    override suspend fun setHidden(packageName: String, hidden: Boolean) = update {
        it.copy(hiddenPackages = if (hidden) it.hiddenPackages + packageName else it.hiddenPackages - packageName)
    }

    override suspend fun setSortOrder(order: AppSortOrder) = update { it.copy(sortOrder = order) }

    private suspend fun update(transform: (AppOrganization) -> AppOrganization) = withContext(dispatcher) {
        mutex.withLock {
            val updated = transform(read())
            preferences.edit {
                putString(KEY_PINNED, updated.pinnedPackages.joinToString(","))
                putStringSet(KEY_HIDDEN, updated.hiddenPackages)
                putString(KEY_SORT, updated.sortOrder.name)
            }
        }
    }

    private fun read() = AppOrganization(
        pinnedPackages = preferences.getString(KEY_PINNED, null).orEmpty()
            .split(",").filter { it.isNotEmpty() }.distinct(),
        hiddenPackages = preferences.getStringSet(KEY_HIDDEN, emptySet()).orEmpty().toSet(),
        sortOrder = AppSortOrder.entries.firstOrNull { it.name == preferences.getString(KEY_SORT, null) }
            ?: AppSortOrder.Alphabetical,
    )

    private companion object {
        const val KEY_PINNED = "pinned_packages"
        const val KEY_HIDDEN = "hidden_packages"
        const val KEY_SORT = "app_sort_order"
        val KEYS = setOf(KEY_PINNED, KEY_HIDDEN, KEY_SORT)
    }
}
