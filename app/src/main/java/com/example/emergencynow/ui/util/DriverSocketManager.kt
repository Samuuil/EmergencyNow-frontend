package com.example.emergencynow.ui.util

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI

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
    val steps: List<Any>
)

object DriverSocketManager {
    private const val TAG = "DriverSocketManager"
    private const val BASE_URL = "https://emergencynow.samuil.me"
    private const val NAMESPACE = "/drivers"

    private var socket: Socket? = null
    private var isConnected = false

    var onCallOffer: ((CallOffer) -> Unit)? = null
    var onCallRoute: ((CallRoute) -> Unit)? = null
    var onRouteUpdate: ((CallRoute) -> Unit)? = null
    var onConnectionChange: ((Boolean) -> Unit)? = null

    var onLocationRequest: ((requestId: Int) -> Unit)? = null

    fun connect(accessToken: String) {
        Log.d(TAG, "connect() called with token: ${accessToken.take(20)}...")
        if (socket != null && isConnected) {
            Log.d(TAG, "Already connected")
            return
        }

        if (socket != null) {
            Log.d(TAG, "Cleaning up existing disconnected socket...")
            socket?.disconnect()
            socket?.off()
            socket = null
        }

        try {
            Log.d(TAG, "Creating socket options...")
            val options = IO.Options().apply {
                auth = mapOf("token" to accessToken)
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 1000
            }

            val uri = "$BASE_URL$NAMESPACE"
            Log.d(TAG, "Connecting to: $uri")
            socket = IO.socket(URI.create(uri), options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to WebSocket")
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
                when (error) {
                    is Exception -> {
                        Log.e(TAG, "Connection error (Exception): ${error.message}", error)
                        error.printStackTrace()
                    }
                    else -> Log.e(TAG, "Connection error: $error (${error?.javaClass?.simpleName})")
                }
                isConnected = false
                onConnectionChange?.invoke(false)
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
                    val route = CallRoute(
                        callId = data.getString("callId"),
                        polyline = routeObj.getString("polyline"),
                        distance = routeObj.getInt("distance"),
                        duration = routeObj.getInt("duration"),
                        steps = emptyList()
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

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create socket: ${e.message}", e)
            e.printStackTrace()
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
        socket?.disconnect()
        socket?.off()
        socket = null
        isConnected = false
        Log.d(TAG, "Disconnected and cleaned up socket")
    }

    fun isConnected(): Boolean = isConnected
}
