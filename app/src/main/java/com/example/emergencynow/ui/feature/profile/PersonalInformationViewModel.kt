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
                            height = profile.height.toString(),
                            weight = profile.weight.toString(),
                            gender = profile.gender.name.lowercase(),
                            allergies = profile.allergies?.joinToString(", ") ?: "",
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
        if (value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(height = value)
        }
    }

    fun updateWeight(value: String) {
        if (value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(weight = value)
        }
    }

    fun updateGender(value: String) {
        _uiState.value = _uiState.value.copy(gender = value)
    }

    fun updateAllergies(value: String) {
        _uiState.value = _uiState.value.copy(allergies = value)
    }

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val heightValue = state.height.toIntOrNull()
            val weightValue = state.weight.toIntOrNull()

            if (heightValue == null || weightValue == null) {
                _uiState.value = state.copy(error = "Height and weight must be numbers")
                return@launch
            }

            val allergiesList = state.allergies.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val allergies = if (allergiesList.isEmpty()) null else allergiesList

            _uiState.value = state.copy(isSaving = true, error = null)

            try {
                val result = if (state.isEditMode) {
                    updateProfileUseCase(heightValue, weightValue, state.gender, allergies)
                } else {
                    createProfileUseCase(heightValue, weightValue, state.gender, allergies)
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
