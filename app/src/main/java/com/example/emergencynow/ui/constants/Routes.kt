package com.example.emergencynow.ui.constants

import kotlinx.serialization.Serializable

@Serializable object WelcomeRoute
@Serializable object HomeRoute
@Serializable object EnterEgnRoute
@Serializable data class ChooseVerificationRoute(val egn: String)
@Serializable data class EnterVerificationCodeRoute(val egn: String)
@Serializable data class PersonalInfoRoute(val isOnboarding: Boolean = false)
@Serializable object EmergencyContactsRoute
@Serializable object EmergencyCallRoute
@Serializable object ProfileHomeRoute
@Serializable object CallTrackingRoute
@Serializable object AmbulanceSelectionRoute
@Serializable object HistoryRoute
@Serializable object PatientLookupRoute
@Serializable data class PatientProfileRoute(val egn: String)
@Serializable data class DispatcherAssignRoute(val callId: String)
@Serializable object ContactPickerRoute
