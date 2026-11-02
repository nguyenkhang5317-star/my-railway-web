package com.smartoptimize.app.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.model.AppFilter
import com.smartoptimize.app.domain.repository.AppsRepository
import com.smartoptimize.app.domain.usecase.AppGrouping
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppsUiState(
    val isLoading: Boolean = true,
    val hasUsageAccess: Boolean = false,
    val allApps: List<AppEntry> = emptyList(),
    val filter: AppFilter = AppFilter.ALL,
    val query: String = "",
    val nowMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    val visibleApps: List<AppEntry>
        get() = AppGrouping.filterApps(
            apps = allApps,
            filter = filter,
            query = query,
            nowMillis = nowMillis,
            hasUsageData = hasUsageAccess
        )
}

class AppsViewModel(
    private val appsRepository: AppsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppsUiState())
    val state: StateFlow<AppsUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val apps = appsRepository.loadApps()
                _state.update {
                    it.copy(
                        isLoading = false,
                        hasUsageAccess = appsRepository.hasUsageAccess(),
                        allApps = apps,
                        nowMillis = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not load the app list: ${e.message ?: "unknown error"}"
                    )
                }
            }
        }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun onFilterChange(filter: AppFilter) = _state.update { it.copy(filter = filter) }
}
