package com.example.emergencynow.ui.feature.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.usecase.call.GetUserCallsUseCase
import com.example.emergencynow.ui.util.AuthSession
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
    private val getUserCallsUseCase: GetUserCallsUseCase
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
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Failed to load call history",
                            isLoading = false
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error loading user calls", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "An unexpected error occurred",
                    isLoading = false
                )
            }
        }
    }

    fun retry() {
        loadUserCalls()
    }
}
