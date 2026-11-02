package com.smartoptimize.app.domain.usecase

import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.model.AppFilter

/**
 * Pure app-list grouping / filtering logic.
 *
 * Definitions (kept honest about what Android lets a normal app observe):
 * - "Recently used"  = UsageStatsManager recorded foreground use < 24h ago.
 *   This does NOT mean the process is currently running.
 * - "Rarely used"    = Usage Access exists but there is no recorded use in
 *   the last 30 days (or no record at all inside the queried window).
 * - "Needs attention"= rarely used, user-installed, not protected.
 *   This is a review suggestion only — the app never restricts anything itself.
 */
object AppGrouping {

    const val RECENT_WINDOW_MILLIS: Long = 24L * 60 * 60 * 1000
    const val RARELY_USED_WINDOW_MILLIS: Long = 30L * 24 * 60 * 60 * 1000

    fun isRecent(app: AppEntry, nowMillis: Long): Boolean {
        val last = app.lastTimeUsedMillis ?: return false
        return last > 0 && nowMillis - last in 0..RECENT_WINDOW_MILLIS
    }

    /**
     * Only meaningful when usage data exists ([hasUsageData]). Without Usage
     * Access the app must never claim an app is "rarely used".
     */
    fun isRarelyUsed(app: AppEntry, nowMillis: Long, hasUsageData: Boolean): Boolean {
        if (!hasUsageData) return false
        val last = app.lastTimeUsedMillis
        return last == null || last <= 0 || nowMillis - last > RARELY_USED_WINDOW_MILLIS
    }

    fun needsAttention(app: AppEntry, nowMillis: Long, hasUsageData: Boolean): Boolean =
        !app.isSystemApp && !app.isProtectedByUser &&
            isRarelyUsed(app, nowMillis, hasUsageData)

    fun filterApps(
        apps: List<AppEntry>,
        filter: AppFilter,
        query: String,
        nowMillis: Long,
        hasUsageData: Boolean
    ): List<AppEntry> {
        val byFilter = when (filter) {
            AppFilter.ALL -> apps
            AppFilter.RECENT -> apps.filter { isRecent(it, nowMillis) }
            AppFilter.RARELY_USED -> apps.filter { isRarelyUsed(it, nowMillis, hasUsageData) }
            AppFilter.NEEDS_ATTENTION -> apps.filter { needsAttention(it, nowMillis, hasUsageData) }
            AppFilter.SYSTEM -> apps.filter { it.isSystemApp }
            AppFilter.PROTECTED -> apps.filter { it.isProtectedByUser }
        }
        val q = query.trim()
        val byQuery = if (q.isEmpty()) byFilter else byFilter.filter {
            it.label.contains(q, ignoreCase = true) ||
                it.packageName.contains(q, ignoreCase = true)
        }
        // Most recently used first; unknown usage last, then alphabetical.
        return byQuery.sortedWith(
            compareByDescending<AppEntry> { it.lastTimeUsedMillis ?: Long.MIN_VALUE }
                .thenBy { it.label.lowercase() }
        )
    }
}
