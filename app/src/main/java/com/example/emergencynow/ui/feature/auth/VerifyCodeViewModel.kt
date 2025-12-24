package com.example.emergencynow.ui.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.data.session.SessionManager
import com.example.emergencynow.domain.usecase.auth.RequestVerificationCodeUseCase
import com.example.emergencynow.domain.usecase.auth.VerifyCodeUseCase
import com.example.emergencynow.ui.util.parseJwt
import com.example.emergencynow.domain.usecase.contact.GetContactsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerifyCodeViewModel(
    private val verifyCodeUseCase: VerifyCodeUseCase,
    private val requestVerificationCodeUseCase: RequestVerificationCodeUseCase,
    private val sessionManager: SessionManager,
    private val getContactsUseCase: GetContactsUseCase,
    private val context: Context
) : ViewModel() {
    
    private val _state = MutableStateFlow(VerifyCodeUIState())
    val state = _state.asStateFlow()
    
    fun setEgn(egn: String) {
        _state.update { it.copy(egn = egn) }
    }
    
    fun onAction(action: VerifyCodeAction) {
        when (action) {
            is VerifyCodeAction.OnCodeChanged -> {
                _state.update { it.copy(code = action.code, error = null) }
            }
            
            is VerifyCodeAction.OnVerifyClicked -> {
                verifyCode()
            }
            
            is VerifyCodeAction.OnResendClicked -> {
                resendCode()
            }
            
            is VerifyCodeAction.OnErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
        }
    }
    
    private fun verifyCode() {
        val egn = _state.value.egn
        val code = _state.value.code
        
        if (egn.isEmpty()) {
            _state.update { it.copy(error = "Missing EGN. Please go back.") }
            return
        }
        
        if (code.length != 6 || !code.all { it.isDigit() }) {
            _state.update { it.copy(error = "Code must be exactly 6 digits") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            verifyCodeUseCase(egn = egn, code = code).fold(
                onSuccess = { token ->
                    sessionManager.setAuthTokens(token.accessToken, token.refreshToken)
                    
                    com.example.emergencynow.ui.util.AuthSession.accessToken = token.accessToken
                    com.example.emergencynow.ui.util.AuthSession.refreshToken = token.refreshToken
                    val payload = parseJwt(token.accessToken)
                    com.example.emergencynow.ui.util.AuthSession.userId = payload?.sub
                    
                    com.example.emergencynow.ui.util.AuthStorage.saveTokens(
                        context = context,
                        accessToken = token.accessToken,
                        refreshToken = token.refreshToken
                    )
                    
                    val isReturningUser = checkIfReturningUser(token.accessToken)
                    
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isVerified = true,
                            isReturningUser = isReturningUser
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Invalid or expired verification code"
                        )
                    }
                }
            )
        }
    }
    
    private fun resendCode() {
        val egn = _state.value.egn
        
        if (egn.isEmpty()) {
            _state.update { it.copy(error = "Missing EGN. Please go back.") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            requestVerificationCodeUseCase(egn = egn, method = "sms").fold(
                onSuccess = { _ ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            code = ""
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to resend code"
                        )
                    }
                }
            )
        }
    }
    
    private suspend fun checkIfReturningUser(accessToken: String): Boolean {
        return getContactsUseCase().getOrNull()?.isNotEmpty() == true
    }
}
