package com.techascent.tv_launcher.presentation

import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.InstalledApp
import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.repository.FavoriteAppsRepository
import com.techascent.tv_launcher.domain.repository.InstalledAppsRepository
import com.techascent.tv_launcher.domain.usecase.ObserveLauncherApps
import com.techascent.tv_launcher.domain.usecase.ToggleFavoriteApp
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class LauncherViewModelTest {
    private val chrome = app("com.android.chrome", "Chrome", isRemovable = false)
    private val netflix = app("com.netflix", "Netflix", isRemovable = true)
    private val youtube = app("com.youtube", "YouTube", isRemovable = true)

    private val apps = FakeInstalledAppsRepository(listOf(chrome, netflix, youtube))
    private val favorites = FakeFavoriteAppsRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(appsRepository: InstalledAppsRepository = apps) = LauncherViewModel(
        observeLauncherApps = ObserveLauncherApps(appsRepository, favorites),
        toggleFavoriteApp = ToggleFavoriteApp(favorites),
        clock = flowOf(ZonedDateTime.parse("2026-10-22T09:00:00+02:00")),
    )

    @Test
    fun `picker lists only apps not already favourited and selecting closes it`() {
        favorites.state.value = setOf(netflix.packageName)
        val viewModel = viewModel()

        viewModel.onAddFavoriteSelected()
        assertEquals(listOf("Chrome", "YouTube"), viewModel.uiState.value.favoritePickerApps?.map { it.label })
        val picked = viewModel.uiState.value.favoritePickerApps!!.last()
        viewModel.onFavoriteAppPicked(picked)

        assertNull(viewModel.uiState.value.favoritePickerApps)
        assertEquals(listOf("Netflix", "YouTube"), viewModel.favorites().map { it.label })
        viewModel.onFavoriteAppPicked(picked)
        assertEquals(listOf("Netflix", "YouTube"), viewModel.favorites().map { it.label })
        viewModel.onAddFavoriteSelected()
        assertEquals(listOf("Chrome"), viewModel.uiState.value.favoritePickerApps?.map { it.label })
    }

    @Test
    fun `picker handles no favourites all favourites and cancellation`() {
        val viewModel = viewModel()
        viewModel.onAddFavoriteSelected()
        assertEquals(3, viewModel.uiState.value.favoritePickerApps?.size)

        favorites.state.value = setOf(chrome.packageName, netflix.packageName, youtube.packageName)
        assertEquals(emptyList<LauncherAppUiState>(), viewModel.uiState.value.favoritePickerApps)
        viewModel.onFavoritePickerDismissed()
        assertNull(viewModel.uiState.value.favoritePickerApps)
        assertEquals(3, viewModel.favorites().size)
    }

    @Test
    fun `favourite menu opens app and removal updates available picker apps`() = runTest {
        favorites.state.value = setOf(netflix.packageName)
        val viewModel = viewModel()
        val app = viewModel.favorites().single()
        val effects = mutableListOf<LauncherEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effect.toList(effects) }

        viewModel.onFavoriteLongPressed(app)
        assertTrue(viewModel.uiState.value.favoriteOptions)
        viewModel.onAppSelected(app)
        assertNull(viewModel.uiState.value.appOptions)
        assertEquals(listOf(LauncherEffect.LaunchApp(app.packageName, app.activityName)), effects)

        viewModel.onAppLongPressed(app)
        assertFalse(viewModel.uiState.value.favoriteOptions)
        viewModel.onFavoriteLongPressed(app)
        viewModel.onToggleFavoriteSelected(app)
        assertTrue(viewModel.favorites().isEmpty())
        viewModel.onAddFavoriteSelected()
        assertEquals(3, viewModel.uiState.value.favoritePickerApps?.size)
    }

    @Test
    fun `favourites keep app order and are flagged in the full list`() {
        favorites.state.value = setOf(youtube.packageName, chrome.packageName)

        val viewModel = viewModel()

        assertEquals(listOf("Chrome", "YouTube"), viewModel.favorites().map { it.label })
        assertEquals(listOf(true, false, true), viewModel.allApps().map { it.isFavorite })
    }

    @Test
    fun `toggling a favourite from the dialog closes it and updates the dock`() {
        val viewModel = viewModel()
        val netflixState = viewModel.allApps().first { it.packageName == netflix.packageName }

        viewModel.onAppLongPressed(netflixState)
        assertEquals(netflix.packageName, viewModel.uiState.value.appOptions?.packageName)

        viewModel.onToggleFavoriteSelected(netflixState)
        assertNull(viewModel.uiState.value.appOptions)
        assertEquals(listOf("Netflix"), viewModel.favorites().map { it.label })
    }

    @Test
    fun `options dialog closes when its app is uninstalled`() {
        val viewModel = viewModel()
        viewModel.onAppLongPressed(viewModel.allApps().first { it.packageName == netflix.packageName })

        apps.state.value = InstalledAppsUpdate.Loaded(listOf(chrome, youtube))

        assertNull(viewModel.uiState.value.appOptions)
    }

    @Test
    fun `uninstall is only requested for removable apps`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<LauncherEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effect.toList(effects) }
        val states = viewModel.allApps()

        viewModel.onUninstallSelected(states.first { it.packageName == chrome.packageName })
        viewModel.onUninstallSelected(states.first { it.packageName == netflix.packageName })

        assertEquals(listOf(LauncherEffect.UninstallApp(netflix.packageName)), effects)
    }

    @Test
    fun `failing app source shows an error`() {
        val failing = object : InstalledAppsRepository {
            override fun observeApps(): Flow<InstalledAppsUpdate> = flow { error("boom") }
        }

        val state = viewModel(failing).uiState.value

        assertEquals(UiState.Error(R.string.apps_load_error), state.allApps)
        assertEquals(UiState.Error(R.string.apps_load_error), state.favorites)
    }

    @Test
    fun `initial state stays loading until the first list arrives`() {
        val pending = object : InstalledAppsRepository {
            override fun observeApps(): Flow<InstalledAppsUpdate> = MutableSharedFlow()
        }
        val state = viewModel(pending).uiState.value
        assertEquals(UiState.Loading, state.allApps)
        assertEquals(UiState.Loading, state.favorites)
    }

    @Test
    fun `empty list is a successful empty state`() {
        apps.state.value = InstalledAppsUpdate.Loaded(emptyList())
        assertEquals(UiState.Success(emptyList<LauncherAppUiState>()), viewModel().uiState.value.allApps)
    }

    @Test
    fun `refresh keeps apps favourites and open dialog visible until the new list arrives`() {
        favorites.state.value = setOf(netflix.packageName)
        val viewModel = viewModel()
        val before = viewModel.uiState.value
        viewModel.onAppLongPressed(viewModel.favorites().single())

        apps.state.value = InstalledAppsUpdate.Loading

        assertEquals(before.allApps, viewModel.uiState.value.allApps)
        assertEquals(before.favorites, viewModel.uiState.value.favorites)
        assertEquals(netflix.packageName, viewModel.uiState.value.appOptions?.packageName)

        apps.state.value = InstalledAppsUpdate.Loaded(listOf(chrome, youtube))
        assertEquals(listOf("Chrome", "YouTube"), viewModel.allApps().map { it.label })
        assertNull(viewModel.uiState.value.appOptions)
    }

    @Test
    fun `refresh failure keeps the previous apps and favourites updates`() = runTest {
        val fail = MutableSharedFlow<Unit>()
        val source = object : InstalledAppsRepository {
            override fun observeApps(): Flow<InstalledAppsUpdate> = flow {
                emit(InstalledAppsUpdate.Loaded(listOf(chrome, netflix)))
                fail.collect { error("refresh failed") }
            }
        }
        favorites.state.value = setOf(netflix.packageName)
        val viewModel = viewModel(source)
        fail.emit(Unit)

        assertEquals(listOf("Chrome", "Netflix"), viewModel.allApps().map { it.label })
        favorites.state.value = setOf(chrome.packageName)
        assertEquals(listOf("Chrome"), viewModel.favorites().map { it.label })
    }

    @Test
    fun `successful empty refresh removes old content`() {
        val viewModel = viewModel()
        apps.state.value = InstalledAppsUpdate.Loaded(emptyList())
        assertEquals(UiState.Success(emptyList<LauncherAppUiState>()), viewModel.uiState.value.favorites)
        assertEquals(UiState.Success(emptyList<LauncherAppUiState>()), viewModel.uiState.value.allApps)
    }

    @Test
    fun `header follows the clock`() {
        val header = (viewModel().uiState.value.header as UiState.Success).data
        assertEquals(9, header.dateTime.hour)
    }

    private fun LauncherViewModel.allApps() = (uiState.value.allApps as UiState.Success).data

    private fun LauncherViewModel.favorites() = (uiState.value.favorites as UiState.Success).data

    private fun app(packageName: String, label: String, isRemovable: Boolean) = InstalledApp(
        packageName = packageName,
        activityName = "$packageName.Main",
        label = label,
        icon = null,
        isRemovable = isRemovable,
    )

    private class FakeInstalledAppsRepository(initial: List<InstalledApp>) : InstalledAppsRepository {
        val state = MutableStateFlow<InstalledAppsUpdate>(InstalledAppsUpdate.Loaded(initial))
        override fun observeApps(): Flow<InstalledAppsUpdate> = state
    }

    private class FakeFavoriteAppsRepository : FavoriteAppsRepository {
        val state = MutableStateFlow(emptySet<String>())
        override fun observeFavorites(): Flow<Set<String>> = state
        override suspend fun toggleFavorite(packageName: String) {
            state.value = state.value.let { if (packageName in it) it - packageName else it + packageName }
        }
    }
}
