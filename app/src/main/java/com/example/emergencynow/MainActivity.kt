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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.domain.usecase.auth.RefreshTokenUseCase
import com.example.emergencynow.data.util.JwtHelper
import com.example.emergencynow.ui.constants.Routes
import org.koin.core.context.GlobalContext
import com.example.emergencynow.ui.components.NotificationHost
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import com.example.emergencynow.ui.navigation.AppNavGraph
import kotlinx.coroutines.launch

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
                            val refreshTokenUseCase = GlobalContext.get().get<RefreshTokenUseCase>()
                            val token = refreshTokenUseCase(refreshToken).getOrThrow()
                            AuthSession.accessToken = token.accessToken
                            AuthSession.refreshToken = token.refreshToken
                            val payload = JwtHelper.parseJwt(token.accessToken)
                            AuthSession.userId = payload?.sub
                            AuthStorage.saveTokens(context, token.accessToken, token.refreshToken)
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