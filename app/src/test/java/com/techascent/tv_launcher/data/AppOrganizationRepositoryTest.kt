package com.techascent.tv_launcher.data

import com.techascent.tv_launcher.data.repository.AppOrganizationRepositoryImpl
import com.techascent.tv_launcher.data.repository.RecentAppsRepositoryImpl
import com.techascent.tv_launcher.domain.model.AppOrganization
import com.techascent.tv_launcher.domain.model.AppSortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppOrganizationRepositoryTest {
    @Test
    fun `preferences survive repository recreation and emit changes`() = runTest {
        val preferences = MemoryPreferences().preferences
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = AppOrganizationRepositoryImpl(preferences, dispatcher)
        val emissions = mutableListOf<AppOrganization>()
        backgroundScope.launch(dispatcher) { repository.observeOrganization().toList(emissions) }
        runCurrent()
        assertEquals(AppOrganization(), emissions.last())

        repository.togglePin("beta")
        repository.togglePin("alpha")
        repository.setHidden("beta", true)
        repository.setSortOrder(AppSortOrder.RecentlyUsed)
        runCurrent()

        val expected = AppOrganization(listOf("beta", "alpha"), setOf("beta"), AppSortOrder.RecentlyUsed)
        assertEquals(expected, emissions.last())
        val recreated = AppOrganizationRepositoryImpl(preferences, dispatcher)
        assertEquals(expected, recreated.observeOrganization().first())

        recreated.setHidden("beta", false)
        recreated.togglePin("beta")
        recreated.togglePin("beta")
        assertEquals(
            expected.copy(pinnedPackages = listOf("alpha", "beta"), hiddenPackages = emptySet()),
            recreated.observeOrganization().first(),
        )
    }

    @Test
    fun `unknown saved sort falls back and ordered pins are deduplicated`() = runTest {
        val preferences = MemoryPreferences().preferences
        preferences.edit().putString("app_sort_order", "future-value")
            .putString("pinned_packages", "beta,,alpha,beta").apply()
        val repository = AppOrganizationRepositoryImpl(preferences, UnconfinedTestDispatcher(testScheduler))
        assertEquals(
            AppOrganization(pinnedPackages = listOf("beta", "alpha")),
            repository.observeOrganization().first(),
        )
    }

    @Test
    fun `serialized preference updates preserve all pins and have no limit`() = runTest {
        val repository = AppOrganizationRepositoryImpl(MemoryPreferences().preferences, UnconfinedTestDispatcher(testScheduler))
        (1..30).map { launch { repository.togglePin("app$it") } }.forEach { it.join() }
        val saved = repository.observeOrganization().first()
        assertEquals((1..30).map { "app$it" }, saved.pinnedPackages)
        repository.setHidden("app20", true)
        repository.setHidden("app20", false)
        assertFalse("app20" in repository.observeOrganization().first().hiddenPackages)
        assertEquals(saved.pinnedPackages, repository.observeOrganization().first().pinnedPackages)
    }

    @Test
    fun `existing recent history is retained and extended beyond ten launches`() = runTest {
        val preferences = MemoryPreferences().preferences
        preferences.edit().putString("recent_packages", (10 downTo 1).joinToString(",") { "app$it" }).apply()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = RecentAppsRepositoryImpl(preferences, dispatcher)
        (11..25).forEach { repository.recordLaunch("app$it") }
        repository.recordLaunch("app3")

        val history = RecentAppsRepositoryImpl(preferences, dispatcher).observeRecentApps().first()
        assertEquals("app3", history.first())
        assertEquals(25, history.size)
        assertEquals(25, history.distinct().size)
        assertTrue("app1" in history)
        assertEquals("app25", history[1])
    }
}
