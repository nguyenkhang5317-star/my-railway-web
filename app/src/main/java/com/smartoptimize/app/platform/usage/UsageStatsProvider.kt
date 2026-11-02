package com.smartoptimize.app.platform.usage

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process

data class UsageSummary(
    val lastTimeUsedMillis: Long,
    val totalForegroundMillis: Long
)

/**
 * Usage statistics via [UsageStatsManager]. Everything degrades to
 * "no data" when Usage Access is absent — data is never simulated.
 */
class UsageStatsProvider(private val context: Context) {

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Aggregated per-package usage over the last [windowMillis]. Daily
     * buckets are summed, so total foreground time is an approximation —
     * the UI labels it as such.
     */
    fun queryUsage(nowMillis: Long, windowMillis: Long = DEFAULT_WINDOW_MILLIS): Map<String, UsageSummary> {
        if (!hasUsageAccess()) return emptyMap()
        val manager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return emptyMap()
        val stats: List<UsageStats> = try {
            manager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                nowMillis - windowMillis,
                nowMillis
            ) ?: emptyList()
        } catch (e: SecurityException) {
            return emptyMap()
        }
        val aggregated = HashMap<String, UsageSummary>()
        for (entry in stats) {
            val pkg = entry.packageName ?: continue
            val previous = aggregated[pkg]
            aggregated[pkg] = UsageSummary(
                lastTimeUsedMillis = maxOf(previous?.lastTimeUsedMillis ?: 0L, entry.lastTimeUsed),
                totalForegroundMillis =
                    (previous?.totalForegroundMillis ?: 0L) + entry.totalTimeInForeground
            )
        }
        return aggregated
    }

    companion object {
        /** 30 days — matches the "rarely used" definition in AppGrouping. */
        const val DEFAULT_WINDOW_MILLIS: Long = 30L * 24 * 60 * 60 * 1000
    }
}
