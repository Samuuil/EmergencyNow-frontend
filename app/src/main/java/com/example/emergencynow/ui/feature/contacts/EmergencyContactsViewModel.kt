package com.example.emergencynow.ui.feature.contacts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.entity.Contact
import com.example.emergencynow.domain.usecase.contact.CreateContactUseCase
import com.example.emergencynow.domain.usecase.contact.DeleteContactUseCase
import com.example.emergencynow.domain.usecase.contact.GetContactsUseCase
import com.example.emergencynow.domain.usecase.contact.UpdateContactUseCase
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val emptyContact = Contact(id = "", name = "", phoneNumber = "", email = null)

data class EmergencyContactsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val contacts: List<Contact> = listOf(emptyContact)
)

class EmergencyContactsViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val createContactUseCase: CreateContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyContactsUiState())
    val uiState: StateFlow<EmergencyContactsUiState> = _uiState.asStateFlow()

    private var originalContacts: List<Contact> = emptyList()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                if (AuthSession.userId.isNullOrEmpty()) {
                    notificationManager.showError("Your session has expired. Please log in again.")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                val remoteContacts = getContactsUseCase().getOrDefault(emptyList())
                originalContacts = remoteContacts
                val contacts = if (remoteContacts.isEmpty()) listOf(emptyContact) else remoteContacts

                _uiState.value = _uiState.value.copy(contacts = contacts, isLoading = false)
            } catch (e: Exception) {
                Log.e("EmergencyContactsViewModel", "Failed to load contacts", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
                notificationManager.showError(
                    e.message ?: "Failed to load contacts. Please try again."
                )
            }
        }
    }

    fun updateContact(index: Int, contact: Contact) {
        val updated = _uiState.value.contacts.toMutableList()
        updated[index] = contact
        _uiState.value = _uiState.value.copy(contacts = updated)
    }

    fun addContact() {
        val current = _uiState.value.contacts
        if (current.size < 5) {
            _uiState.value = _uiState.value.copy(contacts = current + emptyContact)
        }
    }

    fun removeContact(index: Int) {
        viewModelScope.launch {
            try {
                val toRemove = _uiState.value.contacts[index]

                if (toRemove.id.isNotEmpty() && !AuthSession.userId.isNullOrEmpty()) {
                    deleteContactUseCase(toRemove.id).getOrThrow()
                    originalContacts = originalContacts.filter { it.id != toRemove.id }
                }

                val updated = _uiState.value.contacts.toMutableList()
                updated.removeAt(index)
                _uiState.value = _uiState.value.copy(contacts = updated)
            } catch (e: Exception) {
                Log.e("EmergencyContactsViewModel", "Failed to remove contact", e)
                notificationManager.showError(
                    e.message ?: "Failed to remove contact. Please try again."
                )
            }
        }
    }

    fun saveContacts(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                if (AuthSession.userId.isNullOrEmpty()) {
                    notificationManager.showError("Your session has expired. Please log in again.")
                    return@launch
                }

                val allContacts = _uiState.value.contacts
                val partialContacts = allContacts.filter { contact ->
                    val hasSomething = contact.name.isNotBlank() || contact.phoneNumber.isNotBlank() || !contact.email.isNullOrBlank()
                    val isValid = contact.name.isNotBlank() && contact.phoneNumber.isNotBlank()
                    hasSomething && !isValid
                }

                if (partialContacts.isNotEmpty()) {
                    notificationManager.showError(
                        "Some contacts are incomplete. Each contact requires both a name and phone number."
                    )
                    return@launch
                }

                val validContacts = allContacts.filter {
                    it.name.isNotBlank() && it.phoneNumber.isNotBlank()
                }

                if (validContacts.isEmpty()) {
                    notificationManager.showError(
                        "Please fill in at least one contact's name and phone number."
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isSaving = true)

                validContacts.filter { it.id.isEmpty() }.forEach { contact ->
                    createContactUseCase(
                        name = contact.name,
                        phoneNumber = contact.phoneNumber,
                        email = contact.email?.ifBlank { null }
                    ).getOrThrow()
                }

                validContacts.filter { contact ->
                    contact.id.isNotEmpty() && originalContacts.none { it == contact }
                }.forEach { contact ->
                    updateContactUseCase(
                        id = contact.id,
                        name = contact.name,
                        phoneNumber = contact.phoneNumber,
                        email = contact.email?.ifBlank { null }
                    ).getOrThrow()
                }

                _uiState.value = _uiState.value.copy(isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                Log.e("EmergencyContactsViewModel", "Failed to save contacts", e)
                _uiState.value = _uiState.value.copy(isSaving = false)
                notificationManager.showError(
                    e.message ?: "Failed to save contacts. Please try again."
                )
            }
        }
    }
}
