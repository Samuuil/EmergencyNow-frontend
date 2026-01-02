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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.components.inputs.PrimaryTextField
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue

data class Contact(var name: String, var phone: String, var email: String = "", var id: String? = null)

@Composable
fun ContactCard(
    index: Int,
    contact: Contact,
    onChange: (Contact) -> Unit,
    onRemove: () -> Unit
) {
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
                textColor = BrandBlueDark
            )
            
            Spacer(Modifier.height(16.dp))
            
            PrimaryTextField(
                value = contact.phone,
                onValueChange = { onChange(contact.copy(phone = it)) },
                label = "Phone Number",
                placeholder = "Phone Number",
                keyboardType = KeyboardType.Phone,
                textColor = BrandBlueDark
            )
            
            Spacer(Modifier.height(16.dp))
            
            PrimaryTextField(
                value = contact.email,
                onValueChange = { onChange(contact.copy(email = it)) },
                label = "Email (Optional)",
                placeholder = "Email",
                keyboardType = KeyboardType.Email,
                textColor = BrandBlueDark
            )
        }
    }
}


