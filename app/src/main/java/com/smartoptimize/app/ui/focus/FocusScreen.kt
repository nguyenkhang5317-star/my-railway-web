package com.smartoptimize.app.ui.focus

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.permissions.SettingsNavigator
import com.smartoptimize.app.ui.components.AppIcon
import com.smartoptimize.app.ui.components.EmptyState
import com.smartoptimize.app.ui.components.SETTINGS_OPEN_FAILED
import com.smartoptimize.app.ui.components.SectionCard
import com.smartoptimize.app.ui.components.showToast
import com.smartoptimize.app.utils.Formatters

/**
 * FOREGROUND APP FOCUS.
 *
 * Android does not give normal apps the power to force-stop every other
 * application. This screen therefore does the honest, supported version:
 * it shows which app you used most recently (verified via Usage Access),
 * keeps your protected apps out of scope, and gives you one-tap access to
 * each other app's official App Info / battery pages so *you* decide what
 * to restrict. No simulated taps, no fake "all apps stopped" results.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
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
                title = { Text("FOREGROUND APP FOCUS", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                SectionCard(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Focus mode", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (state.focusEnabled)
                                    "On — review the apps below and manage them one by one."
                                else
                                    "Off — turn on to review other recently used apps.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.focusEnabled,
                            onCheckedChange = viewModel::setFocusEnabled
                        )
                    }
                }
            }

            item {
                SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("What this mode honestly does", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "• Shows your most recently used app and other apps used in the last 24 hours.\n" +
                            "• Apps you marked Protected, plus your default launcher, are always kept.\n" +
                            "• “Manage” opens each app's official Android App Info page, where you " +
                            "can check background battery use, restrict background activity where " +
                            "the device offers it, or force-stop the app yourself.\n" +
                            "• Smart Optimize cannot and does not stop other apps for you — Android " +
                            "reserves that power for the system and for you.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            if (!state.hasUsageAccess) {
                item {
                    SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Usage Access needed", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "To identify your most recently used app, Smart Optimize needs " +
                                "Usage Access. Without it, this screen cannot guess — and won't.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = onOpenPermissionGuide) { Text("How to enable") }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                item {
                    Text(
                        "Your most recently used app",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (state.mostRecentApp == null) {
                    item {
                        EmptyState("No app usage recorded in the last 24 hours.")
                    }
                } else {
                    item {
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            val app = state.mostRecentApp!!
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIcon(packageName = app.packageName, size = 48.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.label, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Used ${Formatters.formatRelativeTime(app.lastTimeUsedMillis, state.nowMillis)} — " +
                                            "this is the app Focus Mode keeps in the foreground.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(onClick = { onOpenApp(app.packageName) }) {
                                    Text("Details")
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                if (state.otherRecentApps.isNotEmpty()) {
                    item {
                        Text(
                            "Other apps used in the last 24 hours",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text(
                            "Tap Manage to review an app in Android Settings. Protected and " +
                                "system apps are labelled and are never auto-suggested for restriction.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    items(state.otherRecentApps, key = { it.packageName }) { app ->
                        FocusAppRow(
                            app = app,
                            nowMillis = state.nowMillis,
                            onManage = {
                                if (!SettingsNavigator.openAppDetails(context, app.packageName)) {
                                    showToast(context, SETTINGS_OPEN_FAILED)
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Guidance", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "1) In App Info, open Battery to see background usage.\n" +
                                "2) Where your device offers “Restrict” / background limits, apply " +
                                "them only to apps you do not need notifications from.\n" +
                                "3) Check auto-start settings if your brand provides them.\n" +
                                "4) Never restrict phone, messaging, keyboard, alarms, navigation, " +
                                "music you are playing, accessibility services or your launcher.\n" +
                                "5) Mark apps that must keep working as Protected in their detail screen.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun FocusAppRow(
    app: AppEntry,
    nowMillis: Long,
    onManage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(packageName = app.packageName)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(app.label, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            val label = when {
                app.isProtectedByUser -> "Protected — will be kept"
                app.isSystemApp -> "System app — review carefully"
                else -> "Last used: ${Formatters.formatRelativeTime(app.lastTimeUsedMillis, nowMillis)}"
            }
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (app.isProtectedByUser) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = "Protected",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(onClick = onManage) { Text("Manage") }
        }
    }
}
