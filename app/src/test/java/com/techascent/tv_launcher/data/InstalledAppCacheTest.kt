package com.techascent.tv_launcher.data

import com.techascent.tv_launcher.data.repository.InstalledAppCache
import com.techascent.tv_launcher.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class InstalledAppCacheTest {
    private val cache = InstalledAppCache()
    private var loads = 0

    private fun load(packageName: String, activity: String = "Main") =
        cache.getOrLoad(packageName, activity) {
            loads++
            InstalledApp(packageName, activity, packageName, null, true)
        }

    @Test
    fun `unchanged apps reuse the decoded domain object`() {
        val first = load("a")
        assertSame(first, load("a"))
        assertEquals(1, loads)
    }

    @Test
    fun `package changes reload only affected images`() {
        load("a")
        val other = load("b")
        cache.invalidate("a")
        load("a")
        assertSame(other, load("b"))
        assertEquals(3, loads)
    }

    @Test
    fun `removed or replaced activities are evicted`() {
        load("a")
        val other = load("b")
        cache.retain(setOf("b" to "Main"))
        load("a", "NewMain")
        load("a")
        assertSame(other, load("b"))
        assertEquals(4, loads)
    }

    @Test
    fun `configuration changes discard localized labels and images`() {
        load("a")
        cache.clear()
        load("a")
        assertEquals(2, loads)
    }

    @Test
    fun `refreshing twenty apps after one update decodes only one app`() {
        val packages = (1..20).map { "app.$it" }
        val original = packages.associateWith { load(it) }
        cache.invalidate("app.7")
        packages.forEach {
            val refreshed = load(it)
            if (it != "app.7") assertSame(original.getValue(it), refreshed)
        }
        assertEquals(21, loads)
    }

    @Test
    fun `several pending package changes each invalidate their entry`() {
        load("a")
        load("b")
        val unchanged = load("c")
        cache.invalidate("a")
        cache.invalidate("b")
        load("a")
        load("b")
        assertSame(unchanged, load("c"))
        assertEquals(5, loads)
    }
}
