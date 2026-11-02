package com.smartoptimize.app.data.repository

import com.smartoptimize.app.domain.model.DashboardData
import com.smartoptimize.app.domain.repository.DeviceRepository
import com.smartoptimize.app.platform.battery.BatteryInfoProvider
import com.smartoptimize.app.platform.device.DeviceInfoProvider
import com.smartoptimize.app.platform.memory.RamInfoProvider
import com.smartoptimize.app.platform.storage.StorageInfoProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceRepositoryImpl(
    private val ramProvider: RamInfoProvider,
    private val storageProvider: StorageInfoProvider,
    private val batteryProvider: BatteryInfoProvider,
    private val deviceProvider: DeviceInfoProvider
) : DeviceRepository {

    override suspend fun loadDashboard(): DashboardData = withContext(Dispatchers.IO) {
        DashboardData(
            ram = ramProvider.read(),
            storage = storageProvider.readInternalStorage(),
            battery = batteryProvider.read(),
            device = deviceProvider.read(),
            updatedAtMillis = System.currentTimeMillis()
        )
    }
}
