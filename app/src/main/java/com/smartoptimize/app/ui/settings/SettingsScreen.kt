package com.smartoptimize.app.ui.settings

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartoptimize.app.permissions.SettingsNavigator
import com.smartoptimize.app.ui.components.LabelValueRow
import com.smartoptimize.app.ui.components.SETTINGS_OPEN_FAILED
import com.smartoptimize.app.ui.components.SectionCard
import com.smartoptimize.app.ui.components.showToast

@Composable
fun rememberAppVersionName(): String {
    val context = LocalContext.current
    return remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (e: PackageManager.NameNotFoundException) {
            "?"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenPermissionGuide: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val versionName = rememberAppVersionName()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SETTINGS", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SectionCard {
                SettingsRowTitle(Icons.Filled.Lock, "Usage Access")
                Spacer(Modifier.height(6.dp))
                LabelValueRow(
                    "Status",
                    if (state.hasUsageAccess) "Granted" else "Not granted"
                )
                Text(
                    "Optional. Enables last-used times, usage duration, per-app storage " +
                        "size and Focus Mode detection. Data stays on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row {
                    Button(onClick = {
                        if (!SettingsNavigator.openUsageAccessSettings(context)) {
                            showToast(context, SETTINGS_OPEN_FAILED)
                        }
                    }) { Text("Open Usage Access settings") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onOpenPermissionGuide) { Text("Guide") }
                }
            }

            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.TrackChanges,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Foreground App Focus", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Marks Focus Mode as active. It never stops apps by itself — " +
                                "it only helps you review them.",
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

            SectionCard {
                SettingsRowTitle(Icons.Filled.Shield, "Protected apps")
                Spacer(Modifier.height(6.dp))
                LabelValueRow("Apps you marked to keep", state.protectedCount.toString())
                Text(
                    "Manage the protected list from any app's detail screen. Your default " +
                        "launcher and Smart Optimize itself are always protected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenAbout),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("About Smart Optimize", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Version $versionName • Privacy & limitations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsRowTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

// ---------------------------------------------------------------------------
// PERMISSION GUIDE
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permission guide", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SectionCard {
                Text("Why Usage Access?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Android treats app-usage history as sensitive, so it sits behind a " +
                        "special switch instead of a normal permission dialog. With it, " +
                        "Smart Optimize can show:\n" +
                        "• When each app was last used and for how long (foreground)\n" +
                        "• How much storage each app occupies\n" +
                        "• Which app you used most recently (Focus Mode)\n" +
                        "• Which apps appear rarely used\n\n" +
                        "Without it, the dashboard, storage totals and battery info still work.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SectionCard {
                Text("How to enable it", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "1. Tap the button below to open the Usage Access page.\n" +
                        "2. Find “Smart Optimize” in the list.\n" +
                        "3. Turn on “Permit usage access”.\n" +
                        "4. Come back to Smart Optimize — the data appears automatically.\n\n" +
                        "You can turn it off again at any time in the same place.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (!SettingsNavigator.openUsageAccessSettings(context)) {
                            showToast(context, SETTINGS_OPEN_FAILED)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Open Usage Access settings") }
            }

            SectionCard {
                Text("Privacy", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Usage data is read on this device only, used to build the lists you " +
                        "see, and never uploaded anywhere. Smart Optimize does not request " +
                        "the INTERNET permission, has no accounts, no ads and no analytics SDKs. " +
                        "It also never asks for contacts, SMS, calls or location.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// ABOUT
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val versionName = rememberAppVersionName()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SectionCard {
                Text("Smart Optimize", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Version $versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "A lightweight, honest device companion: real RAM, storage and battery " +
                        "readings, visibility into your installed apps, and guided shortcuts " +
                        "to the official Android settings where you stay in control.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SectionCard {
                Text("Privacy policy (short version)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "• No account, no sign-in.\n" +
                        "• No INTERNET permission — nothing leaves your phone.\n" +
                        "• No ads, no trackers, no analytics.\n" +
                        "• Stored locally: only your Protected-app choices and the Focus Mode switch.\n" +
                        "• Usage Access is optional and can be revoked at any time.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SectionCard {
                Text("What this app cannot do (by Android design)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "• Force-stop other apps or kill their processes.\n" +
                        "• Delete other apps' cache or data.\n" +
                        "• Disable system apps or change protected system settings.\n" +
                        "• Measure exact per-app battery drain (only the system can).\n\n" +
                        "Where Android draws that line, Smart Optimize opens the official " +
                        "Settings page instead of pretending.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
