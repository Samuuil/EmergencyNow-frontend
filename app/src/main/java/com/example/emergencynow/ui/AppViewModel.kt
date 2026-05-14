package com.example.emergencynow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.usecase.auth.RefreshTokenUseCase
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.FcmTokenRegistrar
import com.example.emergencynow.ui.util.parseJwt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StartupState {
    data object Loading : StartupState
    data object Authenticated : StartupState
    data object Unauthenticated : StartupState
}

class AppViewModel(
    private val authStorage: AuthStorage,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val fcmTokenRegistrar: FcmTokenRegistrar,
) : ViewModel() {

    private val _startupState = MutableStateFlow<StartupState>(StartupState.Loading)
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    init {
        resolveStartDestination()
    }

    private fun resolveStartDestination() {
        viewModelScope.launch {
            val refreshToken = authStorage.refreshToken
            if (refreshToken == null) {
                _startupState.value = StartupState.Unauthenticated
                return@launch
            }
            refreshTokenUseCase(refreshToken).fold(
                onSuccess = { token ->
                    authStorage.accessToken = token.accessToken
                    authStorage.refreshToken = token.refreshToken
                    AuthSession.userId = parseJwt(token.accessToken)?.sub
                    fcmTokenRegistrar.fetchAndRegister()
                    _startupState.value = StartupState.Authenticated
                },
                onFailure = {
                    authStorage.clear()
                    _startupState.value = StartupState.Unauthenticated
                }
            )
        }
    }
}
