package com.smartoptimize.app.platform.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.smartoptimize.app.domain.model.BatteryInfo

/**
 * Battery values from [BatteryManager] properties and the sticky
 * [Intent.ACTION_BATTERY_CHANGED] broadcast. Any value the hardware does
 * not report stays null — battery wear is never guessed from charge level.
 */
class BatteryInfoProvider(private val context: Context) {

    fun read(): BatteryInfo {
        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val sticky = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val levelFromSticky: Int? = sticky?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) (level * 100) / scale else null
        }

        val status = sticky?.getIntExtra(
            BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL ||
            batteryManager.isCharging

        val temperature = sticky?.let {
            val raw = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            if (raw == Int.MIN_VALUE) null else raw / 10f
        }
        val voltage = sticky?.let {
            val raw = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            if (raw < 0) null else raw
        }
        val health = sticky?.let {
            when (it.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                else -> null // UNKNOWN -> shown as "not reported"
            }
        }
        val technology = sticky?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

        return BatteryInfo(
            levelPercent = if (capacity in 0..100) capacity else levelFromSticky,
            isCharging = isCharging,
            temperatureCelsius = temperature,
            voltageMillivolts = voltage,
            health = health,
            technology = technology
        )
    }
}
