package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.ui.feature.home.CallTrackingViewModel
import com.example.emergencynow.ui.util.AmbulanceLocationUpdate
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.CallAwaitingDispatcher
import com.example.emergencynow.ui.util.CallDispatched
import com.example.emergencynow.ui.util.CallStatusUpdate
import com.example.emergencynow.ui.util.CallWithDispatcher
import com.example.emergencynow.ui.util.IUserSocketManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeUserSocketManager : IUserSocketManager {
    override var onCallDispatched: ((CallDispatched) -> Unit)? = null
    override var onAmbulanceLocation: ((AmbulanceLocationUpdate) -> Unit)? = null
    override var onCallStatus: ((CallStatusUpdate) -> Unit)? = null
    override var onCallAwaitingDispatcher: ((CallAwaitingDispatcher) -> Unit)? = null
    override var onCallWithDispatcher: ((CallWithDispatcher) -> Unit)? = null
    override var onConnectionChange: ((Boolean) -> Unit)? = null

    var connectCount = 0
    var disconnectCalled = false

    override fun connect(accessToken: String) { connectCount++ }
    override fun disconnect() { disconnectCalled = true }
    override fun isConnected(): Boolean = connectCount > 0 && !disconnectCalled

    fun fireCallDispatched(event: CallDispatched) = onCallDispatched?.invoke(event)
    fun fireAmbulanceLocation(event: AmbulanceLocationUpdate) = onAmbulanceLocation?.invoke(event)
    fun fireCallStatus(event: CallStatusUpdate) = onCallStatus?.invoke(event)
    fun fireCallAwaitingDispatcher(event: CallAwaitingDispatcher) = onCallAwaitingDispatcher?.invoke(event)
    fun fireCallWithDispatcher(event: CallWithDispatcher) = onCallWithDispatcher?.invoke(event)
    fun fireConnectionChange(connected: Boolean) = onConnectionChange?.invoke(connected)
}

