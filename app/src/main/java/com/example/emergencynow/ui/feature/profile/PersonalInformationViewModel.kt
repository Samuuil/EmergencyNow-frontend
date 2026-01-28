package com.example.emergencynow.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.usecase.profile.CreateProfileUseCase
import com.example.emergencynow.domain.usecase.profile.GetProfileUseCase
import com.example.emergencynow.domain.usecase.profile.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PersonalInfoUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val height: String = "",
    val weight: String = "",
    val gender: String = "male",
    val allergies: String = "",
    val bloodType: String = "",
    val illnesses: String = "",
    val medicines: String = "",
    val dateOfBirth: String = "",
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false
)

class PersonalInformationViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalInfoUiState())
    val uiState: StateFlow<PersonalInfoUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = getProfileUseCase()
                result.fold(
                    onSuccess = { profile ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            height = if (profile.height > 0) profile.height.toString() else "",
                            weight = if (profile.weight > 0) profile.weight.toString() else "",
                            gender = profile.gender.name.lowercase(),
                            allergies = profile.allergies?.joinToString(", ") ?: "",
                            bloodType = profile.bloodType ?: "",
                            illnesses = profile.illnesses?.joinToString(", ") ?: "",
                            medicines = profile.medicines?.joinToString(", ") ?: "",
                            dateOfBirth = profile.dateOfBirth ?: "",
                            isEditMode = true
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isEditMode = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}",
                    isEditMode = false
                )
            }
        }
    }

    fun updateHeight(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(height = value)
        }
    }

    fun updateWeight(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(weight = value)
        }
    }

    fun updateGender(value: String) {
        _uiState.value = _uiState.value.copy(gender = value)
    }

    fun updateAllergies(value: String) {
        _uiState.value = _uiState.value.copy(allergies = value)
    }

    fun updateBloodType(value: String) {
        _uiState.value = _uiState.value.copy(bloodType = value)
    }

    fun updateIllnesses(value: String) {
        _uiState.value = _uiState.value.copy(illnesses = value)
    }

    fun updateMedicines(value: String) {
        _uiState.value = _uiState.value.copy(medicines = value)
    }

    fun updateDateOfBirth(value: String) {
        _uiState.value = _uiState.value.copy(dateOfBirth = value)
    }

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val heightValue = state.height.toIntOrNull()
            val weightValue = state.weight.toIntOrNull()

            if (heightValue == null || weightValue == null || heightValue <= 0 || weightValue <= 0) {
                _uiState.value = state.copy(error = "Please enter valid height and weight")
                return@launch
            }

            val allergiesList = state.allergies.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val allergies = if (allergiesList.isEmpty()) null else allergiesList

            val illnessesList = state.illnesses.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val illnesses = if (illnessesList.isEmpty()) null else illnessesList

            val medicinesList = state.medicines.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val medicines = if (medicinesList.isEmpty()) null else medicinesList

            val bloodType = if (state.bloodType.isBlank()) null else state.bloodType
            val dateOfBirth = if (state.dateOfBirth.isBlank()) null else state.dateOfBirth

            _uiState.value = state.copy(isSaving = true, error = null)

            try {
                val result = if (state.isEditMode) {
                    updateProfileUseCase(heightValue, weightValue, state.gender, allergies, bloodType, illnesses, medicines, dateOfBirth)
                } else {
                    createProfileUseCase(heightValue, weightValue, state.gender, allergies, bloodType, illnesses, medicines, dateOfBirth)
                }

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isEditMode = true
                        )
                        onSuccess()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = "Failed to save profile: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save profile"
                )
            }
        }
    }
}
