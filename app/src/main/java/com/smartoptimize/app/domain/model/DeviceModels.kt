package com.smartoptimize.app.domain.model

/**
 * Pure-Kotlin domain models. No Android framework types are used here so the
 * business logic built on top of these models stays unit-testable on the JVM.
 *
 * Any value that a device or API may legitimately fail to provide is modeled
 * as nullable and must be rendered as "not available" by the UI — never
 * replaced with a fabricated number.
 */

data class RamInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val lowMemoryThresholdBytes: Long,
    val isLowMemory: Boolean
) {
    val usedBytes: Long get() = (totalBytes - availableBytes).coerceAtLeast(0L)
    val usedPercent: Float
        get() = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() * 100f else 0f
    val availablePercent: Float get() = 100f - usedPercent
}

data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long
) {
    val usedBytes: Long get() = (totalBytes - availableBytes).coerceAtLeast(0L)
    val usedPercent: Float
        get() = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() * 100f else 0f
}

data class BatteryInfo(
    /** Charge level 0..100, null when the device does not report it. */
    val levelPercent: Int?,
    val isCharging: Boolean,
    val temperatureCelsius: Float?,
    val voltageMillivolts: Int?,
    /** Human readable health string mapped from BatteryManager constants, or null. */
    val health: String?,
    val technology: String?
)

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val supportedAbis: List<String>
) {
    val displayName: String
        get() {
            val m = model.trim()
            val brand = manufacturer.trim()
            return if (brand.isEmpty() || m.startsWith(brand, ignoreCase = true)) m else "$brand $m"
        }
}

data class DashboardData(
    val ram: RamInfo,
    val storage: StorageInfo,
    val battery: BatteryInfo,
    val device: DeviceInfo,
    val updatedAtMillis: Long
)

/**
 * One launchable application known to the app through PackageManager
 * (launcher-intent visibility only — QUERY_ALL_PACKAGES is not used).
 *
 * [lastTimeUsedMillis] / [totalForegroundMillis] come from
 * UsageStatsManager and are null when Usage Access is not granted or the
 * system has no record. Null literally means "unknown", not "never used".
 */
data class AppEntry(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val versionName: String?,
    val firstInstallMillis: Long?,
    val lastUpdateMillis: Long?,
    val lastTimeUsedMillis: Long?,
    val totalForegroundMillis: Long?,
    val isProtectedByUser: Boolean,
    /** Total size (app + data + cache) when StorageStatsManager could read it. */
    val totalSizeBytes: Long? = null
)

enum class AppFilter(val title: String) {
    ALL("All"),
    RECENT("Recently used"),
    RARELY_USED("Rarely used"),
    NEEDS_ATTENTION("Needs attention"),
    SYSTEM("System"),
    PROTECTED("Protected")
}

enum class RecommendationSeverity { INFO, WARNING, CRITICAL }

enum class RecommendationAction {
    NONE,
    OPEN_STORAGE_SETTINGS,
    OPEN_BATTERY_SETTINGS,
    REVIEW_APPS,
    GRANT_USAGE_ACCESS
}

data class Recommendation(
    val id: String,
    val severity: RecommendationSeverity,
    val title: String,
    val description: String,
    val action: RecommendationAction
)
