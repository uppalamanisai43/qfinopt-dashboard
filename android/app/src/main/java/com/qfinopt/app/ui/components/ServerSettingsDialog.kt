package com.qfinopt.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.config.AppConfig
import com.qfinopt.app.data.api.ApiClient
import com.qfinopt.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Composable
fun ServerSettingsDialog(
    onDismiss: () -> Unit,
    onReconnected: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var serverUrl by remember { mutableStateOf(ApiClient.getBaseUrl()) }
    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf<Boolean?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚙️ Backend Server Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Choose a connection method or enter your PC's IP address:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = serverUrl == AppConfig.DEFAULT_CLOUD_URL,
                        onClick = {
                            serverUrl = AppConfig.DEFAULT_CLOUD_URL
                            testStatus = null
                            isSuccess = null
                        },
                        label = { Text("☁️ Cloud (24/7)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurpleAccent.copy(alpha = 0.3f),
                            selectedLabelColor = PurpleAccent
                        )
                    )

                    FilterChip(
                        selected = serverUrl == AppConfig.DEFAULT_WIFI_URL,
                        onClick = {
                            serverUrl = AppConfig.DEFAULT_WIFI_URL
                            testStatus = null
                            isSuccess = null
                        },
                        label = { Text("📶 Wi-Fi", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.3f),
                            selectedLabelColor = PrimaryBlue
                        )
                    )

                    FilterChip(
                        selected = serverUrl == AppConfig.DEFAULT_USB_URL,
                        onClick = {
                            serverUrl = AppConfig.DEFAULT_USB_URL
                            testStatus = null
                            isSuccess = null
                        },
                        label = { Text("🔌 USB", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BullishGreen.copy(alpha = 0.3f),
                            selectedLabelColor = BullishGreen
                        )
                    )

                    FilterChip(
                        selected = serverUrl == AppConfig.DEFAULT_EMULATOR_URL,
                        onClick = {
                            serverUrl = AppConfig.DEFAULT_EMULATOR_URL
                            testStatus = null
                            isSuccess = null
                        },
                        label = { Text("💻 AVD", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGold.copy(alpha = 0.3f),
                            selectedLabelColor = AccentGold
                        )
                    )
                }

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = {
                        serverUrl = it
                        testStatus = null
                        isSuccess = null
                    },
                    label = { Text("Server Base URL", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Test Connection Button & Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            isTesting = true
                            testStatus = "Checking..."
                            isSuccess = null
                            coroutineScope.launch {
                                val result = testConnection(serverUrl)
                                isTesting = false
                                isSuccess = result.first
                                testStatus = result.second
                            }
                        },
                        enabled = !isTesting,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("🔍 Test Connection", fontSize = 11.sp, color = TextPrimary)
                    }

                    if (testStatus != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSuccess == true) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BullishGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Online", color = BullishGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else if (isSuccess == false) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = BearishRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Offline", color = BearishRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text(testStatus!!, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (testStatus != null && isSuccess == false) {
                    Text(
                        text = testStatus!!,
                        color = BearishRed,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }

                Text(
                    text = "💡 Tip: If using USB cable, run 'adb reverse tcp:8000 tcp:8000' on PC and choose USB. If on Wi-Fi, ensure phone and PC are on the same network.",
                    fontSize = 10.sp,
                    color = TextMuted,
                    lineHeight = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanUrl = serverUrl.trim().removeSuffix("/")
                    AppConfig.saveBaseUrl(context, cleanUrl)
                    ApiClient.setBaseUrl(cleanUrl)
                    onReconnected()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save & Connect", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private suspend fun testConnection(url: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    val cleanUrl = url.trim().removeSuffix("/")
    val testClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    try {
        val request = Request.Builder()
            .url("$cleanUrl/health")
            .build()
        val response = testClient.newCall(request).execute()
        if (response.isSuccessful) {
            Pair(true, "Backend is reachable (HTTP ${response.code})")
        } else {
            Pair(false, "Server returned HTTP ${response.code}")
        }
    } catch (e: Exception) {
        Pair(false, e.localizedMessage ?: "Connection timed out")
    }
}
