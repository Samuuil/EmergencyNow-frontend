package com.example.emergencynow.ui.util

import android.util.Log
import com.example.emergencynow.BuildConfig
import com.example.emergencynow.domain.model.entity.DispatcherAmbulanceSummary
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.domain.model.entity.PatientRecord
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

class DispatcherSocketManager {
    companion object {
        private const val TAG = "DispatcherSocketManager"
        private const val NAMESPACE = "/dispatchers"
        private const val CONNECTION_TIMEOUT_MS = 10_000L
    }

    private var socket: Socket? = null
    private var isConnected = false
    private var connectionTimeoutHandler: android.os.Handler? = null

    var onCallAssigned: ((DispatcherCallOffer, List<DispatcherAmbulanceSummary>) -> Unit)? = null
    var onCallReleased: ((callId: String, reason: String) -> Unit)? = null
    var onCallCancelled: ((callId: String) -> Unit)? = null
    var onDriverAccepted: ((callId: String, ambulanceId: String) -> Unit)? = null
    var onDriverRejected: ((callId: String, ambulanceId: String, ambulances: List<DispatcherAmbulanceSummary>) -> Unit)? = null
    var onAmbulanceUnavailable: ((callId: String, ambulanceId: String, ambulances: List<DispatcherAmbulanceSummary>) -> Unit)? = null
    var onAmbulanceListUpdated: ((List<DispatcherAmbulanceSummary>) -> Unit)? = null
    var onConnectionChange: ((Boolean) -> Unit)? = null

