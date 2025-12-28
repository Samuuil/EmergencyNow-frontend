package com.example.emergencynow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.emergencynow.ui.feature.home.HomeScreen
import com.example.emergencynow.ui.feature.home.CallTrackingScreen
import com.example.emergencynow.ui.feature.ambulance.AmbulanceSelectionScreen
import com.example.emergencynow.ui.feature.call.EmergencyCallScreen
import com.example.emergencynow.ui.feature.profile.PersonalInformationScreen
import com.example.emergencynow.ui.feature.profile.ProfileHomeScreen
import com.example.emergencynow.ui.feature.contacts.EmergencyContactsScreen
import com.example.emergencynow.ui.feature.history.HistoryScreen
import com.example.emergencynow.ui.feature.doctor.PatientLookupScreen
import com.example.emergencynow.ui.feature.doctor.PatientProfileScreen
import org.koin.androidx.compose.koinViewModel

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
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }
            val viewModel: com.example.emergencynow.ui.feature.home.HomeViewModel = 
                org.koin.androidx.compose.koinViewModel(viewModelStoreOwner = parentEntry)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            androidx.compose.runtime.LaunchedEffect(uiState.activeCallId, uiState.isDriver, uiState.isLoading) {
                android.util.Log.d("AppNavGraph", "HOME LaunchedEffect - isDriver: ${uiState.isDriver}, activeCallId: ${uiState.activeCallId}, isLoading: ${uiState.isLoading}")
                if (!uiState.isLoading && !uiState.isDriver && uiState.activeCallId != null) {
                    android.util.Log.d("AppNavGraph", "User has active call - navigating to CALL_TRACKING")
                    navController.navigate(Routes.CALL_TRACKING) {
                        launchSingleTop = true
                    }
                }
            }
            
            HomeScreen(
                onMakeEmergencyCall = { navController.navigate(Routes.EMERGENCY_CALL) },
                onOpenProfile = { navController.navigate(Routes.PROFILE_HOME) },
                onSelectAmbulance = { navController.navigate(Routes.AMBULANCE_SELECTION) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToContacts = { navController.navigate(Routes.EMERGENCY_CONTACTS) },
                onPatientLookup = { navController.navigate(Routes.PATIENT_LOOKUP) },
                viewModel = viewModel
            )
        }
        composable(Routes.ENTER_EGN) {
            EnterEgnScreen(
                onBack = { navController.popBackStack() },
                onContinue = { egn ->
                    navController.navigate("${Routes.CHOOSE_VERIFICATION}/$egn")
                }
            )
        }
        composable(
            route = "${Routes.CHOOSE_VERIFICATION}/{egn}",
            arguments = listOf(
                navArgument("egn") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val egn = backStackEntry.arguments?.getString("egn") ?: ""
            ChooseVerificationMethodScreen(
                onBack = { navController.popBackStack() },
                onPhone = { navController.navigate("${Routes.ENTER_VERIFICATION_CODE}/$egn") },
                onEmail = { navController.navigate("${Routes.ENTER_VERIFICATION_CODE}/$egn") }
            )
        }
        composable(
            route = "${Routes.ENTER_VERIFICATION_CODE}/{egn}",
            arguments = listOf(
                navArgument("egn") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val egn = backStackEntry.arguments?.getString("egn") ?: ""
            EnterVerificationCodeScreen(
                egn = egn,
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
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.EMERGENCY_CALL) {
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }
            val homeViewModel: com.example.emergencynow.ui.feature.home.HomeViewModel = 
                org.koin.androidx.compose.koinViewModel(viewModelStoreOwner = parentEntry)
            
            EmergencyCallScreen(
                onBack = { navController.popBackStack() },
                onCallCreated = { callId ->
                    homeViewModel.setActiveCallId(callId)
                    navController.navigate(Routes.CALL_TRACKING) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }
        composable(Routes.CALL_TRACKING) {
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }
            val homeViewModel: com.example.emergencynow.ui.feature.home.HomeViewModel = 
                org.koin.androidx.compose.koinViewModel(viewModelStoreOwner = parentEntry)
            
            CallTrackingScreen(
                onBackToHome = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                viewModel = homeViewModel
            )
        }
        composable(Routes.PROFILE_HOME) {
            ProfileHomeScreen(
                onBack = { navController.popBackStack() },
                onPersonalInfo = { navController.navigate(Routes.PERSONAL_INFO) },
                onEmergencyContacts = { navController.navigate(Routes.EMERGENCY_CONTACTS) }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PATIENT_LOOKUP) {
            PatientLookupScreen(
                onBack = { navController.popBackStack() },
                onLookup = { egn ->
                    navController.navigate("${Routes.PATIENT_PROFILE}/$egn")
                }
            )
        }
        composable(
            route = "${Routes.PATIENT_PROFILE}/{egn}",
            arguments = listOf(
                navArgument("egn") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val egn = backStackEntry.arguments?.getString("egn") ?: ""
            PatientProfileScreen(
                egn = egn,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
