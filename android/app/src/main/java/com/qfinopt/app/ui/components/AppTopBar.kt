package com.qfinopt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import com.qfinopt.app.data.model.MarketIndex
import com.qfinopt.app.data.model.UserResponse
import com.qfinopt.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    marketIndices: Map<String, MarketIndex>,
    sentiment: String,
    monthReturn: Double,
    isRefreshing: Boolean,
    currentUser: UserResponse? = null,
    onRefresh: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onOpenPdf: () -> Unit = {},
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "📈 Q-FinOpt",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        SentimentBadge(sentiment = sentiment)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mutual Fund & Exit Advisor",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = PrimaryBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = PrimaryBlue
                        )
                    }
                }
                IconButton(onClick = onOpenPdf) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF Reports & Reminders",
                        tint = AccentGold
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Server Settings",
                        tint = TextSecondary
                    )
                }
                if (currentUser != null) {
                    val initial = currentUser.name.trim().firstOrNull()?.uppercase() ?: "U"
                    Surface(
                        modifier = Modifier
                            .padding(start = 2.dp, end = 6.dp)
                            .size(32.dp)
                            .clickable { onOpenProfile() },
                        shape = CircleShape,
                        color = PrimaryBlue.copy(alpha = 0.2f),
                        border = BorderStroke(1.5.dp, PrimaryBlue)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initial,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
        )

        // Live Market Indices Ticker
        if (marketIndices.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                marketIndices.values.forEach { idx ->
                    MarketTickerChip(index = idx)
                }
            }
        }
    }
}

@Composable
fun SentimentBadge(sentiment: String) {
    val (bgColor, textColor, icon) = when (sentiment.lowercase()) {
        "bullish" -> Triple(BullishGreen.copy(alpha = 0.2f), BullishGreen, "🟢")
        "bearish" -> Triple(BearishRed.copy(alpha = 0.2f), BearishRed, "🔴")
        else -> Triple(NeutralYellow.copy(alpha = 0.2f), NeutralYellow, "🟡")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Text(icon, fontSize = 9.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = sentiment.uppercase(),
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MarketTickerChip(index: MarketIndex) {
    val isPositive = index.pct >= 0
    val color = if (isPositive) BullishGreen else BearishRed
    val arrow = if (isPositive) "▲" else "▼"

    Surface(
        color = CardDark,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = index.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = String.format("%,.1f", index.price),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$arrow ${String.format("%.2f", Math.abs(index.pct))}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}
