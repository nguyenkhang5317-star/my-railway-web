package com.smartoptimize.app.utils

import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

/**
 * Pure formatting helpers (no Android dependencies) so they can be covered
 * by fast JVM unit tests.
 */
object Formatters {

    private val UNITS = arrayOf("B", "KB", "MB", "GB", "TB")

    fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "—"
        if (bytes < 1024) return "$bytes B"
        val exp = minOf((ln(bytes.toDouble()) / ln(1024.0)).toInt(), UNITS.lastIndex)
        val value = bytes / 1024.0.pow(exp.toDouble())
        return String.format(Locale.US, "%.1f %s", value, UNITS[exp])
    }

    fun formatPercent(value: Float): String =
        String.format(Locale.US, "%.0f%%", value.coerceIn(0f, 100f))

    /** e.g. 3_661_000 ms -> "1h 1m", 45_000 ms -> "< 1m". */
    fun formatDuration(millis: Long): String {
        if (millis < 60_000) return "< 1m"
        val totalMinutes = millis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours <= 0 -> "${minutes}m"
            minutes == 0L -> "${hours}h"
            else -> "${hours}h ${minutes}m"
        }
    }

    /**
     * Coarse relative-time label. [nowMillis] is a parameter (instead of
     * reading the clock internally) to keep the function deterministic
     * and testable.
     */
    fun formatRelativeTime(timeMillis: Long?, nowMillis: Long): String {
        if (timeMillis == null || timeMillis <= 0L) return "No data"
        val diff = nowMillis - timeMillis
        if (diff < 0) return "Just now"
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 30 -> "${days}d ago"
            days < 365 -> "${days / 30}mo ago"
            else -> "${days / 365}y ago"
        }
    }
}
