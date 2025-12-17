package com.example.emergencynow.ui.extention

// Placeholder to keep the package as requested. Add extension functions here later.

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.PATCH
import com.google.gson.annotations.SerializedName
import android.content.Context
import android.util.Base64
import com.google.gson.Gson

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
    val id: String?,
    val height: Int?,
    val weight: Int?,
    val gender: String?,
    val allergies: List<String>?
)

data class ContactResponse(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String?
)

// Pagination metadata from nestjs-paginate
data class PaginationMeta(
    val itemsPerPage: Int?,
    val totalItems: Int?,
    val currentPage: Int?,
    val totalPages: Int?,
    val sortBy: List<List<String>>?
)

data class PaginationLinks(
    val current: String?,
    val next: String?,
    val previous: String?,
    val first: String?,
    val last: String?
)

// Generic paginated response wrapper matching nestjs-paginate output
data class PaginatedResponse<T>(
    val data: List<T>,
    val meta: PaginationMeta?,
    val links: PaginationLinks?
)

data class CreateCallRequest(
    val description: String,
    val latitude: Double,
    val longitude: Double
)

data class CallResponse(
    val id: String?,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val status: String?,
    val routePolyline: String?,
    val estimatedDistance: Int?,
    val estimatedDuration: Int?,
    val routeSteps: List<RouteStepDto>?,
    val ambulanceCurrentLatitude: Double?,
    val ambulanceCurrentLongitude: Double?,
    val dispatchedAt: String?,
    val arrivedAt: String?,
    val completedAt: String?,
    val selectedHospitalId: String?,
    val selectedHospitalName: String?,
    val hospitalRoutePolyline: String?,
    val hospitalRouteDistance: Int?,
    val hospitalRouteDuration: Int?,
    val hospitalRouteSteps: List<RouteStepDto>?
)

data class CallTrackingLocation(
    val latitude: Double,
    val longitude: Double
)

data class RoutePoint(
    val lat: Double,
    val lng: Double
)

data class RouteStepDto(
    val distance: Int,
    val duration: Int,
    val instruction: String,
    val startLocation: RoutePoint,
    val endLocation: RoutePoint
)

data class RouteDto(
    val polyline: String?,
    val distance: Int?,
    val duration: Int?,
    val steps: List<RouteStepDto>?
)

data class CallTrackingCall(
    val id: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val status: String?,
    val ambulanceCurrentLatitude: Double?,
    val ambulanceCurrentLongitude: Double?
)

data class CallTrackingResponse(
    val call: CallTrackingCall,
    val currentLocation: CallTrackingLocation?,
    val route: RouteDto?
)

data class AmbulanceDto(
    val id: String,
    val licensePlate: String,
    val vehicleModel: String?,
    val latitude: Double?,
    val longitude: Double?,
    val available: Boolean,
    val driverId: String?,
)

data class AssignDriverRequest(
    val driverId: String?
)

// Hospital DTOs for hospital selection flow
data class HospitalDto(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val distance: Int?, // meters from driver location
    val duration: Int?  // seconds to reach
)

data class GetHospitalsRequest(
    val latitude: Double,
    val longitude: Double
)

data class SelectHospitalRequest(
    val hospitalId: String,
    val latitude: Double,
    val longitude: Double
)

// Hospital info wrapper for hospital-route response
data class HospitalInfo(
    val id: String?,
    val name: String?
)

// Updated to match backend getHospitalRouteData response structure
data class HospitalRouteResponse(
    val hospital: HospitalInfo?,
    val route: RouteDto?
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
    ): PaginatedResponse<ContactResponse>

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

    @GET("calls/{id}/tracking")
    suspend fun getCallTracking(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): CallTrackingResponse

    @PATCH("calls/{id}/status")
    suspend fun updateCallStatus(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): CallResponse

    @GET("ambulances/available")
    suspend fun getAvailableAmbulances(
        @Header("Authorization") bearer: String,
    ): PaginatedResponse<AmbulanceDto>

    @GET("ambulances/driver/{driverId}")
    suspend fun getAmbulanceByDriver(
        @Path("driverId") driverId: String,
    ): AmbulanceDto?

    @GET("users/user-role/{id}")
    suspend fun getUserRole(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): ResponseBody

    @PATCH("ambulances/{id}/driver")
    suspend fun assignAmbulanceDriver(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: AssignDriverRequest
    ): AmbulanceDto

    // Hospital selection endpoints
    @POST("calls/{id}/hospitals")
    suspend fun getHospitalsForCall(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: GetHospitalsRequest
    ): List<HospitalDto>

    @POST("calls/{id}/select-hospital")
    suspend fun selectHospitalForCall(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: SelectHospitalRequest
    ): HospitalRouteResponse

    @GET("calls/{id}/hospital-route")
    suspend fun getHospitalRoute(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): HospitalRouteResponse
}

object BackendClient {
    // private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val BASE_URL = "https://emergencynow.samuil.me/"
    //private const val BASE_URL = "http://192.168.5.32:3000/"

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

data class JwtPayload(
    val sub: String?,
    val role: String?,
    val egn: String?,
)

fun parseJwt(token: String): JwtPayload? {
    return try {
        val parts = token.split(".")
        if (parts.size < 2) return null
        val payloadPart = parts[1]
        val decodedBytes = Base64.decode(payloadPart, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val json = String(decodedBytes, Charsets.UTF_8)
        Gson().fromJson(json, JwtPayload::class.java)
    } catch (e: Exception) {
        null
    }
}

object AuthSession {
    var egn: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null
    var lastMethod: LoginMethod? = null
    var userId: String? = null
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
