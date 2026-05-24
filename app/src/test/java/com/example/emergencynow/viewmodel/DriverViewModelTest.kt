package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.model.entity.CallDetail
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.domain.model.entity.NearbyHospital
import com.example.emergencynow.domain.model.response.AmbulanceDto
import com.example.emergencynow.domain.usecase.ambulance.GetAmbulanceByDriverUseCase
import com.example.emergencynow.domain.usecase.ambulance.UnassignAmbulanceDriverUseCase
import com.example.emergencynow.domain.usecase.call.GetCallByIdUseCase
import com.example.emergencynow.domain.usecase.call.UpdateCallStatusUseCase
import com.example.emergencynow.domain.usecase.hospital.GetHospitalRouteUseCase
import com.example.emergencynow.domain.usecase.hospital.GetHospitalsForCallUseCase
import com.example.emergencynow.domain.usecase.hospital.SelectHospitalUseCase
import com.example.emergencynow.ui.feature.home.DriverViewModel
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.CallOffer
import com.example.emergencynow.ui.util.CallRoute
import com.example.emergencynow.ui.util.DriverNotificationHelper
import com.example.emergencynow.ui.util.IDriverSocketManager
import com.example.emergencynow.ui.util.PendingCallOfferStorage
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeDriverSocketManager : IDriverSocketManager {
    override var onCallOffer: ((CallOffer) -> Unit)? = null
    override var onCallRoute: ((CallRoute) -> Unit)? = null
    override var onConnectionChange: ((Boolean) -> Unit)? = null
    override var onLocationRequest: ((requestId: Int) -> Unit)? = null

    val acceptedCalls = mutableListOf<String>()
    val declinedCalls = mutableListOf<String>()
    val locationUpdates = mutableListOf<Triple<String, Double, Double>>()
    var connectCalled = false
    var disconnectCalled = false

    override fun connect(accessToken: String) { connectCalled = true }
    override fun disconnect() { disconnectCalled = true }
    override fun acceptCall(callId: String) { acceptedCalls.add(callId) }
    override fun declineCall(callId: String) { declinedCalls.add(callId) }
    override fun sendLocationUpdate(callId: String, latitude: Double, longitude: Double) {
        locationUpdates.add(Triple(callId, latitude, longitude))
    }
    override fun isConnected(): Boolean = connectCalled && !disconnectCalled

    fun fireConnectionChange(connected: Boolean) = onConnectionChange?.invoke(connected)
    fun fireCallOffer(offer: CallOffer) = onCallOffer?.invoke(offer)
    fun fireCallRoute(route: CallRoute) = onCallRoute?.invoke(route)
}

