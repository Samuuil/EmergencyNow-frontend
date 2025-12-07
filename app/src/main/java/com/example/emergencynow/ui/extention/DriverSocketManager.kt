package com.example.emergencynow.ui.extention

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI

/**
 * Data class representing an incoming call offer from the backend.
 */
data class CallOffer(
    val callId: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Int,    // meters
    val duration: Int     // seconds
)

/**
 * Data class representing route information after accepting a call.
 */
data class CallRoute(
    val callId: String,
    val polyline: String,
    val distance: Int,
    val duration: Int,
    val steps: List<Any>
)

/**
 * Singleton manager for the driver WebSocket connection.
 * Connects to the /drivers namespace and handles call.offer, call.route events.
 */
object DriverSocketManager {
    private const val TAG = "DriverSocketManager"
    // Use 10.0.2.2 for Android emulator to reach host machine's localhost
    //private const val BASE_URL = "http://10.0.2.2:3000"
    //private const val BASE_URL = "http://192.168.5.32:3000"
    private const val BASE_URL = "http://localhost:3000"
    private const val NAMESPACE = "/drivers"

    private var socket: Socket? = null
    private var isConnected = false

    // Callbacks
    var onCallOffer: ((CallOffer) -> Unit)? = null
    var onCallRoute: ((CallRoute) -> Unit)? = null
    var onRouteUpdate: ((CallRoute) -> Unit)? = null
    var onConnectionChange: ((Boolean) -> Unit)? = null
    
    // Callback for location requests - frontend should respond with GPS coordinates
    var onLocationRequest: ((requestId: Int) -> Unit)? = null

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
                Log.d(TAG, "Connected to WebSocket")
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

            // Listen for incoming call offers
            socket?.on("call.offer") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val offer = CallOffer(
                        callId = data.getString("callId"),
                        description = data.optString("description", "Emergency Call"),
                        latitude = data.getDouble("latitude"),
                        longitude = data.getDouble("longitude"),
                        distance = data.optInt("distance", 0),
                        duration = data.optInt("duration", 0)
                    )
                    Log.d(TAG, "Received call offer: $offer")
                    onCallOffer?.invoke(offer)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.offer: ${e.message}")
                }
            }

            // Listen for route information after accepting a call
            socket?.on("call.route") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val routeObj = data.getJSONObject("route")
                    val route = CallRoute(
                        callId = data.getString("callId"),
                        polyline = routeObj.getString("polyline"),
                        distance = routeObj.getInt("distance"),
                        duration = routeObj.getInt("duration"),
                        steps = emptyList() // simplified for now
                    )
                    Log.d(TAG, "Received call route: $route")
                    onCallRoute?.invoke(route)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.route: ${e.message}")
                }
            }

            // Listen for route updates during navigation
            socket?.on("route.update") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val routeObj = data.getJSONObject("route")
                    val route = CallRoute(
                        callId = data.getString("callId"),
                        polyline = routeObj.getString("polyline"),
                        distance = routeObj.getInt("distance"),
                        duration = routeObj.getInt("duration"),
                        steps = emptyList()
                    )
                    Log.d(TAG, "Received route update: $route")
                    onRouteUpdate?.invoke(route)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing route.update: ${e.message}")
                }
            }

            // Listen for location requests - backend asks for fresh GPS before deciding who gets call
            socket?.on("location.request") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val requestId = data.getInt("requestId")
                    Log.d(TAG, "Received location.request: requestId=$requestId")
                    onLocationRequest?.invoke(requestId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing location.request: ${e.message}")
                }
            }

            socket?.connect()
            Log.d(TAG, "Connecting to WebSocket...")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create socket: ${e.message}")
        }
    }

    /**
     * Send response to a call offer (accept or reject).
     */
    fun respondToCall(callId: String, accept: Boolean) {
        if (socket == null || !isConnected) {
            Log.w(TAG, "Cannot respond - socket not connected")
            return
        }

        try {
            val data = JSONObject().apply {
                put("callId", callId)
                put("accept", accept)
            }
            socket?.emit("call.respond", data)
            Log.d(TAG, "Sent call.respond: callId=$callId, accept=$accept")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending call.respond: ${e.message}")
        }
    }

    /**
     * Send location response to a location.request from backend.
     * This is called when backend needs fresh GPS data before dispatching a call.
     */
    fun sendLocationResponse(requestId: Int, latitude: Double, longitude: Double) {
        if (socket == null || !isConnected) {
            Log.w(TAG, "Cannot send location response - socket not connected")
            return
        }

        try {
            val data = JSONObject().apply {
                put("requestId", requestId)
                put("latitude", latitude)
                put("longitude", longitude)
            }
            socket?.emit("location.response", data)
            Log.d(TAG, "Sent location.response: requestId=$requestId, lat=$latitude, lng=$longitude")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location.response: ${e.message}")
        }
    }

    /**
     * Send location update during an active call.
     */
    fun sendLocationUpdate(callId: String, latitude: Double, longitude: Double) {
        if (socket == null || !isConnected) {
            Log.w(TAG, "Cannot send location - socket not connected")
            return
        }

        try {
            val data = JSONObject().apply {
                put("callId", callId)
                put("latitude", latitude)
                put("longitude", longitude)
            }
            socket?.emit("location.update", data)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location.update: ${e.message}")
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
        Log.d(TAG, "Disconnected and cleaned up socket")
    }

    /**
     * Check if currently connected.
     */
    fun isConnected(): Boolean = isConnected
}
