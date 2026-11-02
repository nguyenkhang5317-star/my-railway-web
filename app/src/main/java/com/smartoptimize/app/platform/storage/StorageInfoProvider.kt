package com.smartoptimize.app.platform.storage

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.storage.StorageManager
import com.smartoptimize.app.domain.model.StorageInfo
import java.io.IOException

/**
 * Internal-storage figures from [StatFs], plus optional per-app sizes from
 * [StorageStatsManager]. The per-app query throws [SecurityException] when
 * Usage Access has not been granted — that is treated as "unknown", never
 * as zero.
 */
class StorageInfoProvider(private val context: Context) {

    fun readInternalStorage(): StorageInfo {
        val statFs = StatFs(Environment.getDataDirectory().path)
        return StorageInfo(
            totalBytes = statFs.blockCountLong * statFs.blockSizeLong,
            availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        )
    }

    /** App + data + cache bytes for [packageName], or null when not readable. */
    fun readAppTotalSize(packageName: String): Long? {
        val statsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
                ?: return null
        val storageManager =
            context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
                ?: return null
        return try {
            val uuid = storageManager.getUuidForPath(Environment.getDataDirectory())
            val stats = statsManager.queryStatsForPackage(uuid, packageName, Process.myUserHandle())
            stats.appBytes + stats.dataBytes + stats.cacheBytes
        } catch (e: SecurityException) {
            null // Usage Access not granted.
        } catch (e: IOException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