@OptIn(ExperimentalCoroutinesApi::class)
class DriverViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSocket: FakeDriverSocketManager
    private lateinit var getAmbulanceByDriverUseCase: GetAmbulanceByDriverUseCase
    private lateinit var unassignAmbulanceDriverUseCase: UnassignAmbulanceDriverUseCase
    private lateinit var updateCallStatusUseCase: UpdateCallStatusUseCase
    private lateinit var getHospitalsForCallUseCase: GetHospitalsForCallUseCase
    private lateinit var selectHospitalUseCase: SelectHospitalUseCase
    private lateinit var getHospitalRouteUseCase: GetHospitalRouteUseCase
    private lateinit var getCallByIdUseCase: GetCallByIdUseCase
    private lateinit var driverNotificationHelper: DriverNotificationHelper
    private lateinit var authStorage: AuthStorage
    private lateinit var pendingCallOfferStorage: PendingCallOfferStorage

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSocket = FakeDriverSocketManager()
        getAmbulanceByDriverUseCase = mockk()
        unassignAmbulanceDriverUseCase = mockk()
        updateCallStatusUseCase = mockk()
        getHospitalsForCallUseCase = mockk()
        selectHospitalUseCase = mockk()
        getHospitalRouteUseCase = mockk()
        getCallByIdUseCase = mockk()
        driverNotificationHelper = mockk(relaxed = true)
        authStorage = mockk(relaxed = true)
        pendingCallOfferStorage = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm() = DriverViewModel(
        getAmbulanceByDriverUseCase = getAmbulanceByDriverUseCase,
        unassignAmbulanceDriverUseCase = unassignAmbulanceDriverUseCase,
        updateCallStatusUseCase = updateCallStatusUseCase,
        getHospitalsForCallUseCase = getHospitalsForCallUseCase,
        selectHospitalUseCase = selectHospitalUseCase,
        getHospitalRouteUseCase = getHospitalRouteUseCase,
        getCallByIdUseCase = getCallByIdUseCase,
        driverNotificationHelper = driverNotificationHelper,
        authStorage = authStorage,
        pendingCallOfferStorage = pendingCallOfferStorage,
        driverSocket = fakeSocket,
    )

    @Test
    fun `loadData sets assignedAmbulanceId and plate from use case result`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        assertEquals("amb-1", vm.uiState.value.assignedAmbulanceId)
        assertEquals("CA1234AB", vm.uiState.value.assignedAmbulancePlate)
    }

    @Test
    fun `loadData with no ambulance keeps null ambulance state`() = runTest {
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(null)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        assertNull(vm.uiState.value.assignedAmbulanceId)
        assertNull(vm.uiState.value.assignedAmbulancePlate)
    }

    @Test
    fun `loadData with ambulance triggers socket connect`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)

        buildVm().loadData("user-1")
        advanceUntilIdle()

        assertTrue(fakeSocket.connectCalled)
    }

    @Test
    fun `socket onConnectionChange true updates isSocketConnected`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        fakeSocket.fireConnectionChange(true)

        assertTrue(vm.uiState.value.isSocketConnected)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `socket onConnectionChange false sets isSocketConnected false`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        fakeSocket.fireConnectionChange(false)

        assertFalse(vm.uiState.value.isSocketConnected)
    }

    @Test
    fun `socket onCallOffer sets incomingCallOffer`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        val offer = CallOffer(
            callId = "call-1", description = "Emergency", latitude = 42.0,
            longitude = 23.0, distance = 1000, duration = 120
        )
        fakeSocket.fireCallOffer(offer)

        assertEquals(offer, vm.uiState.value.incomingCallOffer)
    }

    @Test
    fun `socket onCallRoute sets activeCallId and route data`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)
        val fakeCallDetail = CallDetail(
            id = "call-1", status = CallStatus.EN_ROUTE,
            userEgn = "1234567890", patientEgn = null, patientPhoneNumber = null,
            selectedHospitalId = null
        )
        coEvery { getCallByIdUseCase(any()) } returns Result.success(fakeCallDetail)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        val route = CallRoute(
            callId = "call-1", polyline = "", distance = 2000,
            duration = 300, steps = listOf("Turn left", "Continue straight")
        )
        fakeSocket.fireCallRoute(route)
        advanceUntilIdle()

        assertEquals("call-1", vm.uiState.value.activeCallId)
        assertEquals(2000, vm.uiState.value.activeRouteDistance)
        assertEquals(300, vm.uiState.value.activeRouteDuration)
    }

    @Test
    fun `acceptCall clears incomingCallOffer and calls socket acceptCall`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)
        val fakeCallDetail = CallDetail(
            id = "call-1", status = CallStatus.EN_ROUTE,
            userEgn = "1234567890", patientEgn = null, patientPhoneNumber = null,
            selectedHospitalId = null
        )
        coEvery { getCallByIdUseCase(any()) } returns Result.success(fakeCallDetail)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        val offer = CallOffer(
            callId = "call-1", description = "Emergency", latitude = 42.0,
            longitude = 23.0, distance = 1000, duration = 120
        )
        fakeSocket.fireCallOffer(offer)
        vm.acceptCall()
        advanceUntilIdle()

        assertNull(vm.uiState.value.incomingCallOffer)
        assertEquals("call-1", fakeSocket.acceptedCalls.first())
        assertEquals("call-1", vm.uiState.value.activeCallId)
    }

    @Test
    fun `declineCall clears incomingCallOffer and calls socket declineCall`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        val offer = CallOffer(
            callId = "call-1", description = "Emergency", latitude = 42.0,
            longitude = 23.0, distance = 1000, duration = 120
        )
        fakeSocket.fireCallOffer(offer)
        vm.declineCall()

        assertNull(vm.uiState.value.incomingCallOffer)
        assertEquals("call-1", fakeSocket.declinedCalls.first())
    }

    @Test
    fun `updateCallStatus ARRIVED shows hospital selection`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)
        coEvery { updateCallStatusUseCase(any(), any()) } returns Result.success(Unit)
        val hospitals = listOf(
            NearbyHospital(id = "h-1", name = "City Hospital", latitude = 42.1, longitude = 23.1)
        )
        coEvery { getHospitalsForCallUseCase(any(), any(), any()) } returns Result.success(hospitals)
        val fakeCallDetail = CallDetail(
            id = "call-1", status = CallStatus.EN_ROUTE,
            userEgn = "1234567890", patientEgn = null, patientPhoneNumber = null,
            selectedHospitalId = null
        )
        coEvery { getCallByIdUseCase(any()) } returns Result.success(fakeCallDetail)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        val route = CallRoute(callId = "call-1", polyline = "", distance = 1000, duration = 200, steps = emptyList())
        fakeSocket.fireCallRoute(route)
        advanceUntilIdle()

        vm.updateDriverLocation(com.google.android.gms.maps.model.LatLng(42.0, 23.0))
        vm.updateCallStatus(CallStatus.ARRIVED)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.showHospitalSelection)
        assertEquals(CallStatus.ARRIVED, vm.uiState.value.callStatus)
        assertEquals(1, vm.uiState.value.availableHospitals.size)
    }

    @Test
    fun `unassignAmbulance disconnects socket and clears ambulance state`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)
        coEvery { unassignAmbulanceDriverUseCase(any()) } returns Result.success(Unit)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        vm.unassignAmbulance()
        advanceUntilIdle()

        assertNull(vm.uiState.value.assignedAmbulanceId)
        assertNull(vm.uiState.value.assignedAmbulancePlate)
        assertTrue(fakeSocket.disconnectCalled)
    }

    @Test
    fun `disconnectSocket updates isSocketConnected to false`() = runTest {
        val ambulance = AmbulanceDto(id = "amb-1", licensePlate = "CA1234AB")
        coEvery { getAmbulanceByDriverUseCase(any()) } returns Result.success(ambulance)

        val vm = buildVm()
        vm.loadData("user-1")
        advanceUntilIdle()

        fakeSocket.fireConnectionChange(true)
        vm.disconnectSocket()

        assertFalse(vm.uiState.value.isSocketConnected)
    }
}
