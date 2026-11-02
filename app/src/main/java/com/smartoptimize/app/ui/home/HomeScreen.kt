package com.smartoptimize.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartoptimize.app.ui.components.DonutChart
import com.smartoptimize.app.ui.components.LabelValueRow
import com.smartoptimize.app.ui.components.SectionCard
import com.smartoptimize.app.utils.Formatters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenSettings: () -> Unit,
    onOpenOptimize: () -> Unit,
    onOpenStorage: () -> Unit,
    onOpenFocus: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMART OPTIMIZE", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.dashboard == null -> {
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            state.errorMessage != null && state.dashboard == null -> {
                Column(
                    modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }

            state.dashboard != null -> {
                val data = state.dashboard!!
                val updatedTime = SimpleDateFormat("HH:mm:ss", Locale.US)
                    .format(Date(data.updatedAtMillis))

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    // ---- Device status header -------------------------------
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PhoneAndroid, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Device status", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        LabelValueRow("Device", data.device.displayName)
                        LabelValueRow(
                            "Android",
                            "${data.device.androidVersion} (API ${data.device.sdkInt})"
                        )
                        if (data.device.supportedAbis.isNotEmpty()) {
                            LabelValueRow("CPU ABI", data.device.supportedAbis.first())
                        }
                        LabelValueRow("Last updated", updatedTime)
                    }

                    // ---- RAM ---------------------------------------------------
                    SectionCard {
                        CardTitle(Icons.Filled.Memory, "RAM")
                        Spacer(Modifier.height(8.dp))
                        LabelValueRow("Total", Formatters.formatBytes(data.ram.totalBytes))
                        LabelValueRow("Available", Formatters.formatBytes(data.ram.availableBytes))
                        LabelValueRow(
                            "Estimated usage",
                            Formatters.formatPercent(data.ram.usedPercent)
                        )
                        LabelValueRow(
                            "Memory state",
                            if (data.ram.isLowMemory) "Low (system reported)" else "Normal"
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (data.ram.usedPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Values come from Android's ActivityManager. “Available” is the " +
                                "memory Android can hand to new processes right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // ---- Storage ------------------------------------------------
                    SectionCard {
                        CardTitle(Icons.Filled.Storage, "STORAGE")
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(
                                fraction = data.storage.usedPercent / 100f,
                                centerLabel = Formatters.formatPercent(data.storage.usedPercent)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                LabelValueRow("Total", Formatters.formatBytes(data.storage.totalBytes))
                                LabelValueRow("Used", Formatters.formatBytes(data.storage.usedBytes))
                                LabelValueRow("Free", Formatters.formatBytes(data.storage.availableBytes))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onOpenStorage,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Open Storage Manager") }
                    }

                    // ---- Battery ------------------------------------------------
                    SectionCard {
                        CardTitle(
                            if (data.battery.isCharging) Icons.Filled.BatteryChargingFull
                            else Icons.Filled.BatteryFull,
                            "BATTERY"
                        )
                        Spacer(Modifier.height(8.dp))
                        LabelValueRow(
                            "Level",
                            data.battery.levelPercent?.let { "$it%" } ?: "Not reported"
                        )
                        LabelValueRow(
                            "Status",
                            if (data.battery.isCharging) "Charging" else "Not charging"
                        )
                        LabelValueRow(
                            "Temperature",
                            data.battery.temperatureCelsius?.let { "$it°C" } ?: "Not reported"
                        )
                        LabelValueRow(
                            "Voltage",
                            data.battery.voltageMillivolts?.let { "$it mV" } ?: "Not reported"
                        )
                    }

                    // ---- Primary actions ----------------------------------------
                    Button(
                        onClick = onOpenOptimize,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Filled.Speed, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("SMART OPTIMIZE NOW")
                    }
                    OutlinedButton(
                        onClick = onOpenFocus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.TrackChanges, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Foreground App Focus")
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CardTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}
