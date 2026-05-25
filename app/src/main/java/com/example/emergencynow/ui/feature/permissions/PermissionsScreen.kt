package com.example.emergencynow.ui.feature.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.components.decorations.BackgroundVariant
import com.example.emergencynow.ui.components.decorations.DecorativeBackground
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.BrandBlueMid
import com.example.emergencynow.ui.theme.CurvePaleBlue
import com.example.emergencynow.ui.theme.PrimaryDarkBlue

@Composable
fun PermissionsScreen(
    permanentlyDenied: Boolean,
    onRequest: () -> Unit,
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        DecorativeBackground(BackgroundVariant.CHOOSE_VERIFICATION, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                text = "Permissions\nRequired",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryDarkBlue,
                lineHeight = 44.sp,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(BrandBlueMid)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "EmergencyNow needs the following permissions to function. Without them the app cannot operate.",
                fontSize = 16.sp,
                color = Color(0xFF4B5563),
                lineHeight = 26.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            PermissionItem(
                icon = Icons.Default.LocationOn,
                title = "Location",
                description = "Required to show your position on the map and dispatch help to you."
            )

            Spacer(Modifier.height(16.dp))

            PermissionItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "Required to receive emergency alerts and driver updates in real time."
            )

            Spacer(Modifier.height(40.dp))

            if (permanentlyDenied) {
                Text(
                    text = "You've permanently denied one or more permissions. Please enable them in Settings.",
                    fontSize = 14.sp,
                    color = Color(0xFFDC2626),
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDarkBlue,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = "Open Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDarkBlue,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = "Grant Permissions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PermissionItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CurvePaleBlue)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BrandBlueDark),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryDarkBlue)
            Text(text = description, fontSize = 13.sp, color = Color(0xFF4B5563), lineHeight = 20.sp)
        }
    }
}
