package com.smartoptimize.app.ui.apps

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.smartoptimize.app.ui.components.AppIcon
import com.smartoptimize.app.ui.components.LabelValueRow
import com.smartoptimize.app.ui.components.SETTINGS_OPEN_FAILED
import com.smartoptimize.app.ui.components.SectionCard
import com.smartoptimize.app.ui.components.showToast
import com.smartoptimize.app.utils.Formatters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    viewModel: AppDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.app == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.app == null -> {
                Column(Modifier.padding(padding).padding(24.dp)) {
                    Text(
                        state.errorMessage ?: "App not found.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onBack) { Text("Go back") }
                }
            }

            else -> {
                val app = state.app!!
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val now = System.currentTimeMillis()

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(packageName = app.packageName, size = 56.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(app.label, style = MaterialTheme.typography.titleLarge)
                            Text(
                                app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    SectionCard {
                        Text("Information", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        LabelValueRow("Type", if (app.isSystemApp) "System app" else "User app")
                        LabelValueRow("Version", app.versionName ?: "Not reported")
                        LabelValueRow(
                            "Installed",
                            app.firstInstallMillis?.let { dateFormat.format(Date(it)) }
                                ?: "Not reported"
                        )
                        LabelValueRow(
                            "Last updated",
                            app.lastUpdateMillis?.let { dateFormat.format(Date(it)) }
                                ?: "Not reported"
                        )
                        LabelValueRow(
                            "Storage used",
                            app.totalSizeBytes?.let { Formatters.formatBytes(it) }
                                ?: "Needs Usage Access"
                        )
                    }

                    SectionCard {
                        Text("Usage (last 30 days)", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        LabelValueRow(
                            "Last used",
                            Formatters.formatRelativeTime(app.lastTimeUsedMillis, now)
                        )
                        LabelValueRow(
                            "Foreground time",
                            app.totalForegroundMillis?.let { "~${Formatters.formatDuration(it)}" }
                                ?: "No data"
                        )
                        Text(
                            "Usage records show when the app was in the foreground. They do " +
                                "not prove that its process is running at this moment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Keep this app (protected)", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Protected apps are never suggested for restriction in " +
                                        "Smart Optimize or Focus Mode.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = app.isProtectedByUser,
                                onCheckedChange = viewModel::setProtected
                            )
                        }
                    }

                    SectionCard {
                        Text("Manage in Android Settings", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Android does not allow a normal app to force-stop other apps, clear " +
                                "their cache or restrict their background work. Use the official " +
                                "App Info page to review permissions, battery usage, storage/" +
                                "cache, or to force-stop/uninstall the app yourself.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (!SettingsNavigator.openAppDetails(context, app.packageName)) {
                                    showToast(context, SETTINGS_OPEN_FAILED)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Open App Info")
                        }
                        if (app.isSystemApp) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "This is a system app. Restricting system, phone, messaging, " +
                                    "keyboard, alarm, launcher or accessibility apps can break " +
                                    "important functions — review carefully.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
