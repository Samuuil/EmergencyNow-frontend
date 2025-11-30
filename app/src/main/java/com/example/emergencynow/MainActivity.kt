package com.example.emergencynow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.emergencynow.ui.constants.Routes
import com.example.emergencynow.ui.extention.AuthSession
import com.example.emergencynow.ui.extention.AuthStorage
import com.example.emergencynow.ui.extention.BackendClient
import com.example.emergencynow.ui.extention.RefreshTokenRequest
import com.example.emergencynow.ui.extention.parseJwt
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import com.example.emergencynow.ui.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmergencyNowTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val tokens = AuthStorage.loadTokens(context)
                    val refreshToken = tokens.refreshToken
                    if (refreshToken != null) {
                        try {
                            val response = BackendClient.api.refresh(RefreshTokenRequest(refreshToken))
                            AuthSession.accessToken = response.accessToken
                            AuthSession.refreshToken = response.refreshToken
                            val payload = parseJwt(response.accessToken)
                            AuthSession.userId = payload?.sub
                            AuthStorage.saveTokens(context, response.accessToken, response.refreshToken)
                            startDestination = Routes.HOME
                        } catch (e: Exception) {
                            AuthStorage.clearTokens(context)
                            startDestination = Routes.WELCOME
                        }
                    } else {
                        startDestination = Routes.WELCOME
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    val destination = startDestination
                    if (destination != null) {
                        AppNavGraph(navController, startDestination = destination)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    EmergencyNowTheme {
        val navController = rememberNavController()
        AppNavGraph(navController)
    }
}