package com.example.emergencynow.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue

@Composable
fun NumberInputWithUnit(
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(
                width = 1.dp,
                color = BrandBlueDark.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(CurvePaleBlue, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = BrandBlueDark
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(BrandBlueDark),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                "0",
                                fontSize = 16.sp,
                                color = BrandBlueDark.copy(alpha = 0.4f)
                            )
                        }
                        innerTextField()
                    }
                    Text(
                        text = unit,
                        fontSize = 14.sp,
                        color = BrandBlueDark.copy(alpha = 0.5f)
                    )
                }
            }
        )
    }
}

@Composable
fun BloodTypeSelector(
    selectedBloodType: String,
    onBloodTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        bloodTypes.chunked(4).forEach { rowTypes ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTypes.forEach { type ->
                    val isSelected = selectedBloodType == type
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) BrandBlueDark else BrandBlueDark.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isSelected) BrandBlueDark else CurvePaleBlue,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onBloodTypeSelected(if (isSelected) "" else type)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else BrandBlueDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var day by remember { mutableStateOf(if (value.length == 10) value.substring(8, 10) else "") }
    var month by remember { mutableStateOf(if (value.length == 10) value.substring(5, 7) else "") }
    var year by remember { mutableStateOf(if (value.length == 10) value.substring(0, 4) else "") }

    // Sync if the ViewModel loads an existing value after initial composition
    LaunchedEffect(value) {
        if (value.length == 10 && value[4] == '-' && value[7] == '-') {
            day = value.substring(8, 10)
            month = value.substring(5, 7)
            year = value.substring(0, 4)
        }
    }

    val monthFocus = remember { FocusRequester() }
    val yearFocus = remember { FocusRequester() }

    fun emit(d: String, m: String, y: String) {
        if (d.length == 2 && m.length == 2 && y.length == 4) {
            onValueChange("$y-$m-$d")
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = "Day",
                fontSize = 12.sp,
                color = BrandBlueDark.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))
            DatePartBox(
                value = day,
                onValueChange = { v ->
                    day = v
                    emit(v, month, year)
                    if (v.length == 2) try { monthFocus.requestFocus() } catch (_: Exception) {}
                },
                placeholder = "DD",
                maxLength = 2
            )
        }

        Text(
            text = "/",
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            color = BrandBlueDark.copy(alpha = 0.3f),
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Column(modifier = Modifier.weight(1.4f)) {
            Text(
                text = "Month",
                fontSize = 12.sp,
                color = BrandBlueDark.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))
            DatePartBox(
                value = month,
                onValueChange = { v ->
                    month = v
                    emit(day, v, year)
                    if (v.length == 2) try { yearFocus.requestFocus() } catch (_: Exception) {}
                },
                placeholder = "MM",
                maxLength = 2,
                focusRequester = monthFocus
            )
        }

        Text(
            text = "/",
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            color = BrandBlueDark.copy(alpha = 0.3f),
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = "Year",
                fontSize = 12.sp,
                color = BrandBlueDark.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))
            DatePartBox(
                value = year,
                onValueChange = { v ->
                    year = v
                    emit(day, month, v)
                },
                placeholder = "YYYY",
                maxLength = 4,
                focusRequester = yearFocus
            )
        }
    }
}

@Composable
private fun DatePartBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    focusRequester: FocusRequester? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) BrandBlueDark else BrandBlueDark.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(CurvePaleBlue, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { new ->
                val digits = new.filter { it.isDigit() }
                if (digits.length <= maxLength) onValueChange(digits)
            },
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandBlueDark,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(BrandBlueDark),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = if (maxLength == 4) 14.sp else 16.sp,
                            color = BrandBlueDark.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun MultilineTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(
                width = 1.dp,
                color = BrandBlueDark.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(CurvePaleBlue, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = BrandBlueDark,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(BrandBlueDark),
            modifier = Modifier.fillMaxSize(),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            fontSize = 16.sp,
                            color = BrandBlueDark.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
