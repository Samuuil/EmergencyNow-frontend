package com.example.emergencynow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.emergencynow.ui.constants.Routes
import com.example.emergencynow.ui.feature.auth.WelcomeScreen
import com.example.emergencynow.ui.feature.auth.EnterEgnScreen
import com.example.emergencynow.ui.feature.auth.ChooseVerificationMethodScreen
import com.example.emergencynow.ui.feature.auth.EnterVerificationCodeScreen
import com.example.emergencynow.ui.feature.auth.HomeScreen
import com.example.emergencynow.ui.feature.auth.EmergencyCallScreen
import com.example.emergencynow.ui.feature.auth.CallTrackingScreen
import com.example.emergencynow.ui.feature.auth.ProfileHomeScreen
import com.example.emergencynow.ui.feature.auth.AmbulanceSelectionScreen
import com.example.emergencynow.ui.feature.profile.PersonalInformationScreen
import com.example.emergencynow.ui.feature.contacts.EmergencyContactsScreen

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String = Routes.WELCOME) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onRegisterEgn = { navController.navigate(Routes.ENTER_EGN) },
                onLogin = { navController.navigate(Routes.ENTER_EGN) }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onMakeEmergencyCall = { navController.navigate(Routes.EMERGENCY_CALL) },
                onOpenProfile = { navController.navigate(Routes.PROFILE_HOME) },
                onSelectAmbulance = { navController.navigate(Routes.AMBULANCE_SELECTION) }
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
                onVerified = { isReturningUser ->
                    if (isReturningUser) {
                        navController.navigate(Routes.HOME)
                    } else {
                        navController.navigate(Routes.PERSONAL_INFO)
                    }
                }
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
                onFinish = { navController.navigate(Routes.HOME) }
            )
        }
        composable(Routes.AMBULANCE_SELECTION) {
            AmbulanceSelectionScreen(
                onBack = { navController.popBackStack() },
                onAmbulanceSelected = {
                    // Just pop back to existing HomeScreen - don't create a new one
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.EMERGENCY_CALL) {
            EmergencyCallScreen(
                onBack = { navController.popBackStack() },
                onCallCreated = { callId ->
                    navController.popBackStack()
                    navController.navigate("${Routes.CALL_TRACKING}/$callId")
                }
            )
        }
        composable(
            route = "${Routes.CALL_TRACKING}/{callId}",
            arguments = listOf(
                navArgument("callId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getString("callId") ?: return@composable
            CallTrackingScreen(
                callId = callId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PROFILE_HOME) {
            ProfileHomeScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Routes.PERSONAL_INFO) },
                onEditContacts = { navController.navigate(Routes.EMERGENCY_CONTACTS) }
            )
        }
    }
}
