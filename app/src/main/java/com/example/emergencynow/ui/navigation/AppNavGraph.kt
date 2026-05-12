package com.example.emergencynow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.emergencynow.ui.constants.AmbulanceSelectionRoute
import com.example.emergencynow.ui.constants.CallTrackingRoute
import com.example.emergencynow.ui.constants.ChooseVerificationRoute
import com.example.emergencynow.ui.constants.EmergencyCallRoute
import com.example.emergencynow.ui.constants.EmergencyContactsRoute
import com.example.emergencynow.ui.constants.EnterEgnRoute
import com.example.emergencynow.ui.constants.EnterVerificationCodeRoute
import com.example.emergencynow.ui.constants.HistoryRoute
import com.example.emergencynow.ui.constants.HomeRoute
import com.example.emergencynow.ui.constants.PatientLookupRoute
import com.example.emergencynow.ui.constants.PatientProfileRoute
import com.example.emergencynow.ui.constants.PersonalInfoRoute
import com.example.emergencynow.ui.constants.ProfileHomeRoute
import com.example.emergencynow.ui.constants.WelcomeRoute
import com.example.emergencynow.ui.feature.ambulance.AmbulanceSelectionScreen
import com.example.emergencynow.ui.feature.auth.ChooseVerificationMethodScreen
import com.example.emergencynow.ui.feature.auth.EnterEgnScreen
import com.example.emergencynow.ui.feature.auth.EnterVerificationCodeScreen
import com.example.emergencynow.ui.feature.auth.WelcomeScreen
import com.example.emergencynow.ui.feature.call.EmergencyCallScreen
import com.example.emergencynow.ui.feature.contacts.EmergencyContactsScreen
import com.example.emergencynow.ui.feature.doctor.PatientLookupScreen
import com.example.emergencynow.ui.feature.doctor.PatientProfileScreen
import com.example.emergencynow.ui.feature.history.HistoryScreen
import com.example.emergencynow.ui.feature.home.CallTrackingScreen
import com.example.emergencynow.ui.feature.home.CallTrackingViewModel
import com.example.emergencynow.ui.feature.home.HomeScreen
import com.example.emergencynow.ui.feature.home.HomeViewModel
import com.example.emergencynow.ui.feature.profile.PersonalInformationScreen
import com.example.emergencynow.ui.feature.profile.ProfileHomeScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: Any = WelcomeRoute) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable<WelcomeRoute> {
            WelcomeScreen(
                onRegisterEgn = { navController.navigate(EnterEgnRoute) },
                onLogin = { navController.navigate(EnterEgnRoute) }
            )
        }
        composable<HomeRoute> {
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry<HomeRoute>()
            }
            val homeViewModel: HomeViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            val callTrackingViewModel: CallTrackingViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
            val trackingState by callTrackingViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(trackingState.activeCallId, homeState.isDriver, homeState.isLoading) {
                if (!homeState.isLoading && !homeState.isDriver && trackingState.activeCallId != null) {
                    navController.navigate(CallTrackingRoute) { launchSingleTop = true }
                }
            }

            HomeScreen(
                onMakeEmergencyCall = { navController.navigate(EmergencyCallRoute) },
                onOpenProfile = { navController.navigate(ProfileHomeRoute) },
                onSelectAmbulance = { navController.navigate(AmbulanceSelectionRoute) },
                onNavigateToHistory = { navController.navigate(HistoryRoute) },
                onNavigateToContacts = { navController.navigate(EmergencyContactsRoute) },
                onPatientLookup = { navController.navigate(PatientLookupRoute) },
                viewModel = homeViewModel,
                callTrackingViewModel = callTrackingViewModel,
            )
        }
        composable<EnterEgnRoute> {
            EnterEgnScreen(
                onBack = { navController.popBackStack() },
                onContinue = { egn -> navController.navigate(ChooseVerificationRoute(egn)) }
            )
        }
        composable<ChooseVerificationRoute> { backStack ->
            val args = backStack.toRoute<ChooseVerificationRoute>()
            ChooseVerificationMethodScreen(
                onBack = { navController.popBackStack() },
                onPhone = { navController.navigate(EnterVerificationCodeRoute(args.egn)) },
                onEmail = { navController.navigate(EnterVerificationCodeRoute(args.egn)) }
            )
        }
        composable<EnterVerificationCodeRoute> { backStack ->
            val args = backStack.toRoute<EnterVerificationCodeRoute>()
            EnterVerificationCodeScreen(
                egn = args.egn,
                onBack = { navController.popBackStack() },
                onVerified = { isReturningUser ->
                    if (isReturningUser) navController.navigate(HomeRoute)
                    else navController.navigate(PersonalInfoRoute(isOnboarding = true))
                }
            )
        }
        composable<PersonalInfoRoute> { backStack ->
            val args = backStack.toRoute<PersonalInfoRoute>()
            PersonalInformationScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    if (args.isOnboarding) navController.navigate(EmergencyContactsRoute)
                    else navController.popBackStack()
                }
            )
        }
        composable<EmergencyContactsRoute> {
            EmergencyContactsScreen(
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate(HomeRoute) }
            )
        }
        composable<AmbulanceSelectionRoute> {
            AmbulanceSelectionScreen(
                onBack = { navController.popBackStack() },
                onAmbulanceSelected = { navController.popBackStack() }
            )
        }
        composable<EmergencyCallRoute> {
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry<HomeRoute>()
            }
            val callTrackingViewModel: CallTrackingViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            EmergencyCallScreen(
                onBack = { navController.popBackStack() },
                onCallCreated = { callId ->
                    callTrackingViewModel.setActiveCallId(callId)
                    navController.navigate(CallTrackingRoute) {
                        popUpTo<HomeRoute> { inclusive = false }
                    }
                }
            )
        }
        composable<CallTrackingRoute> {
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry<HomeRoute>()
            }
            val homeViewModel: HomeViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            val callTrackingViewModel: CallTrackingViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            CallTrackingScreen(
                onBackToHome = { navController.popBackStack<HomeRoute>(inclusive = false) },
                homeViewModel = homeViewModel,
                callTrackingViewModel = callTrackingViewModel,
            )
        }
        composable<ProfileHomeRoute> {
            ProfileHomeScreen(
                onBack = { navController.popBackStack() },
                onPersonalInfo = { navController.navigate(PersonalInfoRoute()) },
                onEmergencyContacts = { navController.navigate(EmergencyContactsRoute) }
            )
        }
        composable<HistoryRoute> {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
        composable<PatientLookupRoute> {
            PatientLookupScreen(
                onBack = { navController.popBackStack() },
                onLookup = { egn -> navController.navigate(PatientProfileRoute(egn)) }
            )
        }
        composable<PatientProfileRoute> { backStack ->
            val args = backStack.toRoute<PatientProfileRoute>()
            PatientProfileScreen(
                egn = args.egn,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
