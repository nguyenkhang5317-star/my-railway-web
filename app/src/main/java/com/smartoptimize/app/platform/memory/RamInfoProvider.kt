package com.smartoptimize.app.platform.memory

import android.app.ActivityManager
import android.content.Context
import com.smartoptimize.app.domain.model.RamInfo

/** Reads RAM values from [ActivityManager.MemoryInfo] (public API). */
class RamInfoProvider(context: Context) {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun read(): RamInfo {
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return RamInfo(
            totalBytes = info.totalMem,
            availableBytes = info.availMem,
            lowMemoryThresholdBytes = info.threshold,
            isLowMemory = info.lowMemory
        )
    }
}
