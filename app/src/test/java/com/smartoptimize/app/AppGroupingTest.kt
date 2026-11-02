package com.smartoptimize.app

import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.model.AppFilter
import com.smartoptimize.app.domain.usecase.AppGrouping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppGroupingTest {

    private val now = 1_700_000_000_000L
    private val hour = 60L * 60 * 1000
    private val day = 24 * hour

    private fun app(
        pkg: String,
        lastUsed: Long? = null,
        system: Boolean = false,
        protectedApp: Boolean = false
    ) = AppEntry(
        packageName = pkg,
        label = pkg,
        isSystemApp = system,
        versionName = "1.0",
        firstInstallMillis = null,
        lastUpdateMillis = null,
        lastTimeUsedMillis = lastUsed,
        totalForegroundMillis = null,
        isProtectedByUser = protectedApp
    )

    @Test
    fun `recent means used within 24 hours`() {
        assertTrue(AppGrouping.isRecent(app("a", lastUsed = now - 2 * hour), now))
        assertFalse(AppGrouping.isRecent(app("a", lastUsed = now - 2 * day), now))
        assertFalse(AppGrouping.isRecent(app("a", lastUsed = null), now))
    }

    @Test
    fun `rarely used requires usage data to exist`() {
        val app = app("a", lastUsed = null)
        // Without Usage Access the app must never claim "rarely used".
        assertFalse(AppGrouping.isRarelyUsed(app, now, hasUsageData = false))
        assertTrue(AppGrouping.isRarelyUsed(app, now, hasUsageData = true))
        assertTrue(
            AppGrouping.isRarelyUsed(app("b", lastUsed = now - 40 * day), now, hasUsageData = true)
        )
        assertFalse(
            AppGrouping.isRarelyUsed(app("c", lastUsed = now - 2 * day), now, hasUsageData = true)
        )
    }

    @Test
    fun `needs attention excludes system and protected apps`() {
        assertTrue(
            AppGrouping.needsAttention(app("a", lastUsed = null), now, hasUsageData = true)
        )
        assertFalse(
            AppGrouping.needsAttention(
                app("a", lastUsed = null, protectedApp = true), now, hasUsageData = true
            )
        )
        assertFalse(
            AppGrouping.needsAttention(
                app("a", lastUsed = null, system = true), now, hasUsageData = true
            )
        )
    }

    @Test
    fun `filter applies grouping query and ordering`() {
        val apps = listOf(
            app("com.example.old", lastUsed = now - 60 * day),
            app("com.example.fresh", lastUsed = now - hour),
            app("com.android.system", lastUsed = now - 3 * hour, system = true)
        )
        val recent = AppGrouping.filterApps(apps, AppFilter.RECENT, "", now, hasUsageData = true)
        assertEquals(
            listOf("com.example.fresh", "com.android.system"),
            recent.map { it.packageName }
        )

        val searched = AppGrouping.filterApps(apps, AppFilter.ALL, "fresh", now, hasUsageData = true)
        assertEquals(listOf("com.example.fresh"), searched.map { it.packageName })

        val attention =
            AppGrouping.filterApps(apps, AppFilter.NEEDS_ATTENTION, "", now, hasUsageData = true)
        assertEquals(listOf("com.example.old"), attention.map { it.packageName })
    }
}
