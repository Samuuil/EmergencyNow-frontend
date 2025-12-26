package com.example.emergencynow.ui.util

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI
import com.example.emergencynow.ui.util.NetworkConfig

data class CallDispatched(
    val callId: String,
    val ambulanceId: String,
    val ambulanceLatitude: Double,
    val ambulanceLongitude: Double,
    val polyline: String,
    val distance: Int,
    val duration: Int
)

data class AmbulanceLocationUpdate(
    val callId: String,
    val latitude: Double,
    val longitude: Double,
    val polyline: String?,
    val distance: Int?,
    val duration: Int?
)

data class CallStatusUpdate(
    val callId: String,
    val status: String
)

object UserSocketManager {
    private const val TAG = "UserSocketManager"
    private const val NAMESPACE = "/users"

    private var socket: Socket? = null
    private var isConnected = false

    var onCallDispatched: ((CallDispatched) -> Unit)? = null
    var onAmbulanceLocation: ((AmbulanceLocationUpdate) -> Unit)? = null
    var onCallStatus: ((CallStatusUpdate) -> Unit)? = null
    var onConnectionChange: ((Boolean) -> Unit)? = null

    fun connect(accessToken: String) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "connect() called with token: ${accessToken.take(20)}...")
        Log.d(TAG, "Current socket state: socket=${socket != null}, isConnected=$isConnected")
        Log.d(TAG, "Callbacks set: onCallDispatched=${onCallDispatched != null}, onAmbulanceLocation=${onAmbulanceLocation != null}, onCallStatus=${onCallStatus != null}")
        Log.d(TAG, "========================================")

        if (socket != null && isConnected) {
            Log.d(TAG, "Socket already connected - callbacks will be invoked when events arrive")
            Log.d(TAG, "Socket connected: ${socket?.connected()}, Socket ID: ${socket?.id()}")
            onConnectionChange?.invoke(true)
            return
        }

        if (socket != null) {
            Log.d(TAG, "Cleaning up existing disconnected socket...")
            socket?.off()
            socket?.disconnect()
            socket = null
            isConnected = false
        }

        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to accessToken)
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 1000
            }

            val base = NetworkConfig.currentBase()
            Log.d(TAG, "Creating socket for ${base}$NAMESPACE")
            socket = IO.socket(URI.create("${base}$NAMESPACE"), options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to WebSocket /users namespace")
                isConnected = true
                onConnectionChange?.invoke(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                Log.d(TAG, "Disconnected from WebSocket: ${args.joinToString()}")
                isConnected = false
                onConnectionChange?.invoke(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()
                Log.e(TAG, "Connection error: $error (${error?.javaClass?.simpleName})")
                isConnected = false
                onConnectionChange?.invoke(false)
                // One-time fallback retry
                if (NetworkConfig.isPrimary()) {
                    try {
                        Log.w(TAG, "Retrying with fallback base: ${NetworkConfig.fallbackBaseUrl()}")
                        NetworkConfig.switchToFallback()
                        disconnect()
                        connect(accessToken)
                    } catch (e: Exception) {
                        Log.e(TAG, "Fallback retry failed: ${e.message}")
                    }
                }
            }

            socket?.on("call.dispatched") { args ->
                Log.d(TAG, "========================================")
                Log.d(TAG, "SOCKET EVENT: call.dispatched RECEIVED!")
                Log.d(TAG, "Args count: ${args.size}")
                Log.d(TAG, "Callback set: ${onCallDispatched != null}")
                Log.d(TAG, "========================================")
                try {
                    val data = args.firstOrNull() as? JSONObject
                    if (data == null) {
                        Log.e(TAG, "call.dispatched: data is null, raw: ${args.firstOrNull()}")
                        return@on
                    }
                    Log.d(TAG, "Raw JSON: $data")
                    
                    val ambulanceLocation = data.getJSONObject("ambulanceLocation")
                    val route = data.getJSONObject("route")
                    
                    val dispatched = CallDispatched(
                        callId = data.getString("callId"),
                        ambulanceId = data.getString("ambulanceId"),
                        ambulanceLatitude = ambulanceLocation.getDouble("latitude"),
                        ambulanceLongitude = ambulanceLocation.getDouble("longitude"),
                        polyline = route.getString("polyline"),
                        distance = route.getInt("distance"),
                        duration = route.getInt("duration")
                    )
                    Log.d(TAG, "Parsed call.dispatched: callId=${dispatched.callId}")
                    
                    if (onCallDispatched != null) {
                        Log.d(TAG, "Invoking onCallDispatched callback...")
                        onCallDispatched?.invoke(dispatched)
                        Log.d(TAG, "Callback invoked successfully")
                    } else {
                        Log.e(TAG, "onCallDispatched callback is NULL!")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.dispatched: ${e.message}", e)
                }
            }

            socket?.on("ambulance.location") { args ->
                Log.d(TAG, "📥 Received ambulance.location event, args count: ${args.size}")
                try {
                    val data = args.firstOrNull() as? JSONObject
                    if (data == null) {
                        Log.e(TAG, "ambulance.location: data is null or not JSONObject, raw: ${args.firstOrNull()}")
                        return@on
                    }
                    Log.d(TAG, "ambulance.location raw data: $data")
                    
                    val ambulanceLocation = data.getJSONObject("ambulanceLocation")
                    val route = data.optJSONObject("route")
                    
                    val update = AmbulanceLocationUpdate(
                        callId = data.getString("callId"),
                        latitude = ambulanceLocation.getDouble("latitude"),
                        longitude = ambulanceLocation.getDouble("longitude"),
                        polyline = route?.optString("polyline"),
                        distance = route?.optInt("distance"),
                        duration = route?.optInt("duration")
                    )
                    Log.d(TAG, "Parsed ambulance.location: callId=${update.callId}, lat=${update.latitude}, lng=${update.longitude}")
                    
                    if (onAmbulanceLocation != null) {
                        Log.d(TAG, "Invoking onAmbulanceLocation callback")
                        onAmbulanceLocation?.invoke(update)
                    } else {
                        Log.w(TAG, "onAmbulanceLocation callback is NULL - location update ignored!")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing ambulance.location: ${e.message}", e)
                }
            }

            socket?.on("call.status") { args ->
                Log.d(TAG, "========================================")
                Log.d(TAG, "SOCKET EVENT: call.status RECEIVED!")
                Log.d(TAG, "Args count: ${args.size}")
                Log.d(TAG, "Callback set: ${onCallStatus != null}")
                Log.d(TAG, "========================================")
                try {
                    val data = args.firstOrNull() as? JSONObject
                    if (data == null) {
                        Log.e(TAG, "call.status: data is null, raw: ${args.firstOrNull()}")
                        return@on
                    }
                    Log.d(TAG, "Raw JSON: $data")
                    
                    val statusUpdate = CallStatusUpdate(
                        callId = data.getString("callId"),
                        status = data.getString("status")
                    )
                    Log.d(TAG, "Parsed call.status: callId=${statusUpdate.callId}, status=${statusUpdate.status}")
                    
                    if (onCallStatus != null) {
                        Log.d(TAG, "Invoking onCallStatus callback...")
                        onCallStatus?.invoke(statusUpdate)
                        Log.d(TAG, "Callback invoked successfully")
                    } else {
                        Log.e(TAG, "onCallStatus callback is NULL!")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.status: ${e.message}", e)
                }
            }

            socket?.connect()
            Log.d(TAG, "Connecting to WebSocket...")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create socket: ${e.message}")
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        isConnected = false
        onCallDispatched = null
        onAmbulanceLocation = null
        onCallStatus = null
        onConnectionChange = null
        Log.d(TAG, "Disconnected and cleaned up socket")
    }

    fun isConnected(): Boolean = isConnected
}
