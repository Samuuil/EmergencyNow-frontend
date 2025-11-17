package com.example.emergencynow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.emergencynow.ui.constants.Routes
import com.example.emergencynow.ui.feature.auth.WelcomeScreen
import com.example.emergencynow.ui.feature.auth.EnterEgnScreen
import com.example.emergencynow.ui.feature.auth.ChooseVerificationMethodScreen
import com.example.emergencynow.ui.feature.auth.EnterVerificationCodeScreen
import com.example.emergencynow.ui.feature.profile.PersonalInformationScreen
import com.example.emergencynow.ui.feature.contacts.EmergencyContactsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onRegisterEgn = { navController.navigate(Routes.ENTER_EGN) },
                onLogin = { navController.navigate(Routes.ENTER_EGN) }
            )
        }
        composable(Routes.ENTER_EGN) {
            EnterEgnScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate(Routes.CHOOSE_VERIFICATION) }
            )
        }
        composable(Routes.CHOOSE_VERIFICATION) {
            ChooseVerificationMethodScreen(
                onBack = { navController.popBackStack() },
                onPhone = { navController.navigate(Routes.ENTER_VERIFICATION_CODE) },
                onEmail = { navController.navigate(Routes.ENTER_VERIFICATION_CODE) }
            )
        }
        composable(Routes.ENTER_VERIFICATION_CODE) {
            EnterVerificationCodeScreen(
                onBack = { navController.popBackStack() },
                onVerified = { navController.navigate(Routes.PERSONAL_INFO) }
            )
        }
        composable(Routes.PERSONAL_INFO) {
            PersonalInformationScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate(Routes.EMERGENCY_CONTACTS) }
            )
        }
        composable(Routes.EMERGENCY_CONTACTS) {
            EmergencyContactsScreen(
                onBack = { navController.popBackStack() },
                onFinish = { /* TODO: Navigate to app home when available */ }
            )
        }
    }
}
