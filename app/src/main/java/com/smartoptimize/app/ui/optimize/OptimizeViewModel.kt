package com.smartoptimize.app.ui.optimize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartoptimize.app.domain.model.Recommendation
import com.smartoptimize.app.domain.repository.AppsRepository
import com.smartoptimize.app.domain.repository.DeviceRepository
import com.smartoptimize.app.domain.usecase.AnalyzeDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OptimizePhase { IDLE, RUNNING, DONE }

data class OptimizeUiState(
    val phase: OptimizePhase = OptimizePhase.IDLE,
    val recommendations: List<Recommendation> = emptyList(),
    val errorMessage: String? = null
)

class OptimizeViewModel(
    private val deviceRepository: DeviceRepository,
    private val appsRepository: AppsRepository,
    private val analyzeDevice: AnalyzeDeviceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OptimizeUiState())
    val state: StateFlow<OptimizeUiState> = _state.asStateFlow()

    /**
     * "OPTIMIZE NOW" performs a real re-measurement and analysis, then lets
     * the user pick individual follow-up actions. Nothing is claimed to be
     * "cleaned" or "boosted".
     */
    fun runAnalysis() {
        if (_state.value.phase == OptimizePhase.RUNNING) return
        viewModelScope.launch {
            _state.value = OptimizeUiState(phase = OptimizePhase.RUNNING)
            try {
                val dashboard = deviceRepository.loadDashboard()
                val apps = try {
                    appsRepository.loadApps()
                } catch (e: Exception) {
                    emptyList()
                }
                val recommendations = analyzeDevice(
                    dashboard = dashboard,
                    apps = apps,
                    hasUsageAccess = appsRepository.hasUsageAccess(),
                    nowMillis = System.currentTimeMillis()
                )
                _state.value = OptimizeUiState(
                    phase = OptimizePhase.DONE,
                    recommendations = recommendations
                )
            } catch (e: Exception) {
                _state.value = OptimizeUiState(
                    phase = OptimizePhase.IDLE,
                    errorMessage = "Analysis failed: ${e.message ?: "unknown error"}"
                )
            }
        }
    }
}
