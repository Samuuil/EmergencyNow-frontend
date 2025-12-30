package com.example.emergencynow.ui.util

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI
import com.example.emergencynow.ui.util.NetworkConfig

data class CallOffer(
    val callId: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Int,
    val duration: Int,
    val priority: String = "HIGH"
)

data class CallRoute(
    val callId: String,
    val polyline: String,
    val distance: Int,
    val duration: Int,
    val steps: List<String>
)

object DriverSocketManager {
    private const val TAG = "DriverSocketManager"
    private const val NAMESPACE = "/drivers"
    private const val CONNECTION_TIMEOUT_MS = 10000L // 10 seconds

    private var socket: Socket? = null
    private var isConnected = false
    private var connectionTimeoutHandler: android.os.Handler? = null

    var onCallOffer: ((CallOffer) -> Unit)? = null
    var onCallRoute: ((CallRoute) -> Unit)? = null
    var onRouteUpdate: ((CallRoute) -> Unit)? = null
    var onConnectionChange: ((Boolean) -> Unit)? = null

    var onLocationRequest: ((requestId: Int) -> Unit)? = null

    fun connect(accessToken: String) {
        Log.d(TAG, "════════════════════════════════════════")
        Log.d(TAG, "🔌 DRIVER SOCKET CONNECT REQUESTED")
        Log.d(TAG, "Token: ${accessToken.take(20)}...")
        Log.d(TAG, "Current socket state: ${socket?.connected()}")
        Log.d(TAG, "isConnected flag: $isConnected")
        Log.d(TAG, "════════════════════════════════════════")
        
        // Check if already connected and socket is actually connected
        if (socket != null && socket?.connected() == true && isConnected) {
            Log.d(TAG, "✅ Already connected - socket ID: ${socket?.id()}")
            onConnectionChange?.invoke(true)
            return
        }

        if (socket != null) {
            Log.d(TAG, "🧹 Cleaning up existing disconnected socket...")
            socket?.disconnect()
            socket?.off()
            socket = null
            isConnected = false
        }

        // Cancel any pending timeout
        connectionTimeoutHandler?.removeCallbacksAndMessages(null)
        connectionTimeoutHandler = null
        
        // Notify that we're connecting
        onConnectionChange?.invoke(false)

        try {
            Log.d(TAG, "⚙️ Creating socket options...")
            val options = IO.Options().apply {
                auth = mapOf("token" to accessToken)
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = Integer.MAX_VALUE // Keep trying
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                timeout = 20000 // 20 second connection timeout
                forceNew = true // Force a new connection
            }

            val base = NetworkConfig.currentBase()
            val uri = "${base}$NAMESPACE"
            Log.d(TAG, "🌐 Connecting to: $uri")
            socket = IO.socket(URI.create(uri), options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Connected to WebSocket - socket ID: ${socket?.id()}")
                connectionTimeoutHandler?.removeCallbacksAndMessages(null)
                isConnected = true
                onConnectionChange?.invoke(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                val reason = args.firstOrNull()?.toString() ?: "unknown"
                Log.d(TAG, "Disconnected from WebSocket: $reason")
                connectionTimeoutHandler?.removeCallbacksAndMessages(null)
                isConnected = false
                onConnectionChange?.invoke(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()
                when (error) {
                    is Exception -> {
                        Log.e(TAG, "Connection error (Exception): ${error.message}", error)
                        error.printStackTrace()
                    }
                    else -> Log.e(TAG, "Connection error: $error (${error?.javaClass?.simpleName})")
                }
                connectionTimeoutHandler?.removeCallbacksAndMessages(null)
                isConnected = false
                onConnectionChange?.invoke(false)
                // One-time fallback retry to localhost if primary is unreachable
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

            socket?.on("call.offer") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val offer = CallOffer(
                        callId = data.getString("callId"),
                        description = data.optString("description", "Emergency Call"),
                        latitude = data.getDouble("latitude"),
                        longitude = data.getDouble("longitude"),
                        distance = data.optInt("distance", 0),
                        duration = data.optInt("duration", 0),
                        priority = data.optString("priority", "HIGH")
                    )
                    Log.d(TAG, "Received call offer: $offer")
                    onCallOffer?.invoke(offer)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.offer: ${e.message}")
                }
            }

            socket?.on("call.route") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val routeObj = data.getJSONObject("route")
                    val steps = mutableListOf<String>()
                    if (routeObj.has("steps")) {
                        val stepsAny = routeObj.get("steps")
                        when (stepsAny) {
                            is org.json.JSONArray -> {
                                for (i in 0 until stepsAny.length()) {
                                    val item = stepsAny.get(i)
                                    when (item) {
                                        is String -> steps.add(item)
                                        is JSONObject -> steps.add(item.optString("instruction", item.toString()))
                                    }
                                }
                            }
                            is JSONObject -> {
                                steps.add(stepsAny.optString("instruction", stepsAny.toString()))
                            }
                        }
                    }
                    val route = CallRoute(
                        callId = data.getString("callId"),
                        polyline = routeObj.getString("polyline"),
                        distance = routeObj.getInt("distance"),
                        duration = routeObj.getInt("duration"),
                        steps = steps
                    )
                    Log.d(TAG, "Received call route: $route")
                    onCallRoute?.invoke(route)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call.route: ${e.message}")
                }
            }

            socket?.on("route.update") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject ?: return@on
                    val routeObj = data.getJSONObject("route")
                    val steps = mutableListOf<String>()
                    if (routeObj.has("steps")) {
                        val stepsAny = routeObj.get("steps")
                        when (stepsAny) {
                            is org.json.JSONArray -> {
                                for (i in 0 until stepsAny.length()) {
                                    val item = stepsAny.get(i)
                                    when (item) {
                                        is String -> steps.add(item)
                                        is JSONObject -> steps.add(item.optString("instruction", item.toString()))
                                    }
                                }
                            }
                            is JSONObject -> {
                                steps.add(stepsAny.optString("instruction", stepsAny.toString()))
                            }
                        }
                    }
                    val route = CallRoute(
                        callId = data.getString("callId"),
                        polyline = routeObj.getString("polyline"),
                        distance = routeObj.getInt("distance"),
                        duration = routeObj.getInt("duration"),
                        steps = steps
                    )
                    Log.d(TAG, "Received route update: $route")
                    onRouteUpdate?.invoke(route)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing route.update: ${e.message}")
                }
            }

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
            Log.d(TAG, "socket.connect() initiated...")
            
            // Set up a timeout to check connection status
            connectionTimeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
            connectionTimeoutHandler?.postDelayed({
                if (socket != null && !socket!!.connected() && !isConnected) {
                    Log.w(TAG, "Connection timeout - socket still not connected after ${CONNECTION_TIMEOUT_MS}ms")
                    // Force disconnect and try again
                    socket?.disconnect()
                    socket?.off()
                    socket = null
                    isConnected = false
                    onConnectionChange?.invoke(false)
                    // Retry connection
                    Log.d(TAG, "Retrying connection after timeout...")
                    connect(accessToken)
                } else if (socket != null && socket!!.connected() && !isConnected) {
                    // Socket is connected but our state is wrong - fix it
                    Log.w(TAG, "Socket is connected but state was wrong - fixing...")
                    isConnected = true
                    onConnectionChange?.invoke(true)
                }
            }, CONNECTION_TIMEOUT_MS)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create socket: ${e.message}", e)
            e.printStackTrace()
            isConnected = false
            onConnectionChange?.invoke(false)
        }
    }

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

    fun acceptCall(callId: String) {
        respondToCall(callId, true)
    }

    fun declineCall(callId: String) {
        respondToCall(callId, false)
    }

    fun completeCall(callId: String) {
        if (socket == null || !isConnected) {
            Log.w(TAG, "Cannot complete call - socket not connected")
            return
        }

        try {
            val data = JSONObject().apply {
                put("callId", callId)
            }
            socket?.emit("call.complete", data)
            Log.d(TAG, "Sent call.complete: callId=$callId")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending call.complete: ${e.message}")
        }
    }

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

    fun sendLocationUpdate(callId: String, latitude: Double, longitude: Double) {
        if (socket == null || !isConnected) {
            Log.w(TAG, "⚠️ Cannot send location - socket not connected (socket=${socket != null}, connected=$isConnected)")
            return
        }

        try {
            val data = JSONObject().apply {
                put("callId", callId)
                put("latitude", latitude)
                put("longitude", longitude)
            }
            socket?.emit("location.update", data)
            Log.d(TAG, "Sent location.update: callId=$callId, lat=$latitude, lng=$longitude")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location.update: ${e.message}")
        }
    }


    fun sendLocationUpdate(latitude: Double, longitude: Double) {
        if (socket == null || !isConnected) {
            Log.w(TAG, "Cannot send location - socket not connected")
            return
        }

        try {
            val data = JSONObject().apply {
                put("latitude", latitude)
                put("longitude", longitude)
            }
            socket?.emit("location.update", data)
            Log.d(TAG, "Sent location.update: lat=$latitude, lng=$longitude")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location.update: ${e.message}")
        }
    }

    fun disconnect() {
        Log.d(TAG, "🔌 Disconnect requested")
        connectionTimeoutHandler?.removeCallbacksAndMessages(null)
        connectionTimeoutHandler = null
        socket?.disconnect()
        socket?.off()
        socket = null
        val wasConnected = isConnected
        isConnected = false
        // Notify listeners if we were connected
        if (wasConnected) {
            Log.d(TAG, "Notifying listeners of disconnection")
            onConnectionChange?.invoke(false)
        }
        Log.d(TAG, "✅ Disconnected and cleaned up socket")
    }

    fun isConnected(): Boolean {
        // Verify actual socket state, not just our cached state
        val actuallyConnected = socket?.connected() == true
        if (actuallyConnected != isConnected) {
            Log.w(TAG, "Connection state mismatch - socket.connected()=$actuallyConnected, isConnected=$isConnected - fixing...")
            isConnected = actuallyConnected
            onConnectionChange?.invoke(actuallyConnected)
        }
        return isConnected
    }
    
    fun forceReconnect(accessToken: String) {
        Log.d(TAG, "════════════════════════════════════════")
        Log.d(TAG, "🔄 FORCE RECONNECT REQUESTED")
        Log.d(TAG, "Current state: socket=${socket != null}, connected=${socket?.connected()}, isConnected=$isConnected")
        Log.d(TAG, "Callbacks set: onConnectionChange=${onConnectionChange != null}, onCallOffer=${onCallOffer != null}")
        Log.d(TAG, "════════════════════════════════════════")
        
        // Don't call disconnect as it might interfere - just reconnect
        connect(accessToken)
        
        Log.d(TAG, "✅ Force reconnect initiated")
    }
}
