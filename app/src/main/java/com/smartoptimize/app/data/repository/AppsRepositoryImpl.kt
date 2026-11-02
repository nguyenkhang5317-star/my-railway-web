package com.smartoptimize.app.data.repository

import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.repository.AppsRepository
import com.smartoptimize.app.domain.repository.PreferencesRepository
import com.smartoptimize.app.platform.pkg.AppListProvider
import com.smartoptimize.app.platform.storage.StorageInfoProvider
import com.smartoptimize.app.platform.usage.UsageStatsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AppsRepositoryImpl(
    private val appListProvider: AppListProvider,
    private val usageProvider: UsageStatsProvider,
    private val storageProvider: StorageInfoProvider,
    private val preferences: PreferencesRepository
) : AppsRepository {

    override fun hasUsageAccess(): Boolean = usageProvider.hasUsageAccess()

    override suspend fun loadApps(): List<AppEntry> = withContext(Dispatchers.IO) {
        val protectedByUser = preferences.protectedPackages.first()
        val usage = usageProvider.queryUsage(nowMillis = System.currentTimeMillis())
        // The default launcher keeps the device usable; always treat it
        // (and this app itself) as protected regardless of user toggles.
        val alwaysProtected = setOfNotNull(appListProvider.defaultHomePackage(), appListProvider.ownPackageName())

        appListProvider.loadLaunchableApps().map { raw ->
            val summary = usage[raw.packageName]
            AppEntry(
                packageName = raw.packageName,
                label = raw.label,
                isSystemApp = raw.isSystemApp,
                versionName = raw.versionName,
                firstInstallMillis = raw.firstInstallMillis,
                lastUpdateMillis = raw.lastUpdateMillis,
                lastTimeUsedMillis = summary?.lastTimeUsedMillis?.takeIf { it > 0 },
                totalForegroundMillis = summary?.totalForegroundMillis,
                isProtectedByUser = raw.packageName in protectedByUser ||
                    raw.packageName in alwaysProtected
            )
        }
    }

    override fun defaultHomePackage(): String? = appListProvider.defaultHomePackage()

    override suspend fun loadAppSize(packageName: String): Long? = withContext(Dispatchers.IO) {
        storageProvider.readAppTotalSize(packageName)
    }
}
