package com.smartoptimize.app.ui.optimize

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartoptimize.app.domain.model.Recommendation
import com.smartoptimize.app.domain.model.RecommendationAction
import com.smartoptimize.app.domain.model.RecommendationSeverity
import com.smartoptimize.app.permissions.SettingsNavigator
import com.smartoptimize.app.ui.components.SETTINGS_OPEN_FAILED
import com.smartoptimize.app.ui.components.SectionCard
import com.smartoptimize.app.ui.components.showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizeScreen(
    viewModel: OptimizeViewModel,
    onBack: () -> Unit,
    onReviewApps: () -> Unit,
    onOpenPermissionGuide: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMART OPTIMIZE", fontWeight = FontWeight.Bold) },
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
                Text("What gets checked", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "1. Internal storage space\n" +
                        "2. Available RAM\n" +
                        "3. Battery level and temperature\n" +
                        "4. Rarely used apps (only with Usage Access)\n" +
                        "5. Missing optional permissions\n\n" +
                        "OPTIMIZE NOW re-measures your device and produces recommendations. " +
                        "You choose which follow-up action to open — nothing is “cleaned”, " +
                        "nothing is force-stopped, and no fake progress or success message is shown.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            state.errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            when (state.phase) {
                OptimizePhase.RUNNING -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator()
                        Spacer(Modifier.width(12.dp))
                        Text("Measuring device data…")
                    }
                }

                OptimizePhase.IDLE -> {
                    Button(
                        onClick = viewModel::runAnalysis,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Filled.Speed, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("OPTIMIZE NOW")
                    }
                }

                OptimizePhase.DONE -> {
                    Text(
                        "Recommendations from the latest measurement",
                        style = MaterialTheme.typography.titleMedium
                    )
                    state.recommendations.forEach { recommendation ->
                        RecommendationCard(
                            recommendation = recommendation,
                            onAction = {
                                when (recommendation.action) {
                                    RecommendationAction.OPEN_STORAGE_SETTINGS ->
                                        if (!SettingsNavigator.openStorageSettings(context)) {
                                            showToast(context, SETTINGS_OPEN_FAILED)
                                        }

                                    RecommendationAction.OPEN_BATTERY_SETTINGS ->
                                        if (!SettingsNavigator.openBatterySettings(context)) {
                                            showToast(context, SETTINGS_OPEN_FAILED)
                                        }

                                    RecommendationAction.REVIEW_APPS -> onReviewApps()
                                    RecommendationAction.GRANT_USAGE_ACCESS -> onOpenPermissionGuide()
                                    RecommendationAction.NONE -> Unit
                                }
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = viewModel::runAnalysis,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Measure again") }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: Recommendation,
    onAction: () -> Unit
) {
    val (icon, tint) = when (recommendation.severity) {
        RecommendationSeverity.CRITICAL ->
            Icons.Filled.Error to MaterialTheme.colorScheme.error

        RecommendationSeverity.WARNING ->
            Icons.Filled.Warning to MaterialTheme.colorScheme.secondary

        RecommendationSeverity.INFO ->
            Icons.Filled.Info to MaterialTheme.colorScheme.primary
    }
    val actionLabel = when (recommendation.action) {
        RecommendationAction.OPEN_STORAGE_SETTINGS -> "Open storage settings"
        RecommendationAction.OPEN_BATTERY_SETTINGS -> "Open battery settings"
        RecommendationAction.REVIEW_APPS -> "Review apps"
        RecommendationAction.GRANT_USAGE_ACCESS -> "How to grant"
        RecommendationAction.NONE -> null
    }

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(Modifier.width(8.dp))
            Text(
                recommendation.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (recommendation.action == RecommendationAction.NONE &&
                recommendation.severity == RecommendationSeverity.INFO
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "OK",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(recommendation.description, style = MaterialTheme.typography.bodyMedium)
        if (actionLabel != null) {
            Spacer(Modifier.height(10.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
