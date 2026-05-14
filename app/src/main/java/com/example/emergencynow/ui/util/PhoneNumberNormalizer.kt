package com.example.emergencynow.ui.util

object PhoneNumberNormalizer {
    private const val DEFAULT_COUNTRY_CODE = "+359"

    fun toE164(raw: String): String? {
        if (raw.isBlank()) return null
        val digits = raw.filter { it.isDigit() || it == '+' }
        return when {
            digits.startsWith("+") -> digits
            digits.startsWith("00") -> "+" + digits.drop(2)
            digits.startsWith("0") -> DEFAULT_COUNTRY_CODE + digits.drop(1)
            digits.startsWith("359") -> "+$digits"
            digits.isEmpty() -> null
            else -> DEFAULT_COUNTRY_CODE + digits
        }
    }
}
