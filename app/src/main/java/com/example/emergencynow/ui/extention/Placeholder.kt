package com.example.emergencynow.ui.extention

// Placeholder to keep the package as requested. Add extension functions here later.

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

enum class LoginMethod {
    @SerializedName("email")
    EMAIL,

    @SerializedName("sms")
    SMS
}

data class InitiateLoginRequest(
    val egn: String,
    val method: LoginMethod
)

data class InitiateLoginResponse(
    val message: String
)

data class VerifyCodeRequest(
    val egn: String,
    val code: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

enum class GenderDto {
    MALE,
    FEMALE,
    OTHER
}

data class CreateProfileRequest(
    val height: Int,
    val weight: Int,
    val gender: GenderDto,
    val allergies: List<String>?
)

data class CreateContactRequest(
    val name: String,
    val phoneNumber: String,
    val email: String?
)

data class ProfileResponse(
    val id: String?
)

data class ContactResponse(
    val id: String?
)

interface BackendApi {
    @POST("auth/initiate-login")
    suspend fun initiateLogin(@Body body: InitiateLoginRequest): InitiateLoginResponse

    @POST("auth/verify-code")
    suspend fun verifyCode(@Body body: VerifyCodeRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): TokenResponse

    @POST("profiles")
    suspend fun createProfile(
        @Header("Authorization") bearer: String,
        @Body body: CreateProfileRequest
    ): ProfileResponse

    @POST("contacts")
    suspend fun createContact(
        @Header("Authorization") bearer: String,
        @Body body: CreateContactRequest
    ): ContactResponse
}

object BackendClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val logging: HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(
        HttpLoggingInterceptor.Level.BODY
    )

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: BackendApi = retrofit.create(BackendApi::class.java)
}

object AuthSession {
    var egn: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null
    var lastMethod: LoginMethod? = null
}
