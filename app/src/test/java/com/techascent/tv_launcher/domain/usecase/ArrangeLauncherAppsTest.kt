package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.model.AppOrganization
import com.techascent.tv_launcher.domain.model.AppSortOrder
import com.techascent.tv_launcher.domain.model.InstalledApp
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArrangeLauncherAppsTest {
    private val arrange = ArrangeLauncherApps()
    private val alpha = app("alpha", "Alpha")
    private val beta = app("beta", "Beta")
    private val gamma = app("gamma", "Gamma")

    @Test
    fun `pins retain insertion order independent of sorting and history without a cap`() {
        val apps = (1..25).map { app("app$it", "App $it") }
        val pins = apps.reversed().map { it.packageName }
        val result = arrange(apps, apps.map { it.packageName }, AppOrganization(pinnedPackages = pins))
        assertEquals(pins, result.pinned.map { it.packageName })
        assertEquals(apps.size, result.all.size)
        assertEquals(5, result.recent.size)
    }

    @Test
    fun `toggle unpins and repinning moves to the end without duplicates`() {
        val settings = AppOrganization()
            .togglePin("gamma").togglePin("alpha").togglePin("gamma").togglePin("gamma")
        assertEquals(listOf("alpha", "gamma"), settings.pinnedPackages)
    }

    @Test
    fun `hidden and uninstalled packages never render in home rows`() {
        val settings = AppOrganization(
            pinnedPackages = listOf("missing", "gamma", "beta", "alpha"),
            hiddenPackages = setOf("beta", "missing"),
        )
        val result = arrange(listOf(gamma, beta, alpha), listOf("beta", "missing", "gamma"), settings)
        assertEquals(listOf(alpha, gamma), result.all)
        assertEquals(listOf(gamma, alpha), result.pinned)
        assertEquals(listOf(gamma), result.recent)
        assertEquals(listOf(beta), result.hidden)
        val restored = arrange(listOf(gamma, beta, alpha), listOf("beta"), settings.copy(hiddenPackages = emptySet()))
        assertEquals(listOf(gamma, beta, alpha), restored.pinned)
        assertEquals(listOf(beta), restored.recent)
        assertTrue(restored.hidden.isEmpty())
    }

    @Test
    fun `recently used puts known launches first and unknown apps alphabetically`() {
        val result = arrange(
            listOf(beta, gamma, alpha),
            listOf("missing", "gamma", "gamma"),
            AppOrganization(sortOrder = AppSortOrder.RecentlyUsed),
        )
        assertEquals(listOf(gamma, alpha, beta), result.all)
        assertEquals(listOf(gamma), result.recent)
        assertEquals(
            listOf(alpha, beta, gamma),
            arrange(listOf(gamma, beta, alpha), emptyList(), AppOrganization(sortOrder = AppSortOrder.RecentlyUsed)).all,
        )
    }

    @Test
    fun `case insensitive equal labels have deterministic package fallback regardless of source order`() {
        val apps = listOf(app("z", "alpha"), app("a", "ALPHA"), beta)
        val settings = AppOrganization()
        val expected = listOf("a", "z", "beta")
        assertEquals(expected, arrange(apps, emptyList(), settings, Locale.ENGLISH).all.map { it.packageName })
        assertEquals(expected, arrange(apps.reversed(), emptyList(), settings, Locale.ENGLISH).all.map { it.packageName })
    }

    @Test
    fun `alphabetical sorting follows locale collation`() {
        val apps = listOf(app("a-ring", "Åland"), app("z", "Zulu"), app("a", "Alpha"))
        assertEquals(
            listOf("a", "z", "a-ring"),
            arrange(apps, emptyList(), AppOrganization(), Locale.forLanguageTag("sv")).all.map { it.packageName },
        )
    }

    private fun app(packageName: String, label: String) = InstalledApp(
        packageName = packageName,
        activityName = "$packageName.Main",
        label = label,
        icon = null,
        isRemovable = true,
    )
}
