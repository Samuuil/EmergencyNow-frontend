package com.example.emergencynow.di

import com.example.emergencynow.BuildConfig
import com.example.emergencynow.data.service.AmbulanceService
import com.example.emergencynow.data.service.AuthService
import com.example.emergencynow.data.service.CallService
import com.example.emergencynow.data.service.ContactService
import com.example.emergencynow.data.service.HospitalService
import com.example.emergencynow.data.service.ProfileService
import com.example.emergencynow.data.service.UserService
import com.example.emergencynow.data.session.SessionManager
import com.example.emergencynow.data.session.TokenAuthenticator
import com.example.emergencynow.data.session.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    single { SessionManager(androidContext()) }

    single<OkHttpClient> {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(TokenInterceptor(get()))
            .authenticator(TokenAuthenticator(get(), get()))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<AuthService> { get<Retrofit>().create(AuthService::class.java) }
    single<ProfileService> { get<Retrofit>().create(ProfileService::class.java) }
    single<ContactService> { get<Retrofit>().create(ContactService::class.java) }
    single<CallService> { get<Retrofit>().create(CallService::class.java) }
    single<AmbulanceService> { get<Retrofit>().create(AmbulanceService::class.java) }
    single<HospitalService> { get<Retrofit>().create(HospitalService::class.java) }
    single<UserService> { get<Retrofit>().create(UserService::class.java) }
}
