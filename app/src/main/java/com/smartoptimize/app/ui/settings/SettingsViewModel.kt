package com.smartoptimize.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartoptimize.app.domain.repository.AppsRepository
import com.smartoptimize.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val hasUsageAccess: Boolean = false,
    val focusEnabled: Boolean = false,
    val protectedCount: Int = 0
)

class SettingsViewModel(
    private val appsRepository: AppsRepository,
    private val preferences: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.focusModeEnabled.collect { enabled ->
                _state.update { it.copy(focusEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferences.protectedPackages.collect { packages ->
                _state.update { it.copy(protectedCount = packages.size) }
            }
        }
    }

    /** Usage Access can change while the user is in Settings — re-check on open. */
    fun refresh() {
        _state.update { it.copy(hasUsageAccess = appsRepository.hasUsageAccess()) }
    }

    fun setFocusEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setFocusModeEnabled(enabled) }
    }
}