    fun connect(accessToken: String) {
        if (socket != null) {
            Log.d(TAG, "Cleaning up existing socket before reconnecting")
            socket?.off()
            socket?.disconnect()
            socket = null
            isConnected = false
        }

        connectionTimeoutHandler?.removeCallbacksAndMessages(null)
        connectionTimeoutHandler = null
        onConnectionChange?.invoke(false)

        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to accessToken)
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = Integer.MAX_VALUE
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                timeout = 20_000
                forceNew = true
            }

            val base = BuildConfig.BASE_URL.removeSuffix("/")
            val uri = "${base}$NAMESPACE"
            Log.d(TAG, "Connecting to: $uri")
            socket = IO.socket(URI.create(uri), options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected (socket id: ${socket?.id()})")
                connectionTimeoutHandler?.removeCallbacksAndMessages(null)
                isConnected = true
                onConnectionChange?.invoke(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                val reason = args.firstOrNull()?.toString() ?: "unknown"
                Log.d(TAG, "Disconnected: $reason")
                connectionTimeoutHandler?.removeCallbacksAndMessages(null)
                isConnected = false
                onConnectionChange?.invoke(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()?.toString() ?: "unknown"
                Log.e(TAG, "Connect error: $error")
                connectionTimeoutHandler?.removeCallbacksAndMessages(null)
                isConnected = false
                onConnectionChange?.invoke(false)
            }

            socket?.on("call.assigned") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val callJson = data.getJSONObject("call")
                    val ambulancesJson = data.optJSONArray("ambulances")
                    val call = parseCallOffer(callJson)
                    val ambulances = parseAmbulanceList(ambulancesJson)
                    onCallAssigned?.invoke(call, ambulances)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.assigned: ${e.message}", e)
                }
            }

            socket?.on("call.released") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val callId = data.getString("callId")
                    val reason = data.optString("reason", "unknown")
                    onCallReleased?.invoke(callId, reason)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.released: ${e.message}", e)
                }
            }

            socket?.on("call.cancelled") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val callId = data.getString("callId")
                    onCallCancelled?.invoke(callId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.cancelled: ${e.message}", e)
                }
            }

            socket?.on("driver.accepted") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val callId = data.getString("callId")
                    val ambulanceId = data.getString("ambulanceId")
                    onDriverAccepted?.invoke(callId, ambulanceId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing driver.accepted: ${e.message}", e)
                }
            }

            socket?.on("driver.rejected") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val callId = data.getString("callId")
                    val ambulanceId = data.getString("ambulanceId")
                    val ambulances = parseAmbulanceList(data.optJSONArray("ambulances"))
                    onDriverRejected?.invoke(callId, ambulanceId, ambulances)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing driver.rejected: ${e.message}", e)
                }
            }

            socket?.on("ambulance.unavailable") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val callId = data.getString("callId")
                    val ambulanceId = data.getString("ambulanceId")
                    val ambulances = parseAmbulanceList(data.optJSONArray("ambulances"))
                    onAmbulanceUnavailable?.invoke(callId, ambulanceId, ambulances)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing ambulance.unavailable: ${e.message}", e)
                }
            }

            socket?.on("ambulance.list-updated") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val ambulances = parseAmbulanceList(data.optJSONArray("ambulances"))
                    onAmbulanceListUpdated?.invoke(ambulances)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing ambulance.list-updated: ${e.message}", e)
                }
            }

            socket?.connect()

            connectionTimeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
            connectionTimeoutHandler?.postDelayed({
                if (socket?.connected() != true && !isConnected) {
                    Log.e(TAG, "Connection timeout after ${CONNECTION_TIMEOUT_MS}ms")
                    onConnectionChange?.invoke(false)
                }
            }, CONNECTION_TIMEOUT_MS)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create socket: ${e.message}", e)
            isConnected = false
            onConnectionChange?.invoke(false)
        }
    }

    fun assignAmbulance(callId: String, ambulanceId: String) {
        if (socket == null || !isConnected) {
            Log.w(TAG, "Cannot assign — socket not connected")
            return
        }
        try {
            val data = JSONObject().apply {
                put("callId", callId)
                put("ambulanceId", ambulanceId)
            }
            socket?.emit("call.assign-ambulance", data)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending call.assign-ambulance: ${e.message}", e)
        }
    }

    fun requestAmbulanceRefresh() {
        if (socket == null || !isConnected) {
            Log.w(TAG, "Cannot request refresh — socket not connected")
            return
        }
        try {
            socket?.emit("dispatcher.refresh-ambulances", JSONObject())
        } catch (e: Exception) {
            Log.e(TAG, "Error sending dispatcher.refresh-ambulances: ${e.message}", e)
        }
    }

    fun disconnect() {
        connectionTimeoutHandler?.removeCallbacksAndMessages(null)
        connectionTimeoutHandler = null
        socket?.disconnect()
        socket?.off()
        socket = null
        val wasConnected = isConnected
        isConnected = false
        if (wasConnected) onConnectionChange?.invoke(false)
    }

    fun isConnected(): Boolean = isConnected && socket?.connected() == true

    private fun parseCallOffer(json: JSONObject): DispatcherCallOffer {
        return DispatcherCallOffer(
            callId = json.getString("callId"),
            description = json.optString("description", ""),
            latitude = json.getDouble("latitude"),
            longitude = json.getDouble("longitude"),
            createdAt = json.optString("createdAt", ""),
            userName = if (json.isNull("userName")) null else json.optString("userName", null),
            patient = parsePatient(json.optJSONObject("patient")),
        )
    }

    private fun parsePatient(json: JSONObject?): PatientRecord? {
        if (json == null) return null
        return PatientRecord(
            egn = json.optString("egn", ""),
            fullName = json.optString("fullName", ""),
            phoneNumber = json.optString("phoneNumber", ""),
            email = json.optString("email", ""),
            bloodType = if (json.isNull("bloodType")) null else json.optString("bloodType"),
            allergies = parseStringList(json.optJSONArray("allergies")),
            medicines = parseStringList(json.optJSONArray("medicines")),
            illnesses = parseStringList(json.optJSONArray("illnesses")),
            height = if (json.isNull("height")) null else json.optInt("height"),
            weight = if (json.isNull("weight")) null else json.optInt("weight"),
            gender = if (json.isNull("gender")) null else json.optString("gender"),
            dateOfBirth = if (json.isNull("dateOfBirth")) null else json.optString("dateOfBirth"),
        )
    }

    private fun parseStringList(arr: JSONArray?): List<String>? {
        if (arr == null) return null
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) list.add(arr.optString(i, ""))
        return list
    }

    private fun parseAmbulanceList(arr: JSONArray?): List<DispatcherAmbulanceSummary> {
        if (arr == null) return emptyList()
        val out = mutableListOf<DispatcherAmbulanceSummary>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            out.add(
                DispatcherAmbulanceSummary(
                    id = obj.getString("id"),
                    licensePlate = obj.optString("licensePlate", ""),
                    vehicleModel = if (obj.isNull("vehicleModel")) null else obj.optString("vehicleModel", null),
                    latitude = if (obj.isNull("latitude")) null else obj.optDouble("latitude").takeIf { !it.isNaN() },
                    longitude = if (obj.isNull("longitude")) null else obj.optDouble("longitude").takeIf { !it.isNaN() },
                    driverId = if (obj.isNull("driverId")) null else obj.optString("driverId", null),
                    driverOnline = obj.optBoolean("driverOnline", false),
                    available = obj.optBoolean("available", false),
                )
            )
        }
        return out
    }
}
