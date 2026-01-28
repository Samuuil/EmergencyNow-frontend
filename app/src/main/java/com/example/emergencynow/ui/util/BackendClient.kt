package com.example.emergencynow.ui.util

import com.example.emergencynow.domain.model.request.*
import com.example.emergencynow.domain.model.response.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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

    private val logging: HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(
        HttpLoggingInterceptor.Level.BODY
    )

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(FallbackHostInterceptor())
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(NetworkConfig.retrofitBaseUrl())
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: BackendApi = retrofit.create(BackendApi::class.java)
}
