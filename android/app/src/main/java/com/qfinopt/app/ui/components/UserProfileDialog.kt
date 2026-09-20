package com.qfinopt.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.qfinopt.app.data.model.UserResponse
import com.qfinopt.app.ui.theme.*

@Composable
fun UserProfileDialog(
    user: UserResponse,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Investor Profile",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar with Initials
                val initials = user.name.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                    .ifBlank { "U" }

                Surface(
                    modifier = Modifier.size(68.dp),
                    shape = CircleShape,
                    color = PrimaryBlue.copy(alpha = 0.2f),
                    border = BorderStroke(2.dp, PrimaryBlue)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = user.email,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = AccentGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = AccentGold, modifier = Modifier.size(12.dp))
                            Text(
                                text = "${user.riskProfile} Risk",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        }
                    }

                    if (user.isGuest) {
                        Surface(
                            color = BearishRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BearishRed.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Guest Session",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BearishRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = BullishGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BullishGreen.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "✓ Verified Member",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BullishGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SEBI Compliance & Legal Disclaimers Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚖️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SEBI COMPLIANCE & LEGAL NOTICE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = "• Educational & Quantitative Research Tool: Q-FinOpt provides algorithmic analytics, Monte Carlo simulations, and AMFI statistical indicators. It does NOT provide SEBI-registered discretionary portfolio management or binding personal investment advice.",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        Text(
                            text = "• 100% Direct Plans (Zero Commission): Q-FinOpt does not hold investor funds, execute financial transactions, or earn distributor commissions, fully adhering to SEBI Direct Plan regulations.",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        Surface(
                            color = AccentGold.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "⚠️ Statutory SEBI Warning: Mutual fund investments are subject to market risks. Read all scheme-related documents carefully before investing.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentGold,
                                modifier = Modifier.padding(8.dp),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Log Out Button
                Button(
                    onClick = {
                        onDismiss()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BearishRed.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, BearishRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log Out",
                        tint = BearishRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out of Account",
                        color = BearishRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
