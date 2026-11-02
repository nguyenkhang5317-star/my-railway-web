package com.smartoptimize.app

import com.smartoptimize.app.domain.model.BatteryInfo
import com.smartoptimize.app.domain.model.DashboardData
import com.smartoptimize.app.domain.model.DeviceInfo
import com.smartoptimize.app.domain.model.RamInfo
import com.smartoptimize.app.domain.model.RecommendationAction
import com.smartoptimize.app.domain.model.RecommendationSeverity
import com.smartoptimize.app.domain.model.StorageInfo
import com.smartoptimize.app.domain.usecase.AnalyzeDeviceUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzeDeviceUseCaseTest {

    private val useCase = AnalyzeDeviceUseCase()
    private val now = 1_700_000_000_000L

    private fun dashboard(
        storageUsedPercent: Float,
        ramAvailablePercent: Float,
        batteryLevel: Int? = 80,
        batteryTemp: Float? = 30f,
        charging: Boolean = false
    ): DashboardData {
        val gb = 1024L * 1024 * 1024
        val totalStorage = 64 * gb
        val availStorage = (totalStorage * (100f - storageUsedPercent) / 100f).toLong()
        val totalRam = 8 * gb
        val availRam = (totalRam * ramAvailablePercent / 100f).toLong()
        return DashboardData(
            ram = RamInfo(
                totalBytes = totalRam,
                availableBytes = availRam,
                lowMemoryThresholdBytes = 512L * 1024 * 1024,
                isLowMemory = false
            ),
            storage = StorageInfo(totalBytes = totalStorage, availableBytes = availStorage),
            battery = BatteryInfo(
                levelPercent = batteryLevel,
                isCharging = charging,
                temperatureCelsius = batteryTemp,
                voltageMillivolts = 4000,
                health = "Good",
                technology = "Li-ion"
            ),
            device = DeviceInfo(
                manufacturer = "Test",
                model = "Test Phone",
                androidVersion = "14",
                sdkInt = 34,
                supportedAbis = listOf("arm64-v8a")
            ),
            updatedAtMillis = now
        )
    }

    @Test
    fun `storage at 95 percent is critical with settings action`() {
        val result = useCase(dashboard(95f, 50f), emptyList(), hasUsageAccess = true, now)
        val first = result.first()
        assertEquals("storage_critical", first.id)
        assertEquals(RecommendationSeverity.CRITICAL, first.severity)
        assertEquals(RecommendationAction.OPEN_STORAGE_SETTINGS, first.action)
    }

    @Test
    fun `healthy device yields only informational recommendations`() {
        val result = useCase(dashboard(40f, 55f), emptyList(), hasUsageAccess = true, now)
        assertTrue(result.all { it.severity == RecommendationSeverity.INFO })
        assertTrue(
            "No recommendation may claim an automatic cleanup happened",
            result.none { it.action == RecommendationAction.OPEN_STORAGE_SETTINGS }
        )
    }

    @Test
    fun `missing usage access is suggested as optional`() {
        val result = useCase(dashboard(40f, 55f), emptyList(), hasUsageAccess = false, now)
        assertTrue(result.any { it.id == "usage_access_missing" })
        assertTrue(
            result.any { it.action == RecommendationAction.GRANT_USAGE_ACCESS }
        )
    }

    @Test
    fun `hot battery raises a warning`() {
        val result = useCase(
            dashboard(40f, 55f, batteryTemp = 42f), emptyList(),
            hasUsageAccess = true, now
        )
        val hot = result.first { it.id == "battery_hot" }
        assertEquals(RecommendationSeverity.WARNING, hot.severity)
    }

    @Test
    fun `recommendations are sorted by severity descending`() {
        val result = useCase(
            dashboard(95f, 5f, batteryTemp = 45f), emptyList(),
            hasUsageAccess = false, now
        )
        val severities = result.map { it.severity.ordinal }
        assertEquals(severities.sortedDescending(), severities)
    }
}
