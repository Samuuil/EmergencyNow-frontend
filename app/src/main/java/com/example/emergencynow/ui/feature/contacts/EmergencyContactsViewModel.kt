package com.example.emergencynow.ui.feature.contacts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.usecase.contact.CreateContactUseCase
import com.example.emergencynow.domain.usecase.contact.DeleteContactUseCase
import com.example.emergencynow.domain.usecase.contact.GetContactsUseCase
import com.example.emergencynow.ui.util.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmergencyContactsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val contacts: List<Contact> = listOf(Contact("", ""))
)

class EmergencyContactsViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val createContactUseCase: CreateContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyContactsUiState())
    val uiState: StateFlow<EmergencyContactsUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val accessToken = AuthSession.accessToken
                if (accessToken.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Missing session. Log in again."
                    )
                    return@launch
                }

                val remoteContacts = getContactsUseCase().getOrDefault(emptyList())
                val contacts = if (remoteContacts.isEmpty()) {
                    listOf(Contact("", "", ""))
                } else {
                    remoteContacts.map { Contact(it.name, it.phoneNumber, it.email ?: "", it.id) }
                }
                
                _uiState.value = _uiState.value.copy(
                    contacts = contacts,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("EmergencyContactsViewModel", "Failed to load contacts", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load contacts: ${e.localizedMessage ?: e::class.simpleName}"
                )
            }
        }
    }

    fun updateContact(index: Int, contact: Contact) {
        val updatedContacts = _uiState.value.contacts.toMutableList()
        updatedContacts[index] = contact
        _uiState.value = _uiState.value.copy(contacts = updatedContacts)
    }

    fun addContact() {
        val currentContacts = _uiState.value.contacts
        if (currentContacts.size < 5) {
            _uiState.value = _uiState.value.copy(
                contacts = currentContacts + Contact("", "", "")
            )
        }
    }

    fun removeContact(index: Int) {
        viewModelScope.launch {
            try {
                val accessToken = AuthSession.accessToken
                val toRemove = _uiState.value.contacts[index]
                
                if (!toRemove.id.isNullOrEmpty() && !accessToken.isNullOrEmpty()) {
                    deleteContactUseCase(toRemove.id!!).getOrThrow()
                }
                
                val updatedContacts = _uiState.value.contacts.toMutableList()
                updatedContacts.removeAt(index)
                _uiState.value = _uiState.value.copy(contacts = updatedContacts)
            } catch (e: Exception) {
                Log.e("EmergencyContactsViewModel", "Failed to remove contact", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to remove contact."
                )
            }
        }
    }

    fun saveContacts(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val accessToken = AuthSession.accessToken
                if (accessToken.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "Missing session. Log in again."
                    )
                    return@launch
                }

                val validContacts = _uiState.value.contacts.filter { 
                    it.name.isNotBlank() && it.phone.isNotBlank() 
                }
                
                if (validContacts.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "Add at least one contact."
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                val newContacts = validContacts.filter { it.id == null }
                newContacts.forEach { contact ->
                    createContactUseCase(
                        name = contact.name,
                        phoneNumber = contact.phone,
                        email = contact.email.ifBlank { null }
                    ).getOrThrow()
                }
                
                _uiState.value = _uiState.value.copy(isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                Log.e("EmergencyContactsViewModel", "Failed to save contacts", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save contacts."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}


