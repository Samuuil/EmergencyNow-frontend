package com.example.emergencynow.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.usecase.auth.RequestVerificationCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EnterEgnViewModel(
    private val requestVerificationCodeUseCase: RequestVerificationCodeUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(EnterEgnUIState())
    val state = _state.asStateFlow()
    
    fun onAction(action: EnterEgnAction) {
        when (action) {
            is EnterEgnAction.OnEgnChanged -> {
                _state.update { it.copy(egn = action.egn, error = null) }
            }
            
            is EnterEgnAction.OnContinueClicked -> {
                requestVerificationCode()
            }
            
            is EnterEgnAction.OnErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
            
            is EnterEgnAction.OnNavigationHandled -> {
                _state.update { it.copy(shouldNavigateToVerification = false) }
            }
        }
    }
    
    private fun requestVerificationCode() {
        val egn = _state.value.egn

        if (egn.length != 10 || !egn.all { it.isDigit() }) {
            _state.update { it.copy(error = "EGN must be exactly 10 digits") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            requestVerificationCodeUseCase(egn = egn, method = "sms").fold(
                onSuccess = { message ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            shouldNavigateToVerification = true
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to send verification code"
                        )
                    }
                }
            )
        }
    }
}
