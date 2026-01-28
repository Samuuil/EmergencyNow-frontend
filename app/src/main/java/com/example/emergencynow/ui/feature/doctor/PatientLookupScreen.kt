@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.components.decorations.EnterEgnBackground
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.BrandBlueMid

@Composable
fun PatientLookupScreen(
    onBack: () -> Unit,
    onLookup: (String) -> Unit
) {
    var egn by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        EnterEgnBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(start = 0.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    text = "Patient Lookup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark.copy(alpha = 0.8f),
                    modifier = Modifier.padding(end = 40.dp)
                )
                
                Spacer(Modifier.width(40.dp))
            }
            
            Spacer(Modifier.height(32.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter\nPatient EGN",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandBlueDark,
                    lineHeight = 52.sp,
                    letterSpacing = (-1).sp
                )
                
                Spacer(Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrandBlueMid)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "Enter the patient's EGN to view their medical profile.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    lineHeight = 28.sp
                )
            }
            
            Spacer(Modifier.height(48.dp))
            
            BasicTextField(
                value = egn,
                onValueChange = { 
                    if (it.all { ch -> ch.isDigit() } && it.length <= 10) {
                        egn = it
                        error = null
                    }
                },
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark,
                    textAlign = TextAlign.Center,
                    letterSpacing = 6.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 2.dp,
                                color = BrandBlueMid,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 20.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        innerTextField()
                    }
                }
            )
            
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error ?: "",
                    color = Color(0xFFEF4444),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (egn.length == 10) {
                        onLookup(egn)
                    } else {
                        error = "Please enter a valid 10-digit EGN"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = BrandBlueDark.copy(alpha = 0.2f)
                    ),
                enabled = egn.length == 10,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (egn.length == 10) BrandBlueDark else Color(0xFFE5E7EB),
                    contentColor = if (egn.length == 10) Color.White else Color(0xFF6B7280),
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF6B7280)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Lookup Patient",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
