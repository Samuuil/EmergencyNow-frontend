package com.example.emergencynow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.emergencynow.ui.AppViewModel
import com.example.emergencynow.ui.StartupState
import com.example.emergencynow.ui.components.NotificationHost
import com.example.emergencynow.ui.constants.HomeRoute
import com.example.emergencynow.ui.constants.WelcomeRoute
import com.example.emergencynow.ui.feature.permissions.PermissionsScreen
import com.example.emergencynow.ui.navigation.AppNavGraph
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.BrandBlueMid
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import com.example.emergencynow.ui.theme.PrimaryDarkBlue
import kotlinx.coroutines.launch
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
                val isOnline by appViewModel.isOnline.collectAsStateWithLifecycle()

                // --- Permission gating ---
                var permissionsGranted by remember { mutableStateOf(checkPermissionsGranted()) }
                var permissionsRequested by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    permissionsRequested = true
                    permissionsGranted = results.values.all { it }
                }

                // Re-check permissions whenever the app comes back to foreground (e.g. from Settings)
                val lifecycleOwner = LocalLifecycleOwner.current
                val coroutineScope = rememberCoroutineScope()
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            permissionsGranted = checkPermissionsGranted()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // Auto-request on first launch
                LaunchedEffect(Unit) {
                    if (!permissionsGranted) {
                        permissionLauncher.launch(requiredPermissions())
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!permissionsGranted) {
                            val permanentlyDenied = permissionsRequested &&
                                    !permissionsGranted &&
                                    requiredPermissions().none {
                                        shouldShowRequestPermissionRationale(it)
                                    }
                            PermissionsScreen(
                                permanentlyDenied = permanentlyDenied,
                                onRequest = { permissionLauncher.launch(requiredPermissions()) }
                            )
                        } else {
                            val startDestination = when (startupState) {
                                StartupState.Authenticated -> HomeRoute
                                StartupState.Unauthenticated -> WelcomeRoute
                                StartupState.Loading -> null
                            }
                            if (startDestination != null) {
                                AppNavGraph(navController, startDestination = startDestination)
                            }
                        }

                        NotificationHost()

                        // --- Offline overlay (always on top, blocks all interaction) ---
                        AnimatedVisibility(
                            visible = !isOnline,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            NoInternetOverlay()
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissionsGranted(): Boolean {
        val locationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return locationGranted && notificationGranted
    }

    private fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

@Composable
private fun NoInternetOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(36.dp),
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "No Internet Connection",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryDarkBlue,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "EmergencyNow requires an active internet connection. Please reconnect to continue.",
                    fontSize = 15.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "The app will resume automatically when you're back online.",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF),
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
