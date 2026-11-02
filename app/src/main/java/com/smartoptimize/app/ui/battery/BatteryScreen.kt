package com.smartoptimize.app.ui.battery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartoptimize.app.permissions.SettingsNavigator
import com.smartoptimize.app.ui.components.AppRow
import com.smartoptimize.app.ui.components.LabelValueRow
import com.smartoptimize.app.ui.components.SETTINGS_OPEN_FAILED
import com.smartoptimize.app.ui.components.SectionCard
import com.smartoptimize.app.ui.components.showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel,
    onOpenApp: (String) -> Unit,
    onOpenPermissionGuide: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("BATTERY MANAGER", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        when {
            state.isLoading && state.battery == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null && state.battery == null -> {
                Column(Modifier.padding(padding).padding(24.dp)) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }

            else -> {
                val battery = state.battery!!
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    item {
                        SectionCard(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                imageVector = if (battery.isCharging)
                                    Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryFull,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                battery.levelPercent?.let { "$it%" } ?: "Level not reported",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            battery.levelPercent?.let { level ->
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { level / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            LabelValueRow(
                                "Status",
                                if (battery.isCharging) "Charging" else "Not charging"
                            )
                            LabelValueRow(
                                "Temperature",
                                battery.temperatureCelsius?.let { "$it°C" } ?: "Not reported"
                            )
                            LabelValueRow(
                                "Voltage",
                                battery.voltageMillivolts?.let { "$it mV" } ?: "Not reported"
                            )
                            LabelValueRow("Health (system)", battery.health ?: "Not reported")
                            LabelValueRow("Technology", battery.technology ?: "Not reported")
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Battery wear cannot be calculated from the current charge " +
                                    "percentage, so Smart Optimize never estimates it.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (!SettingsNavigator.openBatterySettings(context)) {
                                        showToast(context, SETTINGS_OPEN_FAILED)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Open Android battery settings") }
                        }
                    }

                    item {
                        Text(
                            "Apps used most recently",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    if (!state.hasUsageAccess) {
                        item {
                            SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    "Usage Access is off, so recent-app activity cannot be shown. " +
                                        "Android's own battery settings (button above) shows per-app " +
                                        "battery use calculated by the system.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(10.dp))
                                Button(onClick = onOpenPermissionGuide) { Text("How to enable") }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    } else if (state.recentApps.isEmpty()) {
                        item {
                            Text(
                                "No recent app usage recorded.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    } else {
                        item {
                            Text(
                                "Foreground activity is a hint, not a battery measurement. For exact " +
                                    "per-app battery consumption use Android battery settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(state.recentApps, key = { it.packageName }) { app ->
                            AppRow(
                                app = app,
                                hasUsageData = true,
                                nowMillis = state.nowMillis,
                                onClick = { onOpenApp(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}
