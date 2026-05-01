package com.example.emergencynow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.emergencynow.ui.AppViewModel
import com.example.emergencynow.ui.StartupState
import com.example.emergencynow.ui.components.NotificationHost
import com.example.emergencynow.ui.constants.HomeRoute
import com.example.emergencynow.ui.constants.WelcomeRoute
import com.example.emergencynow.ui.navigation.AppNavGraph
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmergencyNowTheme {
                val navController = rememberNavController()
                val appViewModel: AppViewModel = koinViewModel()
                val startupState by appViewModel.startupState.collectAsStateWithLifecycle()

                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val startDestination = when (startupState) {
                            StartupState.Authenticated -> HomeRoute
                            StartupState.Unauthenticated -> WelcomeRoute
                            StartupState.Loading -> null
                        }
                        if (startDestination != null) {
                            AppNavGraph(navController, startDestination = startDestination)
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
