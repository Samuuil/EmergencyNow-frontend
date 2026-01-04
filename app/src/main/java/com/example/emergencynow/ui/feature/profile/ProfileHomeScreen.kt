package com.example.emergencynow.ui.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emergencynow.ui.feature.profile.ProfileMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHomeScreen(
    onBack: () -> Unit,
    onPersonalInfo: () -> Unit,
    onEmergencyContacts: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ProfileMenuItem(
                icon = Icons.Filled.Person,
                title = "Personal Information",
                description = "View and edit your personal details",
                onClick = onPersonalInfo
            )

            ProfileMenuItem(
                icon = Icons.Filled.Contacts,
                title = "Emergency Contacts",
                description = "Manage your emergency contacts",
                onClick = onEmergencyContacts
            )
        }
    }
}

