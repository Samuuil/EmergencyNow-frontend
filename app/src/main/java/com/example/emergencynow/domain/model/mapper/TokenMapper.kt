package com.example.emergencynow.domain.model.mapper

import com.example.emergencynow.data.model.response.TokenResponse
import com.example.emergencynow.domain.model.entity.Token

fun TokenResponse.toToken(): Token {
    return Token(
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}
