package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.model.InstalledApp
import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.model.LauncherAppsResult
import com.techascent.tv_launcher.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ObserveLauncherAppsTest {
    private val app = InstalledApp("example.app", "Main", "App", null, true)

    private fun useCase(source: Flow<InstalledAppsUpdate>) = ObserveLauncherApps(
        object : InstalledAppsRepository {
            override fun observeApps() = source
        },
    )

    @Test
    fun `starts with init`() = runTest {
        assertEquals(LauncherAppsResult.Init, useCase(flowOf(InstalledAppsUpdate.Loading))().first())
    }

    @Test
    fun `success contains installed apps`() = runTest {
        val result = useCase(flowOf(InstalledAppsUpdate.Loaded(listOf(app))))()
            .first { it is LauncherAppsResult.Success } as LauncherAppsResult.Success

        assertEquals(listOf(app), result.apps)
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
    fun `refresh loading keeps previous apps and failure carries them forward`() = runTest {
        val failure = IllegalStateException("refresh failed")
        val results = useCase(flow {
            emit(InstalledAppsUpdate.Loaded(listOf(app)))
            emit(InstalledAppsUpdate.Loading)
            throw failure
        })().toList(mutableListOf())

        assertEquals(LauncherAppsResult.Init, results.first())
        assertEquals(listOf(app), (results[2] as LauncherAppsResult.Success).apps)
        assertEquals(listOf(app), (results.last() as LauncherAppsResult.Error).previous?.apps)
        assertSame(failure, (results.last() as LauncherAppsResult.Error).cause)
    }
}
