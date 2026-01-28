package com.example.emergencynow.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            onValueChange = { newValue ->
                val digitsOnly = newValue.filter { it.isDigit() }
                val formatted = when {
                    digitsOnly.length <= 4 -> digitsOnly
                    digitsOnly.length <= 6 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4)}"
                    else -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 6)}-${digitsOnly.substring(6, minOf(8, digitsOnly.length))}"
                }
                if (formatted.length <= 10) {
                    onValueChange(formatted)
                }
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = BrandBlueDark
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(BrandBlueDark),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            "YYYY-MM-DD",
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


