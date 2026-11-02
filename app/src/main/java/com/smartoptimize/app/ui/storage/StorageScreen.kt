package com.smartoptimize.app.ui.storage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.smartoptimize.app.ui.components.DonutChart
import com.smartoptimize.app.ui.components.LabelValueRow
import com.smartoptimize.app.ui.components.SETTINGS_OPEN_FAILED
import com.smartoptimize.app.ui.components.SectionCard
import com.smartoptimize.app.ui.components.showToast
import com.smartoptimize.app.utils.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    viewModel: StorageViewModel,
    onBack: () -> Unit,
    onOpenApp: (String) -> Unit,
    onOpenPermissionGuide: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("STORAGE MANAGER", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.storage == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null && state.storage == null -> {
                Column(Modifier.padding(padding).padding(24.dp)) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }

            else -> {
                val storage = state.storage!!
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    item {
                        SectionCard(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DonutChart(
                                    fraction = storage.usedPercent / 100f,
                                    centerLabel = Formatters.formatPercent(storage.usedPercent),
                                    size = 140.dp
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    LabelValueRow("Total", Formatters.formatBytes(storage.totalBytes))
                                    LabelValueRow("Used", Formatters.formatBytes(storage.usedBytes))
                                    LabelValueRow("Free", Formatters.formatBytes(storage.availableBytes))
                                    LabelValueRow(
                                        "State",
                                        when {
                                            storage.usedPercent >= 90f -> "Almost full"
                                            storage.usedPercent >= 80f -> "Getting full"
                                            else -> "Healthy"
                                        }
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (!SettingsNavigator.openStorageSettings(context)) {
                                        showToast(context, SETTINGS_OPEN_FAILED)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Open Android storage settings") }
                        }
                    }

                    if (!state.hasUsageAccess) {
                        item {
                            SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text("Per-app sizes need Usage Access", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Android only shares each app's storage size with apps that " +
                                        "have Usage Access. Grant it to see the largest apps and " +
                                        "rarely used app suggestions here.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(10.dp))
                                Button(onClick = onOpenPermissionGuide) { Text("How to enable") }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    } else {
                        if (state.largestApps.isNotEmpty()) {
                            item {
                                Text(
                                    "Largest apps (app + data + cache)",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                            items(state.largestApps, key = { "largest_" + it.packageName }) { app ->
                                AppRow(
                                    app = app,
                                    hasUsageData = true,
                                    nowMillis = state.nowMillis,
                                    onClick = { onOpenApp(app.packageName) }
                                )
                            }
                        }
                        if (state.rarelyUsedApps.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Rarely used apps — review candidates",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                                Text(
                                    "No recorded use in over 30 days. Uninstalling apps you no " +
                                        "longer need frees real storage; nothing is removed for you.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            items(state.rarelyUsedApps, key = { "rare_" + it.packageName }) { app ->
                                AppRow(
                                    app = app,
                                    hasUsageData = true,
                                    nowMillis = state.nowMillis,
                                    onClick = { onOpenApp(app.packageName) }
                                )
                            }
                        }
                    }

                    item {
                        SectionCard(modifier = Modifier.padding(16.dp)) {
                            Text("About other apps' cache", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "A normal Android app cannot delete another app's cache or data. " +
                                    "To clear an app's cache, open its detail screen here, tap " +
                                    "“Open App Info”, then use Storage → Clear cache in Android Settings.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
