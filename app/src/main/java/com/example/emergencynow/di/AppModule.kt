package com.example.emergencynow.di

import com.example.emergencynow.BuildConfig
import com.example.emergencynow.data.datasource.*
import com.example.emergencynow.data.datasource.impl.*
import com.example.emergencynow.data.repository.*
import com.example.emergencynow.data.service.*
import com.example.emergencynow.data.session.TokenAuthenticator
import com.example.emergencynow.data.session.TokenInterceptor
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.domain.repository.*
import com.example.emergencynow.domain.usecase.ambulance.*
import com.example.emergencynow.domain.usecase.ambulance.MarkAmbulanceAvailableUseCase
import com.example.emergencynow.domain.usecase.auth.*
import com.example.emergencynow.domain.usecase.auth.GetUserOnboardingStateUseCase
import com.example.emergencynow.domain.usecase.call.*
import com.example.emergencynow.domain.usecase.call.GetCallByIdUseCase
import com.example.emergencynow.domain.usecase.contact.*
import com.example.emergencynow.domain.usecase.hospital.*
import com.example.emergencynow.domain.usecase.profile.*
import com.example.emergencynow.domain.usecase.user.GetUserRoleUseCase
import com.example.emergencynow.ui.feature.auth.EnterEgnViewModel
import com.example.emergencynow.ui.feature.auth.VerifyCodeViewModel
import com.example.emergencynow.ui.feature.call.EmergencyCallViewModel
import com.example.emergencynow.ui.feature.profile.PersonalInformationViewModel
import com.example.emergencynow.ui.feature.home.HomeViewModel
import com.example.emergencynow.ui.feature.ambulance.AmbulanceSelectionViewModel
import com.example.emergencynow.ui.feature.history.HistoryViewModel
import com.example.emergencynow.ui.feature.doctor.PatientProfileViewModel
import com.example.emergencynow.ui.feature.contacts.EmergencyContactsViewModel
import com.example.emergencynow.ui.feature.auth.ChooseVerificationMethodViewModel
import com.example.emergencynow.ui.feature.home.CallTrackingViewModel
import com.example.emergencynow.ui.feature.home.DriverViewModel
import com.example.emergencynow.ui.util.DriverNotificationHelper
import com.example.emergencynow.ui.util.NotificationManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    single { AuthStorage(androidContext()) }
    single { com.example.emergencynow.data.repository.LocationRepository(androidContext()) }
    single { NotificationManager() }
    single { DriverNotificationHelper(androidContext()) }

    single {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    single<OkHttpClient>(qualifier = named("auth_refresh")) {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    single<Retrofit>(qualifier = named("auth_refresh")) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get<OkHttpClient>(qualifier = named("auth_refresh")))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<AuthService>(qualifier = named("auth_refresh")) {
        get<Retrofit>(qualifier = named("auth_refresh")).create(AuthService::class.java)
    }

    single { TokenInterceptor(get()) }
    single { TokenAuthenticator(get<AuthService>(qualifier = named("auth_refresh")), get()) }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(get<TokenInterceptor>())
            .authenticator(get<TokenAuthenticator>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
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

    single<AuthDataSource> { AuthDataSourceImpl(get()) }
    single<ProfileDataSource> { ProfileDataSourceImpl(get()) }
    single<ContactDataSource> { ContactDataSourceImpl(get()) }
    single<CallDataSource> { CallDataSourceImpl(get()) }
    single<AmbulanceDataSource> { AmbulanceDataSourceImpl(get()) }
    single<HospitalDataSource> { HospitalDataSourceImpl(get()) }
    single<UserDataSource> { UserDataSourceImpl(get()) }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<ContactRepository> { ContactRepositoryImpl(get()) }
    single<CallRepository> { CallRepositoryImpl(get()) }
    single<AmbulanceRepository> { AmbulanceRepositoryImpl(get()) }
    single<HospitalRepository> { HospitalRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }

    factory { RequestVerificationCodeUseCase(get()) }
    factory { VerifyCodeUseCase(get()) }
    factory { RefreshTokenUseCase(get()) }
    factory { GetUserOnboardingStateUseCase(get()) }

    factory { CreateProfileUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { GetProfileUseCase(get()) }
    factory { GetProfileByEgnUseCase(get()) }

    factory { GetContactsUseCase(get()) }
    factory { CreateContactUseCase(get()) }
    factory { DeleteContactUseCase(get()) }

    factory { CreateCallUseCase(get()) }
    factory { GetCallByIdUseCase(get()) }
    factory { GetCallTrackingUseCase(get()) }
    factory { UpdateCallStatusUseCase(get()) }
    factory { GetUserCallsUseCase(get()) }

    factory { GetAvailableAmbulancesUseCase(get()) }
    factory { GetAmbulanceByDriverUseCase(get()) }
    factory { AssignAmbulanceDriverUseCase(get()) }
    factory { UnassignAmbulanceDriverUseCase(get()) }
    factory { MarkAmbulanceAvailableUseCase(get()) }

    factory { GetHospitalsForCallUseCase(get()) }
    factory { SelectHospitalUseCase(get()) }
    factory { GetHospitalRouteUseCase(get()) }

    factory { GetUserRoleUseCase(get()) }

    viewModel { EnterEgnViewModel() }
    viewModel { VerifyCodeViewModel(get(), get(), get(), get(), get()) }
    viewModel { HomeViewModel(getUserRoleUseCase = get(), authStorage = get(), locationRepository = get()) }
    viewModel {
        DriverViewModel(
            getAmbulanceByDriverUseCase = get(),
            unassignAmbulanceDriverUseCase = get(),
            updateCallStatusUseCase = get(),
            getHospitalsForCallUseCase = get(),
            selectHospitalUseCase = get(),
            getHospitalRouteUseCase = get(),
            getCallByIdUseCase = get(),
            markAmbulanceAvailableUseCase = get(),
            driverNotificationHelper = get(),
            authStorage = get()
        )
    }
    viewModel { CallTrackingViewModel(authStorage = get()) }
    viewModel {
        AmbulanceSelectionViewModel(
            getAvailableAmbulancesUseCase = get(),
            assignAmbulanceDriverUseCase = get()
        )
    }
    viewModel { EmergencyCallViewModel(get()) }
    viewModel { PersonalInformationViewModel(get(), get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { PatientProfileViewModel(get()) }
    viewModel {
        EmergencyContactsViewModel(
            getContactsUseCase = get(),
            createContactUseCase = get(),
            deleteContactUseCase = get()
        )
    }
    viewModel {
        ChooseVerificationMethodViewModel(
            requestVerificationCodeUseCase = get(),
            notificationManager = get()
        )
    }
}
