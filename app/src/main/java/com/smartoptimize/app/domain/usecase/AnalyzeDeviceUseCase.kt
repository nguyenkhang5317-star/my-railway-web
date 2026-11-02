package com.smartoptimize.app.domain.usecase

import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.model.DashboardData
import com.smartoptimize.app.domain.model.Recommendation
import com.smartoptimize.app.domain.model.RecommendationAction
import com.smartoptimize.app.domain.model.RecommendationSeverity
import com.smartoptimize.app.utils.Formatters

/**
 * Builds the SMART OPTIMIZE recommendation list from real, already-measured
 * device data. It never invents "cleaned X MB" results and never claims an
 * action happened — it only suggests verifiable next steps to the user.
 */
class AnalyzeDeviceUseCase {

    operator fun invoke(
        dashboard: DashboardData,
        apps: List<AppEntry>,
        hasUsageAccess: Boolean,
        nowMillis: Long
    ): List<Recommendation> {
        val out = mutableListOf<Recommendation>()

        // --- Storage -------------------------------------------------------
        val storage = dashboard.storage
        when {
            storage.usedPercent >= 90f -> out += Recommendation(
                id = "storage_critical",
                severity = RecommendationSeverity.CRITICAL,
                title = "Internal storage almost full",
                description = "Storage is ${Formatters.formatPercent(storage.usedPercent)} used " +
                    "(${Formatters.formatBytes(storage.availableBytes)} free). " +
                    "A nearly full device can slow down updates and app installs. " +
                    "Review large apps and files in Android storage settings.",
                action = RecommendationAction.OPEN_STORAGE_SETTINGS
            )
            storage.usedPercent >= 80f -> out += Recommendation(
                id = "storage_warning",
                severity = RecommendationSeverity.WARNING,
                title = "Internal storage is getting full",
                description = "Storage is ${Formatters.formatPercent(storage.usedPercent)} used " +
                    "(${Formatters.formatBytes(storage.availableBytes)} free). " +
                    "Consider reviewing large or rarely used apps.",
                action = RecommendationAction.OPEN_STORAGE_SETTINGS
            )
            else -> out += Recommendation(
                id = "storage_ok",
                severity = RecommendationSeverity.INFO,
                title = "Storage looks healthy",
                description = "${Formatters.formatBytes(storage.availableBytes)} free of " +
                    "${Formatters.formatBytes(storage.totalBytes)} " +
                    "(${Formatters.formatPercent(storage.usedPercent)} used). No action needed.",
                action = RecommendationAction.NONE
            )
        }

        // --- RAM -----------------------------------------------------------
        val ram = dashboard.ram
        when {
            ram.isLowMemory || ram.availablePercent < 10f -> out += Recommendation(
                id = "ram_low",
                severity = RecommendationSeverity.WARNING,
                title = "Available RAM is low",
                description = "Only ${Formatters.formatBytes(ram.availableBytes)} of " +
                    "${Formatters.formatBytes(ram.totalBytes)} RAM is currently available. " +
                    "Android manages cached processes automatically; the most effective real " +
                    "step is reviewing apps you no longer need and restarting the device if " +
                    "it feels sluggish. This app does not force-close other apps.",
                action = RecommendationAction.REVIEW_APPS
            )
            ram.availablePercent < 20f -> out += Recommendation(
                id = "ram_okish",
                severity = RecommendationSeverity.INFO,
                title = "RAM usage is on the higher side",
                description = "${Formatters.formatBytes(ram.availableBytes)} of " +
                    "${Formatters.formatBytes(ram.totalBytes)} RAM is available. This is " +
                    "usually normal — Android keeps frequently used apps cached for faster " +
                    "relaunch. No cleanup action is required.",
                action = RecommendationAction.NONE
            )
            else -> out += Recommendation(
                id = "ram_ok",
                severity = RecommendationSeverity.INFO,
                title = "RAM looks healthy",
                description = "${Formatters.formatBytes(ram.availableBytes)} of " +
                    "${Formatters.formatBytes(ram.totalBytes)} RAM is available.",
                action = RecommendationAction.NONE
            )
        }

        // --- Battery ---------------------------------------------------------
        val battery = dashboard.battery
        battery.temperatureCelsius?.let { temp ->
            if (temp >= 40f) {
                out += Recommendation(
                    id = "battery_hot",
                    severity = RecommendationSeverity.WARNING,
                    title = "Battery temperature is high",
                    description = "Battery is at ${temp}°C. Let the device cool down, avoid " +
                        "heavy use while charging, and check which apps were recently active " +
                        "in Android battery settings.",
                    action = RecommendationAction.OPEN_BATTERY_SETTINGS
                )
            }
        }
        battery.levelPercent?.let { level ->
            if (level <= 20 && !battery.isCharging) {
                out += Recommendation(
                    id = "battery_low",
                    severity = RecommendationSeverity.INFO,
                    title = "Battery is low ($level%)",
                    description = "Consider charging soon or enabling Battery Saver in " +
                        "Android settings if you need the remaining charge to last longer.",
                    action = RecommendationAction.OPEN_BATTERY_SETTINGS
                )
            }
        }

        // --- Usage access / rarely used apps --------------------------------
        if (!hasUsageAccess) {
            out += Recommendation(
                id = "usage_access_missing",
                severity = RecommendationSeverity.INFO,
                title = "Grant Usage Access for app insights (optional)",
                description = "Without Usage Access the app cannot show last-used times, " +
                    "usage duration or per-app storage size. All other features keep " +
                    "working. Usage data is read on-device only and never leaves the phone.",
                action = RecommendationAction.GRANT_USAGE_ACCESS
            )
        } else {
            val rarelyUsed = apps.filter {
                AppGrouping.needsAttention(it, nowMillis, hasUsageData = true)
            }
            if (rarelyUsed.isNotEmpty()) {
                out += Recommendation(
                    id = "rarely_used_apps",
                    severity = RecommendationSeverity.INFO,
                    title = "${rarelyUsed.size} rarely used app(s) found",
                    description = "These user-installed apps show no recorded use in over " +
                        "30 days. Reviewing or uninstalling apps you no longer need is a " +
                        "real way to free storage. Nothing is removed automatically.",
                    action = RecommendationAction.REVIEW_APPS
                )
            }
        }

        return out.sortedByDescending { it.severity.ordinal }
    }
}
