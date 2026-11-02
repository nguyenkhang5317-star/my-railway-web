package com.smartoptimize.app.ui.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.model.StorageInfo
import com.smartoptimize.app.domain.repository.AppsRepository
import com.smartoptimize.app.domain.repository.DeviceRepository
import com.smartoptimize.app.domain.usecase.AppGrouping
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StorageUiState(
    val isLoading: Boolean = true,
    val storage: StorageInfo? = null,
    val hasUsageAccess: Boolean = false,
    /** Largest apps first (sizes only exist with Usage Access). */
    val largestApps: List<AppEntry> = emptyList(),
    val rarelyUsedApps: List<AppEntry> = emptyList(),
    val nowMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

class StorageViewModel(
    private val deviceRepository: DeviceRepository,
    private val appsRepository: AppsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StorageUiState())
    val state: StateFlow<StorageUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val dashboard = deviceRepository.loadDashboard()
                val hasAccess = appsRepository.hasUsageAccess()
                val now = System.currentTimeMillis()

                var largest: List<AppEntry> = emptyList()
                var rarelyUsed: List<AppEntry> = emptyList()
                if (hasAccess) {
                    val apps = try {
                        appsRepository.loadApps()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    val withSizes = apps.map { entry ->
                        entry.copy(totalSizeBytes = appsRepository.loadAppSize(entry.packageName))
                    }
                    largest = withSizes
                        .filter { (it.totalSizeBytes ?: 0L) > 0 }
                        .sortedByDescending { it.totalSizeBytes }
                        .take(15)
                    rarelyUsed = withSizes
                        .filter { AppGrouping.needsAttention(it, now, hasUsageData = true) }
                        .take(15)
                }

                _state.value = StorageUiState(
                    isLoading = false,
                    storage = dashboard.storage,
                    hasUsageAccess = hasAccess,
                    largestApps = largest,
                    rarelyUsedApps = rarelyUsed,
                    nowMillis = now
                )
            } catch (e: Exception) {
                _state.value = StorageUiState(
                    isLoading = false,
                    errorMessage = "Could not read storage information: ${e.message ?: "unknown error"}"
                )
            }
        }
    }
}
