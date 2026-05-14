package com.example.emergencynow.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.usecase.auth.GetUserOnboardingStateUseCase
import com.example.emergencynow.domain.usecase.auth.OnboardingState
import com.example.emergencynow.domain.usecase.auth.RequestVerificationCodeUseCase
import com.example.emergencynow.domain.usecase.auth.VerifyCodeUseCase
import com.example.emergencynow.ui.util.parseJwt
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.FcmTokenRegistrar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerifyCodeViewModel(
    private val verifyCodeUseCase: VerifyCodeUseCase,
    private val requestVerificationCodeUseCase: RequestVerificationCodeUseCase,
    private val authStorage: AuthStorage,
    private val getOnboardingStateUseCase: GetUserOnboardingStateUseCase,
    private val notificationManager: com.example.emergencynow.ui.util.NotificationManager,
    private val fcmTokenRegistrar: FcmTokenRegistrar,
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyCodeUIState())
    val state = _state.asStateFlow()

    fun setEgn(egn: String) {
        _state.update { it.copy(egn = egn) }
    }

    fun onAction(action: VerifyCodeAction) {
        when (action) {
            is VerifyCodeAction.OnCodeChanged -> {
                _state.update { it.copy(code = action.code) }
            }

            is VerifyCodeAction.OnVerifyClicked -> {
                verifyCode()
            }

            is VerifyCodeAction.OnResendClicked -> {
                resendCode()
            }
        }
    }

    private fun verifyCode() {
        val egn = _state.value.egn
        val code = _state.value.code

        if (egn.isEmpty()) {
            notificationManager.showError("Missing EGN. Please go back.")
            return
        }

        if (code.length != 6 || !code.all { it.isDigit() }) {
            notificationManager.showError("Code must be exactly 6 digits")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            verifyCodeUseCase(egn = egn, code = code).fold(
                onSuccess = { token ->
                    authStorage.accessToken = token.accessToken
                    authStorage.refreshToken = token.refreshToken
                    val payload = parseJwt(token.accessToken)
                    AuthSession.userId = payload?.sub

                    fcmTokenRegistrar.fetchAndRegister()

                    val isReturningUser = checkIfReturningUser()

                    _state.update {
                        it.copy(
                            isLoading = false,
                            isVerified = true,
                            isReturningUser = isReturningUser
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false) }
                    notificationManager.showError(error.message ?: "Invalid or expired verification code")
                }
            )
        }
    }

    private fun resendCode() {
        val egn = _state.value.egn

        if (egn.isEmpty()) {
            notificationManager.showError("Missing EGN. Please go back.")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val method = AuthSession.lastMethod?.name?.lowercase() ?: "sms"
            requestVerificationCodeUseCase(egn = egn, method = method).fold(
                onSuccess = { _ ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            code = ""
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false) }
                    notificationManager.showError(error.message ?: "Failed to resend code")
                }
            )
        }
    }

    private suspend fun checkIfReturningUser(): Boolean {
        return getOnboardingStateUseCase().getOrNull() is OnboardingState.ReturningUser
    }
}
