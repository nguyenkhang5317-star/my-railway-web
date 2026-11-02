package com.smartoptimize.app.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.repository.AppsRepository
import com.smartoptimize.app.domain.repository.PreferencesRepository
import com.smartoptimize.app.domain.usecase.AppGrouping
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusUiState(
    val isLoading: Boolean = true,
    val focusEnabled: Boolean = false,
    val hasUsageAccess: Boolean = false,
    val ownPackageName: String = "",
    /** Most recently used app other than Smart Optimize itself (verifiable data only). */
    val mostRecentApp: AppEntry? = null,
    /** Other recently used apps the user may review. */
    val otherRecentApps: List<AppEntry> = emptyList(),
    val nowMillis: Long = System.currentTimeMillis()
)

class FocusViewModel(
    private val appsRepository: AppsRepository,
    private val preferences: PreferencesRepository,
    private val ownPackageName: String
) : ViewModel() {

    private val _state = MutableStateFlow(FocusUiState(ownPackageName = ownPackageName))
    val state: StateFlow<FocusUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.focusModeEnabled.collect { enabled ->
                _state.update { it.copy(focusEnabled = enabled) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val now = System.currentTimeMillis()
            val hasAccess = appsRepository.hasUsageAccess()
            val apps = try {
                appsRepository.loadApps()
            } catch (e: Exception) {
                emptyList()
            }
            val recent = apps
                .filter { it.packageName != ownPackageName && AppGrouping.isRecent(it, now) }
                .sortedByDescending { it.lastTimeUsedMillis ?: 0L }
            _state.update {
                it.copy(
                    isLoading = false,
                    hasUsageAccess = hasAccess,
                    mostRecentApp = recent.firstOrNull(),
                    otherRecentApps = recent.drop(1),
                    nowMillis = now
                )
            }
        }
    }

    fun setFocusEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setFocusModeEnabled(enabled) }
    }
}
