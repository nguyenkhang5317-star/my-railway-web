package com.smartoptimize.app.ui.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.model.BatteryInfo
import com.smartoptimize.app.domain.repository.AppsRepository
import com.smartoptimize.app.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BatteryUiState(
    val isLoading: Boolean = true,
    val battery: BatteryInfo? = null,
    val hasUsageAccess: Boolean = false,
    val recentApps: List<AppEntry> = emptyList(),
    val nowMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

class BatteryViewModel(
    private val deviceRepository: DeviceRepository,
    private val appsRepository: AppsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BatteryUiState())
    val state: StateFlow<BatteryUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val dashboard = deviceRepository.loadDashboard()
                val hasAccess = appsRepository.hasUsageAccess()
                val recent = if (hasAccess) {
                    try {
                        appsRepository.loadApps()
                            .filter { (it.lastTimeUsedMillis ?: 0L) > 0 }
                            .sortedByDescending { it.lastTimeUsedMillis }
                            .take(10)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                _state.value = BatteryUiState(
                    isLoading = false,
                    battery = dashboard.battery,
                    hasUsageAccess = hasAccess,
                    recentApps = recent,
                    nowMillis = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _state.value = BatteryUiState(
                    isLoading = false,
                    errorMessage = "Could not read battery information: ${e.message ?: "unknown error"}"
                )
            }
        }
    }
}
