package com.example.emergencynow.ui.navigation

import com.example.emergencynow.ui.feature.ambulance.AmbulanceSelectionUiState
import com.example.emergencynow.ui.feature.ambulance.AmbulanceSelectionViewModel
import com.example.emergencynow.ui.feature.auth.ChooseVerificationMethodUiState
import com.example.emergencynow.ui.feature.auth.ChooseVerificationMethodViewModel
import com.example.emergencynow.ui.feature.auth.EnterEgnViewModel
import com.example.emergencynow.ui.feature.auth.VerifyCodeUIState
import com.example.emergencynow.ui.feature.auth.VerifyCodeViewModel
import com.example.emergencynow.ui.feature.call.EmergencyCallUiState
import com.example.emergencynow.ui.feature.call.EmergencyCallViewModel
import com.example.emergencynow.ui.feature.contacts.ContactPickerUiState
import com.example.emergencynow.ui.feature.contacts.ContactPickerViewModel
import com.example.emergencynow.ui.feature.contacts.EmergencyContactsUiState
import com.example.emergencynow.ui.feature.contacts.EmergencyContactsViewModel
import com.example.emergencynow.ui.feature.dispatcher.DispatcherUiState
import com.example.emergencynow.ui.feature.dispatcher.DispatcherViewModel
import com.example.emergencynow.ui.feature.doctor.PatientProfileUiState
import com.example.emergencynow.ui.feature.doctor.PatientProfileViewModel
import com.example.emergencynow.ui.feature.history.HistoryUiState
import com.example.emergencynow.ui.feature.history.HistoryViewModel
import com.example.emergencynow.ui.feature.home.CallTrackingUiState
import com.example.emergencynow.ui.feature.home.CallTrackingViewModel
import com.example.emergencynow.ui.feature.home.DriverUiState
import com.example.emergencynow.ui.feature.home.DriverViewModel
import com.example.emergencynow.ui.feature.home.HomeUiState
import com.example.emergencynow.ui.feature.home.HomeViewModel
import com.example.emergencynow.ui.feature.profile.PersonalInfoUiState
import com.example.emergencynow.ui.feature.profile.PersonalInformationViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class NavTestEnvironment {

    val homeState = MutableStateFlow(HomeUiState(isLoading = false))
    val driverState = MutableStateFlow(DriverUiState())
    val callTrackingState = MutableStateFlow(CallTrackingUiState())
    val dispatcherState = MutableStateFlow(DispatcherUiState())
    val emergencyCallState = MutableStateFlow(EmergencyCallUiState())
    val contactPickerState = MutableStateFlow(ContactPickerUiState())
    val chooseVerificationState = MutableStateFlow(ChooseVerificationMethodUiState())
    val verifyCodeState = MutableStateFlow(VerifyCodeUIState())
    val personalInfoState = MutableStateFlow(PersonalInfoUiState(isLoading = false))
    val emergencyContactsState = MutableStateFlow(EmergencyContactsUiState(isLoading = false))
    val ambulanceState = MutableStateFlow(AmbulanceSelectionUiState(isLoading = false))
    val historyState = MutableStateFlow(HistoryUiState(isLoading = false))
    val patientProfileState = MutableStateFlow(PatientProfileUiState())

    val module: Module = module {
        viewModel<HomeViewModel> {
            mockk<HomeViewModel>(relaxed = true).also { every { it.uiState } returns homeState }
        }
        viewModel<DriverViewModel> {
            mockk<DriverViewModel>(relaxed = true).also { every { it.uiState } returns driverState }
        }
        viewModel<CallTrackingViewModel> {
            mockk<CallTrackingViewModel>(relaxed = true).also { every { it.uiState } returns callTrackingState }
        }
        viewModel<DispatcherViewModel> {
            mockk<DispatcherViewModel>(relaxed = true).also { every { it.uiState } returns dispatcherState }
        }
        viewModel<EmergencyCallViewModel> {
            mockk<EmergencyCallViewModel>(relaxed = true).also { every { it.uiState } returns emergencyCallState }
        }
        viewModel<ContactPickerViewModel> {
            mockk<ContactPickerViewModel>(relaxed = true).also { every { it.uiState } returns contactPickerState }
        }
        viewModel<ChooseVerificationMethodViewModel> {
            mockk<ChooseVerificationMethodViewModel>(relaxed = true).also { vm ->
                every { vm.uiState } returns chooseVerificationState
                every { vm.requestVerificationCode(any(), any()) } answers {
                    secondArg<() -> Unit>().invoke()
                }
            }
        }
        viewModel<VerifyCodeViewModel> {
            mockk<VerifyCodeViewModel>(relaxed = true).also { every { it.state } returns verifyCodeState }
        }
        viewModel<PersonalInformationViewModel> {
            mockk<PersonalInformationViewModel>(relaxed = true).also { every { it.uiState } returns personalInfoState }
        }
        viewModel<EmergencyContactsViewModel> {
            mockk<EmergencyContactsViewModel>(relaxed = true).also { every { it.uiState } returns emergencyContactsState }
        }
        viewModel<AmbulanceSelectionViewModel> {
            mockk<AmbulanceSelectionViewModel>(relaxed = true).also { every { it.uiState } returns ambulanceState }
        }
        viewModel<HistoryViewModel> {
            mockk<HistoryViewModel>(relaxed = true).also { every { it.uiState } returns historyState }
        }
        viewModel<PatientProfileViewModel> {
            mockk<PatientProfileViewModel>(relaxed = true).also { every { it.uiState } returns patientProfileState }
        }
        viewModel<EnterEgnViewModel> { EnterEgnViewModel() }
    }
}
