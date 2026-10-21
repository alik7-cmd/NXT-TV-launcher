package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.model.InstalledApp
import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.model.LauncherAppsResult
import com.techascent.tv_launcher.domain.repository.FavoriteAppsRepository
import com.techascent.tv_launcher.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveLauncherAppsTest {
    private val app = InstalledApp("example.app", "Main", "App", null, true)
    private val favorites = MutableStateFlow(setOf(app.packageName))
    private val favoriteRepository = object : FavoriteAppsRepository {
        override fun observeFavorites(): Flow<Set<String>> = favorites
        override suspend fun toggleFavorite(packageName: String) {
            favorites.value = favorites.value - packageName
        }
    }

    private fun useCase(source: Flow<InstalledAppsUpdate>) = ObserveLauncherApps(
        object : InstalledAppsRepository {
            override fun observeApps() = source
        },
        favoriteRepository,
    )

    @Test
    fun `starts with init`() = runTest {
        assertEquals(LauncherAppsResult.Init, useCase(flowOf(InstalledAppsUpdate.Loading))().first())
    }

    @Test
    fun `success contains domain apps and favourite packages`() = runTest {
        val result = useCase(flowOf(InstalledAppsUpdate.Loaded(listOf(app))))()
            .first { it is LauncherAppsResult.Success } as LauncherAppsResult.Success

        assertEquals(listOf(app), result.apps)
        assertEquals(setOf(app.packageName), result.favoritePackages)
    }

    @Test
    fun `source failure returns its cause`() = runTest {
        val failure = IllegalStateException("apps unavailable")
        val result = useCase(flow { throw failure })()
            .first { it is LauncherAppsResult.Error } as LauncherAppsResult.Error

        assertSame(failure, result.cause)
        assertEquals(null, result.previous)
    }

    @Test
    fun `refresh keeps apps and favourite updates including after failure`() = runTest {
        val fail = MutableStateFlow(false)
        val results = mutableListOf<LauncherAppsResult>()
        val source = flow {
            emit(InstalledAppsUpdate.Loaded(listOf(app)))
            fail.collect {
                if (it) {
                    emit(InstalledAppsUpdate.Loading)
                    error("refresh failed")
                }
            }
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase(source)().collect { results.add(it) }
        }
        runCurrent()
        fail.value = true
        runCurrent()
        assertTrue(results.last() is LauncherAppsResult.Error)
        assertEquals(listOf(app), (results.last() as LauncherAppsResult.Error).previous?.apps)

        favorites.value = emptySet()
        runCurrent()
        assertEquals(emptySet<String>(), (results.last() as LauncherAppsResult.Error).previous?.favoritePackages)
    }
}
