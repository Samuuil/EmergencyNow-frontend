package com.example.emergencynow.data.extensions

import com.example.emergencynow.data.error.EmergencyError
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import retrofit2.Response

@OptIn(ExperimentalSerializationApi::class)
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
            val httpStatusCode = request.code()
            val error = try {
                val parsed = json.decodeFromString<EmergencyError.Generic>(errorBody ?: "{}")
                parsed.copy(httpStatusCode = httpStatusCode)
            } catch (e: Exception) {
                EmergencyError.Generic(
                    code = null,
                    messageString = errorBody ?: "Unknown error",
                    httpStatusCode = httpStatusCode
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

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
