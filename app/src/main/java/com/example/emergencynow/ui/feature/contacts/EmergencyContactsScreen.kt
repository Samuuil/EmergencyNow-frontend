@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import com.example.emergencynow.ui.extention.AuthSession
import com.example.emergencynow.ui.extention.BackendClient
import com.example.emergencynow.ui.extention.CreateContactRequest
import kotlinx.coroutines.launch

@Composable
fun EmergencyContactsScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    var contacts by remember { mutableStateOf(listOf(Contact("", ""))) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Emergency Contacts") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = {
                        if (isLoading) {
                            return@Button
                        }
                        val accessToken = AuthSession.accessToken
                        if (accessToken.isNullOrEmpty()) {
                            error = "Missing session. Log in again."
                            return@Button
                        }
                        val validContacts = contacts.filter { it.name.isNotBlank() && it.phone.isNotBlank() }
                        if (validContacts.isEmpty()) {
                            error = "Add at least one contact."
                            return@Button
                        }
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                validContacts.forEach { contact ->
                                    BackendClient.api.createContact(
                                        bearer = "Bearer $accessToken",
                                        body = CreateContactRequest(
                                            name = contact.name,
                                            phoneNumber = contact.phone,
                                            email = null
                                        )
                                    )
                                }
                                onFinish()
                            } catch (e: Exception) {
                                error = "Failed to save contacts."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = contacts.any { it.name.isNotBlank() && it.phone.isNotBlank() } && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Finish Setup") }
            }
        }
    ) { inner ->
        Column(modifier = Modifier.padding(inner).padding(16.dp).fillMaxSize()) {
            Text("Add up to 5 people we can notify in an emergency.", style = MaterialTheme.typography.bodyMedium)
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(contacts) { index, contact ->
                    ContactCard(
                        index = index,
                        contact = contact,
                        onChange = { updated ->
                            contacts = contacts.toMutableList().also { it[index] = updated }
                        },
                        onRemove = {
                            contacts = contacts.toMutableList().also { it.removeAt(index) }
                        }
                    )
                }
            }
            val canAddMore = contacts.size < 5
            OutlinedButton(onClick = { if (canAddMore) contacts = contacts + Contact("", "") }, enabled = canAddMore, modifier = Modifier.fillMaxWidth()) {
                Text("Add Another Contact")
            }
        }
    }
}

data class Contact(var name: String, var phone: String)

@Composable
private fun ContactCard(index: Int, contact: Contact, onChange: (Contact) -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Contact ${index + 1}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onRemove) { Icon(Icons.Filled.Delete, contentDescription = "Remove") }
            }
            OutlinedTextField(
                value = contact.name,
                onValueChange = { onChange(contact.copy(name = it)) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = contact.phone,
                onValueChange = { onChange(contact.copy(phone = it)) },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
