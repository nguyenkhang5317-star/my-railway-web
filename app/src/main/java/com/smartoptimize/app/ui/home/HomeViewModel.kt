package com.smartoptimize.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartoptimize.app.domain.model.DashboardData
import com.smartoptimize.app.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val dashboard: DashboardData? = null,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    /** Data is refreshed on demand (screen open / Refresh tap) — never polled. */
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val data = deviceRepository.loadDashboard()
                _state.value = HomeUiState(isLoading = false, dashboard = data)
            } catch (e: Exception) {
                _state.value = HomeUiState(
                    isLoading = false,
                    errorMessage = "Could not read device information: ${e.message ?: "unknown error"}"
                )
            }
        }
    }
}
