package com.smartoptimize.app

import com.smartoptimize.app.utils.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `bytes under 1KB stay in bytes`() {
        assertEquals("0 B", Formatters.formatBytes(0))
        assertEquals("512 B", Formatters.formatBytes(512))
    }

    @Test
    fun `bytes scale to KB MB GB`() {
        assertEquals("1.0 KB", Formatters.formatBytes(1024))
        assertEquals("1.5 MB", Formatters.formatBytes((1.5 * 1024 * 1024).toLong()))
        assertEquals("2.0 GB", Formatters.formatBytes(2L * 1024 * 1024 * 1024))
    }

    @Test
    fun `negative bytes render as unavailable`() {
        assertEquals("—", Formatters.formatBytes(-1))
    }

    @Test
    fun `duration formats hours and minutes`() {
        assertEquals("< 1m", Formatters.formatDuration(30_000))
        assertEquals("5m", Formatters.formatDuration(5 * 60_000))
        assertEquals("2h", Formatters.formatDuration(2 * 60 * 60_000))
        assertEquals("1h 1m", Formatters.formatDuration(3_661_000))
    }

    @Test
    fun `relative time handles missing data honestly`() {
        val now = 1_700_000_000_000L
        assertEquals("No data", Formatters.formatRelativeTime(null, now))
        assertEquals("No data", Formatters.formatRelativeTime(0, now))
    }

    @Test
    fun `relative time buckets`() {
        val now = 1_700_000_000_000L
        assertEquals("Just now", Formatters.formatRelativeTime(now - 10_000, now))
        assertEquals("5m ago", Formatters.formatRelativeTime(now - 5 * 60_000, now))
        assertEquals("3h ago", Formatters.formatRelativeTime(now - 3 * 60 * 60_000, now))
        assertEquals("2d ago", Formatters.formatRelativeTime(now - 2L * 24 * 60 * 60_000, now))
    }

    @Test
    fun `percent is clamped`() {
        assertEquals("0%", Formatters.formatPercent(-5f))
        assertEquals("100%", Formatters.formatPercent(140f))
        assertEquals("55%", Formatters.formatPercent(55.4f))
    }
}
