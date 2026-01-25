package com.example.emergencynow.ui.feature.auth
data class EnterEgnUIState(
    val egn: String = "",
    val isLoading: Boolean = false,
    val shouldNavigateToVerification: Boolean = false
)

sealed interface EnterEgnAction {
    data class OnEgnChanged(val egn: String) : EnterEgnAction
    data object OnContinueClicked : EnterEgnAction

    data object OnNavigationHandled : EnterEgnAction
}

data class VerifyCodeUIState(
    val egn: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val isReturningUser: Boolean = false
)

sealed interface VerifyCodeAction {
    data class OnCodeChanged(val code: String) : VerifyCodeAction
    data object OnVerifyClicked : VerifyCodeAction
    data object OnResendClicked : VerifyCodeAction
}
