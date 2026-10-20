package com.techascent.tv_launcher.presentation

import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.InstalledApp
import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.repository.FavoriteAppsRepository
import com.techascent.tv_launcher.domain.repository.InstalledAppsRepository
import com.techascent.tv_launcher.domain.usecase.ObserveFavoriteApps
import com.techascent.tv_launcher.domain.usecase.ObserveInstalledApps
import com.techascent.tv_launcher.domain.usecase.ToggleFavoriteApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
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
        observeInstalledApps = ObserveInstalledApps(appsRepository),
        observeFavoriteApps = ObserveFavoriteApps(favorites),
        toggleFavoriteApp = ToggleFavoriteApp(favorites),
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
        assertEquals(listOf("Netflix", "YouTube"), viewModel.content().favorites.map { it.label })
        viewModel.onFavoriteAppPicked(picked)
        assertEquals(listOf("Netflix", "YouTube"), viewModel.content().favorites.map { it.label })
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
        assertEquals(3, viewModel.content().favorites.size)
    }

    @Test
    fun `favourite menu opens app and removal updates available picker apps`() = runTest {
        favorites.state.value = setOf(netflix.packageName)
        val viewModel = viewModel()
        val app = viewModel.content().favorites.single()
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
        assertTrue(viewModel.content().favorites.isEmpty())
        viewModel.onAddFavoriteSelected()
        assertEquals(3, viewModel.uiState.value.favoritePickerApps?.size)
    }

    @Test
    fun `favourites keep app order and are flagged in the full list`() {
        favorites.state.value = setOf(youtube.packageName, chrome.packageName)

        val state = viewModel().content()

        assertFalse(state.isRefreshing)
        assertEquals(listOf("Chrome", "YouTube"), state.favorites.map { it.label })
        assertEquals(listOf(true, false, true), state.apps.map { it.isFavorite })
    }

    @Test
    fun `toggling a favourite from the dialog closes it and updates the dock`() {
        val viewModel = viewModel()
        val netflixState = viewModel.content().apps.first { it.packageName == netflix.packageName }

        viewModel.onAppLongPressed(netflixState)
        assertEquals(netflix.packageName, viewModel.uiState.value.appOptions?.packageName)

        viewModel.onToggleFavoriteSelected(netflixState)
        assertNull(viewModel.uiState.value.appOptions)
        assertEquals(listOf("Netflix"), viewModel.content().favorites.map { it.label })
    }

    @Test
    fun `options dialog closes when its app is uninstalled`() {
        val viewModel = viewModel()
        viewModel.onAppLongPressed(viewModel.content().apps.first { it.packageName == netflix.packageName })

        apps.state.value = InstalledAppsUpdate.Loaded(listOf(chrome, youtube))

        assertNull(viewModel.uiState.value.appOptions)
    }

    @Test
    fun `uninstall is only requested for removable apps`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<LauncherEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effect.toList(effects) }
        val states = viewModel.content().apps

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

        assertEquals(AppsUiState.Error(R.string.apps_load_error), state.appsState)
    }

    @Test
    fun `initial state stays loading until the first list arrives`() {
        val pending = object : InstalledAppsRepository {
            override fun observeApps(): Flow<InstalledAppsUpdate> = MutableSharedFlow()
        }
        assertEquals(AppsUiState.Loading, viewModel(pending).uiState.value.appsState)
    }

    @Test
    fun `empty list is an explicit empty state`() {
        apps.state.value = InstalledAppsUpdate.Loaded(emptyList())
        assertEquals(AppsUiState.Empty, viewModel().uiState.value.appsState)
    }

    @Test
    fun `refresh keeps apps favourites and open dialog visible until the new list arrives`() {
        favorites.state.value = setOf(netflix.packageName)
        val viewModel = viewModel()
        val before = viewModel.content()
        viewModel.onAppLongPressed(before.favorites.single())

        apps.state.value = InstalledAppsUpdate.Loading

        assertTrue(viewModel.content().isRefreshing)
        assertEquals(before.apps, viewModel.content().apps)
        assertEquals(before.favorites, viewModel.content().favorites)
        assertEquals(netflix.packageName, viewModel.uiState.value.appOptions?.packageName)

        favorites.state.value = setOf(chrome.packageName)
        assertEquals(listOf("Chrome"), viewModel.content().favorites.map { it.label })
        assertTrue(viewModel.content().isRefreshing)

        apps.state.value = InstalledAppsUpdate.Loaded(listOf(chrome, youtube))
        assertFalse(viewModel.content().isRefreshing)
        assertEquals(listOf("Chrome", "YouTube"), viewModel.content().apps.map { it.label })
        assertNull(viewModel.uiState.value.appOptions)
    }

    @Test
    fun `refresh failure retains content and reports a non-blocking error`() = runTest {
        val fail = MutableSharedFlow<Unit>()
        val source = object : InstalledAppsRepository {
            override fun observeApps(): Flow<InstalledAppsUpdate> = flow {
                emit(InstalledAppsUpdate.Loaded(listOf(chrome, netflix)))
                emitAll(flow {
                    fail.collect {
                        emit(InstalledAppsUpdate.Loading)
                        error("refresh failed")
                    }
                })
            }
        }
        favorites.state.value = setOf(netflix.packageName)
        val viewModel = viewModel(source)
        fail.emit(Unit)

        val content = viewModel.content()
        assertFalse(content.isRefreshing)
        assertEquals(R.string.apps_load_error, content.refreshErrorResId)
        assertEquals(listOf("Chrome", "Netflix"), content.apps.map { it.label })
        assertEquals(listOf("Netflix"), content.favorites.map { it.label })
        favorites.state.value = setOf(chrome.packageName)
        assertEquals(listOf("Chrome"), viewModel.content().favorites.map { it.label })
    }

    @Test
    fun `successful empty refresh removes old content`() {
        val viewModel = viewModel()
        apps.state.value = InstalledAppsUpdate.Loading
        assertTrue(viewModel.content().isRefreshing)
        apps.state.value = InstalledAppsUpdate.Loaded(emptyList())
        assertEquals(AppsUiState.Empty, viewModel.uiState.value.appsState)
    }

    private fun LauncherViewModel.content() = uiState.value.appsState as AppsUiState.Content

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
