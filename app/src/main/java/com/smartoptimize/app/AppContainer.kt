package com.smartoptimize.app

import android.content.Context
import com.smartoptimize.app.data.preferences.PreferencesRepositoryImpl
import com.smartoptimize.app.data.repository.AppsRepositoryImpl
import com.smartoptimize.app.data.repository.DeviceRepositoryImpl
import com.smartoptimize.app.domain.repository.AppsRepository
import com.smartoptimize.app.domain.repository.DeviceRepository
import com.smartoptimize.app.domain.repository.PreferencesRepository
import com.smartoptimize.app.domain.usecase.AnalyzeDeviceUseCase
import com.smartoptimize.app.platform.battery.BatteryInfoProvider
import com.smartoptimize.app.platform.device.DeviceInfoProvider
import com.smartoptimize.app.platform.memory.RamInfoProvider
import com.smartoptimize.app.platform.pkg.AppListProvider
import com.smartoptimize.app.platform.storage.StorageInfoProvider
import com.smartoptimize.app.platform.usage.UsageStatsProvider

/**
 * Minimal manual dependency container. A DI framework would add startup
 * cost and APK size that this utility app deliberately avoids.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val preferencesRepository: PreferencesRepository =
        PreferencesRepositoryImpl(appContext)

    val deviceRepository: DeviceRepository = DeviceRepositoryImpl(
        ramProvider = RamInfoProvider(appContext),
        storageProvider = StorageInfoProvider(appContext),
        batteryProvider = BatteryInfoProvider(appContext),
        deviceProvider = DeviceInfoProvider()
    )

    val appsRepository: AppsRepository = AppsRepositoryImpl(
        appListProvider = AppListProvider(appContext),
        usageProvider = UsageStatsProvider(appContext),
        storageProvider = StorageInfoProvider(appContext),
        preferences = preferencesRepository
    )

    val analyzeDeviceUseCase: AnalyzeDeviceUseCase = AnalyzeDeviceUseCase()
}
