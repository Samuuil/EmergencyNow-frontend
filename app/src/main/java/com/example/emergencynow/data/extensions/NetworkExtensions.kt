package com.example.emergencynow.data.extensions

import com.example.emergencynow.data.error.EmergencyError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

private val errorJson = Json { ignoreUnknownKeys = true }

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: HttpException) {
        val (code, message) = parseHttpError(e)
        Result.failure(
            EmergencyError.Generic(
                code = code,
                messageString = message,
                httpStatusCode = e.code()
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun parseHttpError(e: HttpException): Pair<String?, String?> {
    return try {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val obj = errorJson.parseToJsonElement(body).jsonObject
            val code = obj["code"]?.jsonPrimitive?.contentOrNull

            // NestJS validation errors send message as an array; other errors send it as a string
            val messageEl = obj["message"]
            val message = when {
                messageEl is kotlinx.serialization.json.JsonArray ->
                    messageEl.mapNotNull { it.jsonPrimitive.contentOrNull }.joinToString("\n")
                messageEl != null ->
                    messageEl.jsonPrimitive.contentOrNull
                else -> null
            }

            // Fall back to the "error" field (e.g. "Bad Request") if message is blank
            val fallback = obj["error"]?.jsonPrimitive?.contentOrNull
            Pair(code, message?.ifBlank { fallback } ?: fallback)
        } else {
            Pair(null, e.message())
        }
    } catch (_: Exception) {
        Pair(null, e.message())
    }
}
