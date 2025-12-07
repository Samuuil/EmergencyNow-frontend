package com.example.emergencynow.ui.extention

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI

/**
 * Data class for call.dispatched event - when ambulance is assigned to user's call
 */
data class CallDispatched(
    val callId: String,
    val ambulanceId: String,
    val ambulanceLatitude: Double,
    val ambulanceLongitude: Double,
    val polyline: String,
    val distance: Int,
    val duration: Int
)

/**
 * Data class for ambulance.location event - live ambulance position updates
 */
data class AmbulanceLocationUpdate(
    val callId: String,
    val latitude: Double,
    val longitude: Double,
    val polyline: String?,
    val distance: Int?,
    val duration: Int?
)

/**
 * Data class for call.status event - call status changes
 */
data class CallStatusUpdate(
    val callId: String,
    val status: String
)

/**
 * Singleton manager for the user WebSocket connection.
 * Connects to the /users namespace and handles call.dispatched, ambulance.location, call.status events.
 */
object UserSocketManager {
    private const val TAG = "UserSocketManager"
    private const val BASE_URL = "http://localhost:3000"
    private const val NAMESPACE = "/users"

    private var socket: Socket? = null
    private var isConnected = false

    // Callbacks
    var onCallDispatched: ((CallDispatched) -> Unit)? = null
    var onAmbulanceLocation: ((AmbulanceLocationUpdate) -> Unit)? = null
    var onCallStatus: ((CallStatusUpdate) -> Unit)? = null
    var onConnectionChange: ((Boolean) -> Unit)? = null

    /**
     * Connect to the WebSocket server with JWT authentication.
     */
    fun connect(accessToken: String) {
        if (socket != null && isConnected) {
            Log.d(TAG, "Already connected")
            return
        }

        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to accessToken)
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 1000
            }

            socket = IO.socket(URI.create("$BASE_URL$NAMESPACE"), options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to WebSocket /users namespace")
                isConnected = true
                onConnectionChange?.invoke(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Disconnected from WebSocket")
                isConnected = false
                onConnectionChange?.invoke(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()
                Log.e(TAG, "Connection error: $error")
                isConnected = false
                onConnectionChange?.invoke(false)
            }

            // Listen for call.dispatched - ambulance assigned to user's call
            socket?.on("call.dispatched") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
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
                    Log.d(TAG, "Received call.dispatched: callId=${dispatched.callId}")
                    onCallDispatched?.invoke(dispatched)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.dispatched: ${e.message}")
                }
            }

            // Listen for ambulance.location - live position updates
            socket?.on("ambulance.location") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
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
                    Log.d(TAG, "Received ambulance.location: lat=${update.latitude}, lng=${update.longitude}")
                    onAmbulanceLocation?.invoke(update)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing ambulance.location: ${e.message}")
                }
            }

            // Listen for call.status - status changes
            socket?.on("call.status") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val statusUpdate = CallStatusUpdate(
                        callId = data.getString("callId"),
                        status = data.getString("status")
                    )
                    Log.d(TAG, "Received call.status: ${statusUpdate.status}")
                    onCallStatus?.invoke(statusUpdate)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.status: ${e.message}")
                }
            }

            socket?.connect()
            Log.d(TAG, "Connecting to WebSocket...")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create socket: ${e.message}")
        }
    }

    /**
     * Disconnect from the WebSocket server.
     */
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

    /**
     * Check if currently connected.
     */
    fun isConnected(): Boolean = isConnected
}
