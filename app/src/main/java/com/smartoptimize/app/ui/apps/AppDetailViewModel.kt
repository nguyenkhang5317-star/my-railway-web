package com.smartoptimize.app.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartoptimize.app.domain.model.AppEntry
import com.smartoptimize.app.domain.repository.AppsRepository
import com.smartoptimize.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppDetailUiState(
    val isLoading: Boolean = true,
    val app: AppEntry? = null,
    val errorMessage: String? = null
)

class AppDetailViewModel(
    private val packageName: String,
    private val appsRepository: AppsRepository,
    private val preferences: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppDetailUiState())
    val state: StateFlow<AppDetailUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val entry = appsRepository.loadApps().firstOrNull { it.packageName == packageName }
                val withSize = entry?.copy(
                    totalSizeBytes = appsRepository.loadAppSize(packageName)
                )
                _state.value = AppDetailUiState(
                    isLoading = false,
                    app = withSize,
                    errorMessage = if (withSize == null) "This app is no longer available." else null
                )
            } catch (e: Exception) {
                _state.value = AppDetailUiState(
                    isLoading = false,
                    errorMessage = "Could not load app details: ${e.message ?: "unknown error"}"
                )
            }
        }
    }

    fun setProtected(isProtected: Boolean) {
        viewModelScope.launch {
            preferences.setPackageProtected(packageName, isProtected)
            _state.update { current ->
                current.copy(app = current.app?.copy(isProtectedByUser = isProtected))
            }
        }
    }
}
