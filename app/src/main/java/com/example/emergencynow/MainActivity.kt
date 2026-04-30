package com.example.emergencynow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.domain.usecase.auth.RefreshTokenUseCase
import com.example.emergencynow.ui.util.parseJwt
import com.example.emergencynow.ui.constants.HomeRoute
import com.example.emergencynow.ui.constants.WelcomeRoute
import org.koin.core.context.GlobalContext
import com.example.emergencynow.ui.components.NotificationHost
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import com.example.emergencynow.ui.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmergencyNowTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<Any?>(null) }

                LaunchedEffect(Unit) {
                    val authStorage = GlobalContext.get().get<AuthStorage>()
                    val refreshToken = authStorage.refreshToken
                    if (refreshToken != null) {
                        try {
                            val refreshTokenUseCase = GlobalContext.get().get<RefreshTokenUseCase>()
                            val token = refreshTokenUseCase(refreshToken).getOrThrow()
                            authStorage.accessToken = token.accessToken
                            authStorage.refreshToken = token.refreshToken
                            val payload = parseJwt(token.accessToken)
                            AuthSession.userId = payload?.sub
                            startDestination = HomeRoute
                        } catch (e: Exception) {
                            authStorage.clear()
                            startDestination = WelcomeRoute
                        }
                    } else {
                        startDestination = WelcomeRoute
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                        val destination = startDestination
                        if (destination != null) {
                            AppNavGraph(navController, startDestination = destination)
                        }
                        
                        NotificationHost()
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