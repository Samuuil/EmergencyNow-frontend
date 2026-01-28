package com.example.emergencynow.ui.feature.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.example.emergencynow.ui.util.NotificationManager

class EnterEgnViewModel(
    private val notificationManager: NotificationManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(EnterEgnUIState())
    val state = _state.asStateFlow()
    
    fun onAction(action: EnterEgnAction) {
        when (action) {
            is EnterEgnAction.OnEgnChanged -> {
                _state.update { it.copy(egn = action.egn) }
            }
            
            is EnterEgnAction.OnContinueClicked -> {
                validateAndNavigate()
            }
            

            is EnterEgnAction.OnNavigationHandled -> {
                _state.update { it.copy(shouldNavigateToVerification = false) }
            }
        }
    }
    
    private fun validateAndNavigate() {
        val egn = _state.value.egn

        if (egn.length != 10 || !egn.all { it.isDigit() }) {
            notificationManager.showError("EGN must be exactly 10 digits")
            return
        }
        
        _state.update { 
            it.copy(
                shouldNavigateToVerification = true
            )
        }
    }
}
