@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.domain.usecase.contact.CreateContactUseCase
import com.example.emergencynow.domain.usecase.contact.DeleteContactUseCase
import com.example.emergencynow.domain.usecase.contact.GetContactsUseCase
import com.example.emergencynow.ui.components.buttons.PrimaryButton
import com.example.emergencynow.ui.components.decorations.ProfileGeometricBackground
import com.example.emergencynow.ui.components.inputs.PrimaryTextField
import com.example.emergencynow.ui.util.AuthSession
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun EmergencyContactsScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    var contacts by remember { mutableStateOf(listOf(Contact("", ""))) }
    val scope = rememberCoroutineScope()
    val getContactsUseCase: GetContactsUseCase = koinInject()
    val createContactUseCase: CreateContactUseCase = koinInject()
    val deleteContactUseCase: DeleteContactUseCase = koinInject()
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        val accessToken = AuthSession.accessToken
        if (accessToken.isNullOrEmpty()) {
            error = "Missing session. Log in again."
            isLoading = false
        } else {
            try {
                val remoteContacts = getContactsUseCase().getOrDefault(emptyList())
                contacts = if (remoteContacts.isEmpty()) {
                    listOf(Contact("", "", ""))
                } else {
                    remoteContacts.map { Contact(it.name, it.phoneNumber, it.email ?: "", it.id) }
                }
            } catch (e: Exception) {
                error = "Failed to load contacts: ${e.localizedMessage ?: e::class.simpleName}"
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ProfileGeometricBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Spacer(Modifier.height(48.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Emergency Contacts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 48.dp)
                )
                Spacer(Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Add up to 5 people we can notify in an emergency.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Contact list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(contacts) { index, contact ->
                        ContactCard(
                            index = index,
                            contact = contact,
                            onChange = { updated ->
                                contacts = contacts.toMutableList().also { it[index] = updated }
                            },
                            onRemove = {
                                val accessToken = AuthSession.accessToken
                                val toRemove = contacts[index]
                                if (!toRemove.id.isNullOrEmpty() && !accessToken.isNullOrEmpty()) {
                                    scope.launch {
                                        try {
                                            deleteContactUseCase(toRemove.id!!).getOrThrow()
                                            contacts = contacts.toMutableList().also { it.removeAt(index) }
                                        } catch (e: Exception) {
                                            error = "Failed to remove contact."
                                        }
                                    }
                                } else {
                                    contacts = contacts.toMutableList().also { it.removeAt(index) }
                                }
                            }
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Add button
                val canAddMore = contacts.size < 5
                OutlinedButton(
                    onClick = { if (canAddMore) contacts = contacts + Contact("", "", "") },
                    enabled = canAddMore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Add Another Contact", fontWeight = FontWeight.Medium)
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Finish button
                PrimaryButton(
                    text = if (isSaving) "" else "Finish Setup",
                    onClick = {
                        if (isSaving) return@PrimaryButton
                        val accessToken = AuthSession.accessToken
                        if (accessToken.isNullOrEmpty()) {
                            error = "Missing session. Log in again."
                            return@PrimaryButton
                        }
                        val validContacts = contacts.filter { it.name.isNotBlank() && it.phone.isNotBlank() }
                        if (validContacts.isEmpty()) {
                            error = "Add at least one contact."
                            return@PrimaryButton
                        }
                        val newContacts = validContacts.filter { it.id == null }
                        scope.launch {
                            isSaving = true
                            error = null
                            try {
                                newContacts.forEach { contact ->
                                    createContactUseCase(
                                        name = contact.name,
                                        phoneNumber = contact.phone,
                                        email = contact.email.ifBlank { null }
                                    ).getOrThrow()
                                }
                                onFinish()
                            } catch (e: Exception) {
                                error = "Failed to save contacts."
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = contacts.any { it.name.isNotBlank() && it.phone.isNotBlank() } && !isSaving,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                
                if (isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
        
        // Full-screen loading overlay when saving
        if (isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

data class Contact(var name: String, var phone: String, var email: String = "", var id: String? = null)

@Composable
private fun ContactCard(
    index: Int,
    contact: Contact,
    onChange: (Contact) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Contact ${index + 1}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            PrimaryTextField(
                value = contact.name,
                onValueChange = { onChange(contact.copy(name = it)) },
                label = "Full Name",
                placeholder = "Full Name",
                keyboardType = KeyboardType.Text
            )
            
            Spacer(Modifier.height(16.dp))
            
            PrimaryTextField(
                value = contact.phone,
                onValueChange = { onChange(contact.copy(phone = it)) },
                label = "Phone Number",
                placeholder = "Phone Number",
                keyboardType = KeyboardType.Phone
            )
            
            Spacer(Modifier.height(16.dp))
            
            PrimaryTextField(
                value = contact.email,
                onValueChange = { onChange(contact.copy(email = it)) },
                label = "Email (Optional)",
                placeholder = "Email",
                keyboardType = KeyboardType.Email
            )
        }
    }
}