@OptIn(ExperimentalCoroutinesApi::class)
class CallTrackingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSocket: FakeUserSocketManager
    private lateinit var authStorage: AuthStorage

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSocket = FakeUserSocketManager()
        authStorage = mockk(relaxed = true)
        every { authStorage.accessToken } returns "fake-token"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm() = CallTrackingViewModel(authStorage = authStorage, userSocket = fakeSocket)

    @Test
    fun `setActiveCallId sets callId and PENDING status`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")

        assertEquals("call-1", vm.uiState.value.activeCallId)
        assertEquals(CallStatus.PENDING, vm.uiState.value.userCallStatus)
    }

    @Test
    fun `setActiveCallId triggers socket connect`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")

        assertEquals(1, fakeSocket.connectCount)
    }

    @Test
    fun `connectSocket does nothing when accessToken is null`() = runTest {
        every { authStorage.accessToken } returns null

        val vm = buildVm()
        vm.connectSocket()

        assertEquals(0, fakeSocket.connectCount)
    }

    @Test
    fun `connectSocket does not reconnect if already connected`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")         // connect #1
        fakeSocket.fireConnectionChange(true) // state → isSocketConnected = true

        vm.connectSocket()                    // should early-return, no second connect

        assertEquals(1, fakeSocket.connectCount)
    }

    @Test
    fun `onCallDispatched updates state to DISPATCHED with ambulance location`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")

        val event = CallDispatched(
            callId = "call-1", ambulanceId = "amb-1",
            ambulanceLatitude = 42.5, ambulanceLongitude = 23.5,
            polyline = "", distance = 1500, duration = 180
        )
        fakeSocket.fireCallDispatched(event)

        val state = vm.uiState.value
        assertEquals(CallStatus.DISPATCHED, state.userCallStatus)
        assertNotNull(state.ambulanceLocation)
        assertEquals(42.5, state.ambulanceLocation!!.latitude, 0.001)
        assertEquals(23.5, state.ambulanceLocation!!.longitude, 0.001)
        assertEquals(1500, state.activeRouteDistance)
        assertEquals(180, state.activeRouteDuration)
        assertFalse(state.isAwaitingDispatcher)
    }

    @Test
    fun `onCallAwaitingDispatcher sets waiting state with queue position`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")

        fakeSocket.fireCallAwaitingDispatcher(CallAwaitingDispatcher("call-1", position = 3, queueSize = 5))

        val state = vm.uiState.value
        assertTrue(state.isAwaitingDispatcher)
        assertEquals(3, state.queuePosition)
        assertFalse(state.isWithDispatcher)
    }

    @Test
    fun `onCallWithDispatcher sets isWithDispatcher and clears waiting state`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")
        fakeSocket.fireCallAwaitingDispatcher(CallAwaitingDispatcher("call-1", position = 1, queueSize = 1))

        fakeSocket.fireCallWithDispatcher(CallWithDispatcher("call-1"))

        val state = vm.uiState.value
        assertTrue(state.isWithDispatcher)
        assertFalse(state.isAwaitingDispatcher)
        assertNull(state.queuePosition)
    }

    @Test
    fun `onCallStatus COMPLETED resets all state`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")
        fakeSocket.fireCallDispatched(
            CallDispatched("call-1", "amb-1", 42.0, 23.0, "", 1000, 120)
        )

        fakeSocket.fireCallStatus(CallStatusUpdate("call-1", "completed"))

        val state = vm.uiState.value
        assertNull(state.activeCallId)
        assertNull(state.ambulanceLocation)
        assertNull(state.userCallStatus)
        assertFalse(state.isAwaitingDispatcher)
        assertFalse(state.isWithDispatcher)
        assertNull(state.queuePosition)
        assertTrue(state.activeRoutePolyline.isEmpty())
    }

    @Test
    fun `onCallStatus CANCELLED resets all state`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")

        fakeSocket.fireCallStatus(CallStatusUpdate("call-1", "cancelled"))

        assertNull(vm.uiState.value.activeCallId)
        assertNull(vm.uiState.value.userCallStatus)
    }

    @Test
    fun `onCallStatus EN_ROUTE updates userCallStatus without resetting`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")
        fakeSocket.fireCallDispatched(
            CallDispatched("call-1", "amb-1", 42.0, 23.0, "", 1000, 120)
        )

        fakeSocket.fireCallStatus(CallStatusUpdate("call-1", "en_route"))

        assertEquals(CallStatus.EN_ROUTE, vm.uiState.value.userCallStatus)
        assertEquals("call-1", vm.uiState.value.activeCallId)
        assertNotNull(vm.uiState.value.ambulanceLocation)
    }

    @Test
    fun `onAmbulanceLocation updates ambulance location`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")
        fakeSocket.fireCallDispatched(
            CallDispatched("call-1", "amb-1", 42.0, 23.0, "", 1000, 120)
        )

        fakeSocket.fireAmbulanceLocation(
            AmbulanceLocationUpdate("call-1", latitude = 42.6, longitude = 23.6, polyline = null, distance = null, duration = null)
        )

        val loc = vm.uiState.value.ambulanceLocation
        assertNotNull(loc)
        assertEquals(42.6, loc!!.latitude, 0.001)
        assertEquals(23.6, loc.longitude, 0.001)
    }

    @Test
    fun `onAmbulanceLocation ignored when status is ARRIVED`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")
        fakeSocket.fireCallDispatched(
            CallDispatched("call-1", "amb-1", 42.0, 23.0, "", 1000, 120)
        )
        fakeSocket.fireCallStatus(CallStatusUpdate("call-1", "arrived"))

        fakeSocket.fireAmbulanceLocation(
            AmbulanceLocationUpdate("call-1", latitude = 99.0, longitude = 99.0, polyline = null, distance = null, duration = null)
        )

        val loc = vm.uiState.value.ambulanceLocation
        assertNotNull(loc)
        assertEquals(42.0, loc!!.latitude, 0.001)
    }

    @Test
    fun `setPatientContext stores name and identified flag`() = runTest {
        val vm = buildVm()
        vm.setPatientContext("Ivan Ivanov", identified = true)

        assertEquals("Ivan Ivanov", vm.uiState.value.patientName)
        assertEquals(true, vm.uiState.value.patientIdentified)
    }

    @Test
    fun `clearCallState resets active call fields`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")
        fakeSocket.fireCallDispatched(
            CallDispatched("call-1", "amb-1", 42.0, 23.0, "", 1000, 120)
        )

        vm.clearCallState()

        val state = vm.uiState.value
        assertNull(state.activeCallId)
        assertNull(state.ambulanceLocation)
        assertNull(state.userCallStatus)
        assertTrue(state.activeRoutePolyline.isEmpty())
    }

    @Test
    fun `disconnectSocket calls disconnect and sets isSocketConnected false`() = runTest {
        val vm = buildVm()
        vm.setActiveCallId("call-1")
        fakeSocket.fireConnectionChange(true)
        assertTrue(vm.uiState.value.isSocketConnected)

        vm.disconnectSocket()

        assertTrue(fakeSocket.disconnectCalled)
        assertFalse(vm.uiState.value.isSocketConnected)
    }

    @Test
    fun `hasActiveCall returns true when activeCallId is set`() = runTest {
        val vm = buildVm()
        assertFalse(vm.hasActiveCall())

        vm.setActiveCallId("call-1")
        assertTrue(vm.hasActiveCall())
    }
}
