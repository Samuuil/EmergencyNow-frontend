package com.example.emergencynow.data.extensions

import com.example.emergencynow.data.error.EmergencyError
import kotlinx.serialization.json.Json
import retrofit2.Response

val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

fun <T> requestBody(request: Response<T>): Result<T> {
    return try {
        if (request.isSuccessful) {
            request.body()?.let {
                Result.success(it)
            } ?: Result.failure(Throwable("Empty response body"))
        } else {
            val errorBody = request.errorBody()?.string()
            val error = try {
                json.decodeFromString<EmergencyError.Generic>(errorBody ?: "")
            } catch (e: Exception) {
                // Fallback for unparseable errors
                EmergencyError.Generic(
                    code = null,
                    messageString = errorBody ?: "Unknown error",
                    statusCode = request.code()
                )
            }
            Result.failure(error)
        }
    } catch (ex: Exception) {
        Result.failure(Throwable(ex.message ?: "Unknown error"))
    }
}

fun <T> resultBody(result: Result<T>): Result<T> {
    return result.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it) }
    )
}
