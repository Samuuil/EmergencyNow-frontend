package com.example.emergencynow.ui.feature.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.usecase.call.GetUserCallsUseCase
import com.example.emergencynow.ui.util.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val calls: List<Call> = emptyList()
)

class HistoryViewModel(
    private val getUserCallsUseCase: GetUserCallsUseCase,
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadUserCalls()
    }

    fun loadUserCalls() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = getUserCallsUseCase(page = 1, limit = 50)
                result.fold(
                    onSuccess = { calls ->
                        _uiState.value = _uiState.value.copy(
                            calls = calls,
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        Log.e("HistoryViewModel", "Failed to load user calls", exception)
                        val message = exception.message ?: "Failed to load call history"
                        _uiState.value = _uiState.value.copy(error = message, isLoading = false)
                        notificationManager.showError(message)
                    }
                )
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error loading user calls", e)
                val message = e.message ?: "An unexpected error occurred"
                _uiState.value = _uiState.value.copy(error = message, isLoading = false)
                notificationManager.showError(message)
            }
        }
    }

    fun retry() {
        loadUserCalls()
    }
}
