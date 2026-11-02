package com.smartoptimize.app.platform.pkg

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/** Raw package facts, before merging with usage statistics. */
data class RawInstalledApp(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val versionName: String?,
    val firstInstallMillis: Long?,
    val lastUpdateMillis: Long?
)

/**
 * Enumerates launchable apps via the launcher intent declared in the
 * manifest `<queries>` block. QUERY_ALL_PACKAGES is intentionally not used,
 * so non-launchable background-only packages stay invisible — a documented
 * limitation, surfaced in the UI and README.
 */
class AppListProvider(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    fun loadLaunchableApps(): List<RawInstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = try {
            pm.queryIntentActivities(intent, 0)
        } catch (e: SecurityException) {
            emptyList()
        }
        val byPackage = LinkedHashMap<String, RawInstalledApp>()
        for (resolveInfo in resolved) {
            val appInfo = resolveInfo.activityInfo?.applicationInfo ?: continue
            val pkg = appInfo.packageName
            if (byPackage.containsKey(pkg)) continue
            val packageInfo = try {
                pm.getPackageInfo(pkg, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            byPackage[pkg] = RawInstalledApp(
                packageName = pkg,
                label = appInfo.loadLabel(pm).toString(),
                isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                versionName = packageInfo?.versionName,
                firstInstallMillis = packageInfo?.firstInstallTime?.takeIf { it > 0 },
                lastUpdateMillis = packageInfo?.lastUpdateTime?.takeIf { it > 0 }
            )
        }
        return byPackage.values.sortedBy { it.label.lowercase() }
    }

    fun defaultHomePackage(): String? {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return try {
            pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
        } catch (e: SecurityException) {
            null
        }
    }

    fun ownPackageName(): String = context.packageName
}
