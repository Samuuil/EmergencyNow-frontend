@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.components.decorations.BackgroundVariant
import com.example.emergencynow.ui.components.decorations.DecorativeBackground
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun EmergencyContactsScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    viewModel: EmergencyContactsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var focusTargetIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(uiState.contacts.size) {
        if (focusTargetIndex >= 0 && focusTargetIndex < uiState.contacts.size) {
            listState.animateScrollToItem(focusTargetIndex)
            delay(600)
            focusTargetIndex = -1
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DecorativeBackground(BackgroundVariant.CHOOSE_VERIFICATION, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark,
                    modifier = Modifier.padding(end = 48.dp)
                )
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(40.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 196.dp)
                    ) {
                        itemsIndexed(uiState.contacts) { index, contact ->
                            ContactCard(
                                index = index,
                                contact = contact,
                                autoFocus = index == focusTargetIndex,
                                onChange = { updated -> viewModel.updateContact(index, updated) },
                                onRemove = { viewModel.removeContact(index) }
                            )
                        }
                    }

                    val bgColor = MaterialTheme.colorScheme.background
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.3f to bgColor.copy(alpha = 0.94f),
                                    1f to bgColor
                                )
                            )
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp)
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(Modifier.height(28.dp))

                        val lastIsEmpty = uiState.contacts.lastOrNull()?.let {
                            it.name.isBlank() && it.phoneNumber.isBlank()
                        } ?: false
                        val canAddMore = uiState.contacts.size < 5 && !lastIsEmpty
                        OutlinedButton(
                            onClick = {
                                focusTargetIndex = uiState.contacts.size
                                viewModel.addContact()
                            },
                            enabled = canAddMore,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CurvePaleBlue,
                                contentColor = BrandBlueDark,
                                disabledContainerColor = CurvePaleBlue.copy(alpha = 0.5f),
                                disabledContentColor = BrandBlueDark.copy(alpha = 0.4f)
                            ),
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

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.saveContacts(onFinish) },
                            enabled = uiState.contacts.any { it.name.isNotBlank() && it.phoneNumber.isNotBlank() } && !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = BrandBlueDark.copy(alpha = 0.2f)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlueDark,
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE5E7EB),
                                disabledContentColor = Color(0xFF6B7280)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Finish Setup",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isSaving) {
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
