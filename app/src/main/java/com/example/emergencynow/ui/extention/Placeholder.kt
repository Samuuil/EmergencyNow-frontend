package com.example.emergencynow.ui.extention

// Placeholder to keep the package as requested. Add extension functions here later.

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName
import android.content.Context

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
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String?
)

data class CreateCallRequest(
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val patientEgn: String?
)

data class CallResponse(
    val id: String?,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?
)

interface BackendApi {
    @POST("auth/initiate-login")
    suspend fun initiateLogin(@Body body: InitiateLoginRequest): InitiateLoginResponse

    @POST("auth/verify-code")
    suspend fun verifyCode(@Body body: VerifyCodeRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): TokenResponse

    @POST("profiles/me")
    suspend fun createProfile(
        @Header("Authorization") bearer: String,
        @Body body: CreateProfileRequest
    ): ProfileResponse

    @GET("contacts/me")
    suspend fun getMyContacts(
        @Header("Authorization") bearer: String,
    ): List<ContactResponse>

    @POST("contacts/me")
    suspend fun createMyContact(
        @Header("Authorization") bearer: String,
        @Body body: CreateContactRequest
    ): ContactResponse

    @DELETE("contacts/me/{id}")
    suspend fun deleteMyContact(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    )

    @POST("calls")
    suspend fun createCall(
        @Header("Authorization") bearer: String,
        @Body body: CreateCallRequest
    ): CallResponse
}

object BackendClient {
    // private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val BASE_URL = "http://localhost:3000/"

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

object AuthStorage {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    data class Tokens(val accessToken: String?, val refreshToken: String?)

    fun loadTokens(context: Context): Tokens {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Tokens(
            accessToken = prefs.getString(KEY_ACCESS, null),
            refreshToken = prefs.getString(KEY_REFRESH, null)
        )
    }

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .apply()
    }

    fun clearTokens(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
