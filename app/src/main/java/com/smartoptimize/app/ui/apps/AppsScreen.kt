package com.smartoptimize.app.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartoptimize.app.domain.model.AppFilter
import com.smartoptimize.app.ui.components.AppRow
import com.smartoptimize.app.ui.components.EmptyState
import com.smartoptimize.app.ui.components.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    viewModel: AppsViewModel,
    onOpenApp: (String) -> Unit,
    onOpenFocus: () -> Unit,
    onOpenPermissionGuide: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BACKGROUND APP MANAGER", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenFocus) {
                        Icon(Icons.Filled.TrackChanges, contentDescription = "Foreground App Focus")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppFilter.entries.toList()) { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { viewModel.onFilterChange(filter) },
                        label = { Text(filter.title) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading && state.allApps.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null && state.allApps.isEmpty() -> {
                    Column(Modifier.padding(24.dp)) {
                        Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (!state.hasUsageAccess) {
                            item {
                                SectionCard(modifier = Modifier.padding(16.dp)) {
                                    Icon(Icons.Filled.Info, contentDescription = null)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Usage Access is off",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "The list below shows launchable apps, but last-used " +
                                            "times, usage duration and “rarely used” grouping need " +
                                            "Usage Access. Recently used also does not mean an " +
                                            "app's process is running right now.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Button(onClick = onOpenPermissionGuide) {
                                        Text("How to enable")
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                "${state.visibleApps.size} app(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        if (state.visibleApps.isEmpty()) {
                            item {
                                EmptyState(
                                    message = if (state.query.isNotBlank())
                                        "No apps match your search."
                                    else
                                        "No apps in this group."
                                )
                            }
                        } else {
                            items(state.visibleApps, key = { it.packageName }) { app ->
                                AppRow(
                                    app = app,
                                    hasUsageData = state.hasUsageAccess,
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
}
