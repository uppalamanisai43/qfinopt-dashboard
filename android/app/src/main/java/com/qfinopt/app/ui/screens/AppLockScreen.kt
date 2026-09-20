package com.qfinopt.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.theme.*

@Composable
fun AppLockScreen(
    onUnlockWithPin: (String) -> Boolean,
    onUnlockWithBiometric: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun onDigitClick(digit: String) {
        if (enteredPin.length < 4) {
            isError = false
            val newPin = enteredPin + digit
            enteredPin = newPin
            if (newPin.length == 4) {
                val success = onUnlockWithPin(newPin)
                if (!success) {
                    isError = true
                    enteredPin = ""
                }
            }
        }
    }

    fun onBackspaceClick() {
        if (enteredPin.isNotEmpty()) {
            isError = false
            enteredPin = enteredPin.dropLast(1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Shield Icon & Glow
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(PrimaryBlue.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = CardDark,
                    border = BorderStroke(1.5.dp, PrimaryBlueLight)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = PrimaryBlueLight,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Title & Status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Q-FinOpt Secure Lock",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isError) "Incorrect PIN. Try again." else "Enter 4-digit PIN or use Biometrics",
                    fontSize = 12.sp,
                    color = if (isError) BearishRed else TextSecondary,
                    fontWeight = if (isError) FontWeight.Bold else FontWeight.Normal
                )
            }

            // 4 PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isError -> BearishRed
                                    isFilled -> BullishGreen
                                    else -> SurfaceDark
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Numeric Keypad
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keypad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "BIO" -> {
                                    Surface(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .clickable { onUnlockWithBiometric() },
                                        color = SurfaceDark,
                                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Biometrics",
                                                tint = BullishGreen,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                }
                                "DEL" -> {
                                    Surface(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .clickable { onBackspaceClick() },
                                        color = SurfaceDark,
                                        border = BorderStroke(1.dp, CardBorder)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Backspace",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Surface(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .clickable { onDigitClick(key) },
                                        color = CardDark,
                                        border = BorderStroke(1.dp, CardBorder)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick bypass demo hint
            Text(
                text = "Default PIN: 1234 • Tap 🛡️ for Biometric",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
