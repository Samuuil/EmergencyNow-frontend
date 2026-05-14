package com.example.emergencynow.ui.feature.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.domain.model.entity.Contact
import com.example.emergencynow.ui.components.inputs.PrimaryTextField
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue
import kotlinx.coroutines.delay

@Composable
fun ContactCard(
    index: Int,
    contact: Contact,
    autoFocus: Boolean = false,
    onChange: (Contact) -> Unit,
    onRemove: () -> Unit
) {
    val nameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            delay(300)
            try {
                nameFocusRequester.requestFocus()
            } catch (e: IllegalStateException) {
                // Field not yet attached to layout
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CurvePaleBlue
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
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
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
                keyboardType = KeyboardType.Text,
                textColor = BrandBlueDark,
                focusRequester = nameFocusRequester
            )

            Spacer(Modifier.height(16.dp))

            PrimaryTextField(
                value = contact.phoneNumber,
                onValueChange = { onChange(contact.copy(phoneNumber = it)) },
                label = "Phone Number",
                placeholder = "Phone Number",
                keyboardType = KeyboardType.Phone,
                textColor = BrandBlueDark
            )

            Spacer(Modifier.height(16.dp))

            PrimaryTextField(
                value = contact.email ?: "",
                onValueChange = { onChange(contact.copy(email = it.ifBlank { null })) },
                label = "Email (Optional)",
                placeholder = "Email",
                keyboardType = KeyboardType.Email,
                textColor = BrandBlueDark
            )
        }
    }
}
