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
    private const val BASE_URL = "https://emergencynow.samuil.me"
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
        Log.d(TAG, "connect() called with token: ${accessToken.take(20)}...")
        
        // If already connected with an active socket, just log and return
        // The callbacks are already set on the manager object before connect() is called
        if (socket != null && isConnected) {
            Log.d(TAG, "Already connected to /users namespace")
            onConnectionChange?.invoke(true)
            return
        }

        // Clean up any existing socket before creating a new one
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

            Log.d(TAG, "Creating socket for $BASE_URL$NAMESPACE")
            socket = IO.socket(URI.create("$BASE_URL$NAMESPACE"), options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Connected to WebSocket /users namespace")
                isConnected = true
                onConnectionChange?.invoke(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                Log.d(TAG, "❌ Disconnected from WebSocket: ${args.joinToString()}")
                isConnected = false
                onConnectionChange?.invoke(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()
                Log.e(TAG, "❌ Connection error: $error (${error?.javaClass?.simpleName})")
                isConnected = false
                onConnectionChange?.invoke(false)
            }

            // Listen for call.dispatched - ambulance assigned to user's call
            socket?.on("call.dispatched") { args ->
                Log.d(TAG, "📥 Received call.dispatched event")
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
                    Log.d(TAG, "📍 Parsed ambulance.location: callId=${update.callId}, lat=${update.latitude}, lng=${update.longitude}")
                    
                    if (onAmbulanceLocation != null) {
                        Log.d(TAG, "Invoking onAmbulanceLocation callback")
                        onAmbulanceLocation?.invoke(update)
                    } else {
                        Log.w(TAG, "onAmbulanceLocation callback is NULL - location update ignored!")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error parsing ambulance.location: ${e.message}", e)
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
