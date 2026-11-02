package com.smartoptimize.app.domain.repository

import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.model.DashboardData
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    /** Reads fresh RAM / storage / battery / device values from public Android APIs. */
    suspend fun loadDashboard(): DashboardData
}

interface AppsRepository {
    /** True when the user has granted Usage Access in Android Settings. */
    fun hasUsageAccess(): Boolean

    /**
     * Launchable apps merged with usage statistics (when available) and the
     * user's protected-app choices.
     */
    suspend fun loadApps(): List<AppEntry>

    /** Package name of the current default home (launcher) app, if resolvable. */
    fun defaultHomePackage(): String?

    /** App + data + cache size for one package, or null when not readable. */
    suspend fun loadAppSize(packageName: String): Long?
}

interface PreferencesRepository {
    /** Packages the user marked as "keep" — never suggested for restriction. */
    val protectedPackages: Flow<Set<String>>

    /** Whether Foreground App Focus mode is currently switched on. */
    val focusModeEnabled: Flow<Boolean>

    suspend fun setPackageProtected(packageName: String, isProtected: Boolean)
    suspend fun setFocusModeEnabled(enabled: Boolean)
}
