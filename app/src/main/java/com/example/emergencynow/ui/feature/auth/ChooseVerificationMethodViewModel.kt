package com.example.emergencynow.ui.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.request.LoginMethod
import com.example.emergencynow.domain.usecase.auth.RequestVerificationCodeUseCase
import com.example.emergencynow.ui.util.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChooseVerificationMethodUiState(
    val isLoading: Boolean = false
)

class ChooseVerificationMethodViewModel(
    private val requestVerificationCodeUseCase: RequestVerificationCodeUseCase,
    private val notificationManager: com.example.emergencynow.ui.util.NotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChooseVerificationMethodUiState())
    val uiState: StateFlow<ChooseVerificationMethodUiState> = _uiState.asStateFlow()

    fun requestVerificationCode(method: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val currentEgn = AuthSession.egn
                if (currentEgn.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    notificationManager.showError("Missing EGN. Go back and enter it again.")
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLoading = true)
                
                AuthSession.lastMethod = if (method == "sms") LoginMethod.SMS else LoginMethod.EMAIL
                requestVerificationCodeUseCase(egn = currentEgn, method = method).getOrThrow()
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                Log.e("ChooseVerificationMethodViewModel", "Failed to send verification code", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
                notificationManager.showError("Failed to send verification code.")
            }
        }
    }


}


