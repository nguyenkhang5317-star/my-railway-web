package com.smartoptimize.app.permissions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * The only way this app "manages" other apps: opening the official Android
 * Settings pages where the user stays in control. Every intent is checked
 * with [Intent.resolveActivity] first and failures are reported back as
 * `false` so the UI can show manual instructions instead of crashing or,
 * worse, pretending something happened.
 */
object SettingsNavigator {

    private fun tryStart(context: Context, intent: Intent): Boolean {
        return try {
            if (intent.resolveActivity(context.packageManager) == null) return false
            if (context.findActivity() == null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    /** Official per-app info page (permissions, storage, battery, force stop by the user). */
    fun openAppDetails(context: Context, packageName: String): Boolean {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        if (tryStart(context, intent)) return true
        return tryStart(context, Intent(Settings.ACTION_SETTINGS))
    }

    /** System page where the user can grant Usage Access to this app. */
    fun openUsageAccessSettings(context: Context): Boolean {
        if (tryStart(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))) return true
        return tryStart(context, Intent(Settings.ACTION_SETTINGS))
    }

    fun openBatterySettings(context: Context): Boolean {
        if (tryStart(context, Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))) return true
        return tryStart(context, Intent(Settings.ACTION_SETTINGS))
    }

    fun openStorageSettings(context: Context): Boolean {
        @Suppress("DEPRECATION")
        if (tryStart(context, Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))) return true
        return tryStart(context, Intent(Settings.ACTION_SETTINGS))
    }

    private fun Context.findActivity(): Activity? {
        var current: Context = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}
