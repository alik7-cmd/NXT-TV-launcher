package com.techascent.tv_launcher.presentation

import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.domain.model.AppStyle
import com.techascent.tv_launcher.domain.model.AppOrganization
import com.techascent.tv_launcher.domain.model.AppSortOrder
import com.techascent.tv_launcher.domain.repository.AppOrganizationRepository
import com.techascent.tv_launcher.domain.model.InstalledApp
import com.techascent.tv_launcher.domain.model.InstalledAppsUpdate
import com.techascent.tv_launcher.domain.model.LauncherWallpaper
import com.techascent.tv_launcher.domain.repository.AppStyleRepository
import com.techascent.tv_launcher.domain.repository.InstalledAppsRepository
import com.techascent.tv_launcher.domain.repository.RecentAppsRepository
import com.techascent.tv_launcher.domain.usecase.ObserveLauncherApps
import com.techascent.tv_launcher.domain.usecase.ObserveRecentApps
import com.techascent.tv_launcher.domain.usecase.RecordAppLaunch
import com.techascent.tv_launcher.presentation.state.LauncherAppUiState
import com.techascent.tv_launcher.presentation.state.LauncherUiState
import com.techascent.tv_launcher.presentation.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val recents = FakeRecentAppsRepository()
    private val style = FakeAppStyleRepository()
    private val organization = FakeAppOrganizationRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(appsRepository: InstalledAppsRepository = apps) = LauncherViewModel(
        observeLauncherApps = ObserveLauncherApps(appsRepository),
        observeRecentApps = ObserveRecentApps(recents),
        recordAppLaunch = RecordAppLaunch(recents),
        appStyleRepository = style,
        appOrganizationRepository = organization,
        clock = flowOf(ZonedDateTime.parse("2026-10-22T09:00:00+02:00")),
    )

    @Test
    fun `initial state stays loading until apps arrive`() {
        val pending = object : InstalledAppsRepository {
            override fun observeApps(): Flow<InstalledAppsUpdate> = MutableSharedFlow()
        }

        assertEquals(UiState.Loading, viewModel(pending).uiState.value.allApps)
    }

    @Test
    fun `installed apps default to alphabetical order`() {
        val state = viewModel().uiState.value
        assertEquals(listOf("Chrome", "Netflix", "YouTube"), allApps(state).map { it.label })
    }

    @Test
    fun `empty list is a successful empty state`() {
        apps.state.value = InstalledAppsUpdate.Loaded(emptyList())
        assertEquals(UiState.Success(emptyList<LauncherAppUiState>()), viewModel().uiState.value.allApps)
    }

    @Test
    fun `failing app source shows an error`() {
        val failing = object : InstalledAppsRepository {
            override fun observeApps(): Flow<InstalledAppsUpdate> = flow { error("boom") }
        }

        assertEquals(UiState.Error(R.string.apps_load_error), viewModel(failing).uiState.value.allApps)
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

        assertNull(viewModel.uiState.value.appOptions)
        assertEquals(listOf(LauncherEffect.UninstallApp(netflix.packageName)), effects)
    }

    @Test
    fun `refresh keeps apps and open dialog until the updated list arrives`() {
        val viewModel = viewModel()
        val before = viewModel.uiState.value.allApps
        viewModel.onAppLongPressed(viewModel.allApps().first { it.packageName == netflix.packageName })

        apps.state.value = InstalledAppsUpdate.Loading

        assertEquals(before, viewModel.uiState.value.allApps)
        assertEquals(netflix.packageName, viewModel.uiState.value.appOptions?.packageName)

        apps.state.value = InstalledAppsUpdate.Loaded(listOf(chrome, youtube))
        assertEquals(listOf("Chrome", "YouTube"), viewModel.allApps().map { it.label })
        assertNull(viewModel.uiState.value.appOptions)
    }

    @Test
    fun `launch history sorts apps newest first and skips uninstalled packages`() {
        recents.state.value = listOf("removed.app")
        val viewModel = viewModel()
        viewModel.onSortOrderSelected(AppSortOrder.RecentlyUsed)
        assertEquals(listOf("Chrome", "Netflix", "YouTube"), viewModel.allApps().map { it.label })

        viewModel.allApps().forEach(viewModel::onAppSelected)

        assertEquals(listOf("YouTube", "Netflix", "Chrome"), viewModel.allApps().map { it.label })
    }

    @Test
    fun `recently used sorting uses launch history beyond five apps`() {
        recents.state.value = (7 downTo 1).map { "example.app$it" }
        apps.state.value = InstalledAppsUpdate.Loaded((1..7).map { app("example.app$it", "App $it", true) })
        val viewModel = viewModel()
        viewModel.onSortOrderSelected(AppSortOrder.RecentlyUsed)

        assertEquals((7 downTo 1).map { "App $it" }, viewModel.allApps().map { it.label })
    }

    @Test
    fun `selecting an app launches it and records launch history`() = runTest {
        val viewModel = viewModel()
        val app = viewModel.allApps().first { it.packageName == netflix.packageName }
        val effects = mutableListOf<LauncherEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effect.toList(effects) }

        viewModel.onAppLongPressed(app)
        viewModel.onAppSelected(app)

        assertNull(viewModel.uiState.value.appOptions)
        assertEquals(listOf(LauncherEffect.LaunchApp(app.packageName, app.activityName)), effects)
        assertEquals(listOf(app.packageName), recents.state.value)
    }

    @Test
    fun `customize dialog opens and closes and style selection is saved`() {
        val viewModel = viewModel()
        assertEquals(AppStyle.Banner, viewModel.uiState.value.appStyle)

        viewModel.onCustomizeSelected()
        assertTrue(viewModel.uiState.value.customizeOpen)
        viewModel.onAppStyleSelected(AppStyle.Circle)
        assertEquals(AppStyle.Circle, viewModel.uiState.value.appStyle)
        viewModel.onCustomizeDismissed()

        assertFalse(viewModel.uiState.value.customizeOpen)
        assertEquals(AppStyle.Circle, viewModel.uiState.value.appStyle)
    }

    @Test
    fun `app names are saved independently of icon style`() {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.showAppNames)
        viewModel.onShowAppNamesSelected(true)
        assertTrue(style.showNames.value)
        assertTrue(viewModel.uiState.value.showAppNames)
        viewModel.onAppStyleSelected(AppStyle.Circle)
        assertTrue(viewModel.uiState.value.showAppNames)
        assertTrue(viewModel().uiState.value.showAppNames)
        viewModel.onShowAppNamesSelected(false)
        assertFalse(viewModel.uiState.value.showAppNames)
    }

    @Test
    fun `applying wallpaper closes customization and restores selection`() {
        val viewModel = viewModel()
        assertEquals(LauncherWallpaper.Three, viewModel.uiState.value.wallpaper)
        viewModel.onCustomizeSelected()
        viewModel.onWallpaperApplied(LauncherWallpaper.Two)

        assertFalse(viewModel.uiState.value.customizeOpen)
        assertEquals(LauncherWallpaper.Two, style.wallpaper.value)
        assertEquals(LauncherWallpaper.Two, viewModel.uiState.value.wallpaper)
        assertEquals(LauncherWallpaper.Two, viewModel().uiState.value.wallpaper)
        assertEquals(AppStyle.Banner, viewModel.uiState.value.appStyle)
    }

    @Test
    fun `dismissing customization does not change wallpaper`() {
        val viewModel = viewModel()
        viewModel.onCustomizeSelected()
        viewModel.onCustomizeDismissed()
        assertEquals(LauncherWallpaper.Three, style.wallpaper.value)
    }

    @Test
    fun `header follows the clock`() {
        val header = (viewModel().uiState.value.header as UiState.Success).data
        assertEquals(9, header.dateTime.hour)
    }

    @Test
    fun `pins toggle in pin order and remain in the grid across launch and sort events`() {
        val viewModel = viewModel()
        val states = viewModel.allApps()
        val netflix = states[1]
        val chrome = states[0]
        viewModel.onAppLongPressed(netflix)
        viewModel.onPinSelected(netflix)
        assertNull(viewModel.uiState.value.appOptions)
        viewModel.onPinSelected(chrome)
        viewModel.onAppSelected(chrome)
        viewModel.onSortOrderSelected(AppSortOrder.RecentlyUsed)

        assertEquals(listOf("Netflix", "Chrome"), viewModel.uiState.value.pinnedApps.map { it.label })
        assertEquals(3, viewModel.allApps().size)
        viewModel.onPinSelected(netflix)
        viewModel.onPinSelected(netflix)
        assertEquals(listOf("Chrome", "Netflix"), viewModel.uiState.value.pinnedApps.map { it.label })
        assertEquals(viewModel.uiState.value.pinnedApps, viewModel().uiState.value.pinnedApps)
        assertEquals(AppSortOrder.RecentlyUsed, viewModel().uiState.value.sortOrder)
    }

    @Test
    fun `hiding excludes every home section and restore preserves pin and history`() {
        val viewModel = viewModel()
        val netflix = viewModel.allApps()[1]
        viewModel.onPinSelected(netflix)
        viewModel.onAppSelected(netflix)
        viewModel.onSortOrderSelected(AppSortOrder.RecentlyUsed)
        viewModel.onAppLongPressed(netflix)
        viewModel.onHideSelected(netflix)

        assertNull(viewModel.uiState.value.appOptions)
        assertFalse(viewModel.allApps().any { it.packageName == netflix.packageName })
        assertEquals(listOf(netflix.packageName), recents.state.value)
        assertTrue(viewModel.uiState.value.pinnedApps.isEmpty())
        assertEquals(listOf(netflix), viewModel.uiState.value.hiddenApps)
        assertEquals(listOf(netflix), viewModel().uiState.value.hiddenApps)

        viewModel.onRestoreAppSelected(netflix)
        assertTrue(viewModel.uiState.value.hiddenApps.isEmpty())
        assertEquals(listOf(netflix), viewModel.uiState.value.pinnedApps)
        assertEquals(netflix, viewModel.allApps().first())
        assertEquals(3, viewModel.allApps().size)
    }

    @Test
    fun `app updates refresh sections without removing saved preferences`() {
        val viewModel = viewModel()
        val netflixUi = viewModel.allApps()[1]
        val youtubeUi = viewModel.allApps()[2]
        viewModel.onPinSelected(netflixUi)
        viewModel.onHideSelected(youtubeUi)
        apps.state.value = InstalledAppsUpdate.Loaded(listOf(chrome))
        assertTrue(viewModel.uiState.value.pinnedApps.isEmpty())
        assertTrue(viewModel.uiState.value.hiddenApps.isEmpty())

        apps.state.value = InstalledAppsUpdate.Loaded(listOf(youtube, netflix.copy(label = "Updated"), chrome))
        assertEquals(listOf("Updated"), viewModel.uiState.value.pinnedApps.map { it.label })
        assertEquals(listOf("YouTube"), viewModel.uiState.value.hiddenApps.map { it.label })
        assertEquals(listOf("Chrome", "Updated"), viewModel.allApps().map { it.label })
    }

    @Test
    fun `recently used sorting reacts to launches with alphabetical fallback`() {
        apps.state.value = InstalledAppsUpdate.Loaded(listOf(youtube, netflix, chrome))
        val viewModel = viewModel()
        assertEquals(listOf("Chrome", "Netflix", "YouTube"), viewModel.allApps().map { it.label })
        viewModel.onSortOrderSelected(AppSortOrder.RecentlyUsed)
        viewModel.onAppSelected(viewModel.allApps().last())
        assertEquals(listOf("YouTube", "Chrome", "Netflix"), viewModel.allApps().map { it.label })
        viewModel.onSortOrderSelected(AppSortOrder.Alphabetical)
        assertEquals(listOf("Chrome", "Netflix", "YouTube"), viewModel.allApps().map { it.label })
        assertEquals(listOf(youtube.packageName), recents.state.value)
    }

    private fun LauncherViewModel.allApps() = allApps(uiState.value)

    private fun allApps(state: LauncherUiState) =
        (state.allApps as UiState.Success).data

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

    private class FakeAppStyleRepository : AppStyleRepository {
        val wallpaper = MutableStateFlow(LauncherWallpaper.Three)
        override fun observeWallpaper(): Flow<LauncherWallpaper> = wallpaper
        override suspend fun setWallpaper(wallpaper: LauncherWallpaper) {
            this.wallpaper.value = wallpaper
        }
        val state = MutableStateFlow(AppStyle.Banner)
        val showNames = MutableStateFlow(false)
        override fun observeShowAppNames(): Flow<Boolean> = showNames
        override suspend fun setShowAppNames(show: Boolean) {
            showNames.value = show
        }
        override fun observeStyle(): Flow<AppStyle> = state
        override suspend fun setStyle(style: AppStyle) {
            state.value = style
        }
    }

    private class FakeRecentAppsRepository : RecentAppsRepository {
        val state = MutableStateFlow(emptyList<String>())
        override fun observeRecentApps(): Flow<List<String>> = state
        override suspend fun recordLaunch(packageName: String) {
            state.value = listOf(packageName) + (state.value - packageName)
        }
    }

    private class FakeAppOrganizationRepository : AppOrganizationRepository {
        val state = MutableStateFlow(AppOrganization())
        override fun observeOrganization(): Flow<AppOrganization> = state
        override suspend fun togglePin(packageName: String) {
            state.value = state.value.togglePin(packageName)
        }
        override suspend fun setHidden(packageName: String, hidden: Boolean) {
            state.value = state.value.copy(
                hiddenPackages = if (hidden) state.value.hiddenPackages + packageName
                else state.value.hiddenPackages - packageName,
            )
        }
        override suspend fun setSortOrder(order: AppSortOrder) {
            state.value = state.value.copy(sortOrder = order)
        }
    }
}
