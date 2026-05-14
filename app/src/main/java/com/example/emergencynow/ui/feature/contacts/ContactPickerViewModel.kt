package com.example.emergencynow.ui.feature.contacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.ui.util.DeviceContact
import com.example.emergencynow.ui.util.DeviceContactsLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactPickerUiState(
    val isLoading: Boolean = false,
    val hasPermission: Boolean = false,
    val contacts: List<DeviceContact> = emptyList(),
    val query: String = "",
    val error: String? = null,
)

class ContactPickerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ContactPickerUiState())
    val uiState: StateFlow<ContactPickerUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean, context: Context) {
        _uiState.value = _uiState.value.copy(hasPermission = granted)
        if (granted) loadContacts(context)
    }

    fun loadContacts(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val contacts = DeviceContactsLoader.load(context)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contacts = contacts,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load contacts",
                )
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun filteredContacts(): List<DeviceContact> {
        val q = _uiState.value.query.trim()
        if (q.isEmpty()) return _uiState.value.contacts
        val needle = q.lowercase()
        return _uiState.value.contacts.filter {
            it.displayName.lowercase().contains(needle) ||
                it.phoneNumber.filter { ch -> ch.isDigit() }.contains(q.filter { ch -> ch.isDigit() })
        }
    }
}
