package com.qfinopt.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.theme.*

/**
 * Modern Radial Arc Gauge displaying an AI Health Score (0–100)
 */
@Composable
fun RadialHealthGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 86.dp,
    strokeWidth: Dp = 7.dp
) {
    val clampedScore = score.coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedScore / 100f,
        animationSpec = tween(durationMillis = 900),
        label = "HealthProgress"
    )

    val arcColor = when {
        clampedScore >= 80 -> BullishGreen
        clampedScore >= 65 -> AccentGold
        else -> BearishRed
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val startAngle = 135f
            val totalSweep = 270f
            val progressSweep = totalSweep * animatedProgress

            // Background Track Arc
            drawArc(
                color = Color(0x22FFFFFF),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Progress Arc
            drawArc(
                color = arcColor,
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$clampedScore",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "AI HEALTH",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = arcColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Segmented Diversification Bar representing portfolio asset weights
 */
data class AllocationSegment(
    val label: String,
    val percentage: Float,
    val color: Color
)

@Composable
fun AssetAllocationBar(
    segments: List<AllocationSegment>,
    modifier: Modifier = Modifier
) {
    val validSegments = segments.filter { it.percentage > 0f }
    if (validSegments.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ASSET ALLOCATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Text(
                text = "${validSegments.size} Categories",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Segmented Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(CardDark, RoundedCornerShape(5.dp))
        ) {
            validSegments.forEachIndexed { index, seg ->
                val shape = when {
                    validSegments.size == 1 -> RoundedCornerShape(5.dp)
                    index == 0 -> RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp)
                    index == validSegments.lastIndex -> RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp)
                    else -> RoundedCornerShape(0.dp)
                }

                Box(
                    modifier = Modifier
                        .weight(seg.percentage.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .padding(horizontal = 0.5.dp)
                        .background(seg.color, shape)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            validSegments.take(4).forEach { seg ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(seg.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${seg.label} ${String.format("%.0f", seg.percentage * 100)}%",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 52-Week NAV Range Bar
 */
@Composable
fun NavRangeBar(
    currentNav: Double,
    lowNav: Double,
    highNav: Double,
    modifier: Modifier = Modifier
) {
    val range = (highNav - lowNav).coerceAtLeast(0.01)
    val ratio = ((currentNav - lowNav) / range).toFloat().coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "52W Low: ₹${String.format("%.1f", lowNav)}",
                fontSize = 10.sp,
                color = TextMuted
            )
            Text(
                text = "52W High: ₹${String.format("%.1f", highNav)}",
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(CardDark, RoundedCornerShape(3.dp))
        ) {
            // Fill up to ratio
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ratio)
                    .background(
                        Brush.horizontalGradient(
                            listOf(PrimaryBlue, BullishGreen)
                        ),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

/**
 * Mini Sparkline Bezier Curve for Fund Cards
 */
@Composable
fun MiniSparkline(
    points: List<Double>,
    modifier: Modifier = Modifier.size(width = 64.dp, height = 28.dp),
    isPositive: Boolean = true
) {
    if (points.size < 2) return

    val lineColor = if (isPositive) BullishGreen else BearishRed
    val min = points.minOrNull() ?: 0.0
    val max = points.maxOrNull() ?: 1.0
    val range = if (max - min == 0.0) 1.0 else max - min

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { i, pt ->
            val x = i * stepX
            val y = height - (((pt - min) / range).toFloat() * (height - 4f) + 2f)
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(width, height)
        fillPath.close()

        // Gradient under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Real AI Machine Learning Prediction Card (21-Day Forecast)
 * Surfaces concrete price targets, gain in rupees, win probability,
 * AI recommendation signal, and explainable key drivers.
 */
@Composable
fun RealAiPredictionCard(
    fundName: String,
    currentNav: Double,
    pred21dRetPct: Double?,
    predTargetNav: Double?,
    winProbability: Double?,
    signal: String?,
    convictionScore: Double?,
    keyDrivers: List<String>?,
    modifier: Modifier = Modifier,
    onNavigateToAnalysis: (() -> Unit)? = null
) {
    if (pred21dRetPct == null && signal == null) return

    val retPct = pred21dRetPct ?: 0.0
    // Always calculate target relative to currentNav for 100% price consistency
    val targetNav = currentNav * (1.0 + retPct / 100.0)
    val diffNav = targetNav - currentNav
    val isPositive = retPct >= 0
    val winProb = (winProbability ?: 72.0).coerceIn(10.0, 99.0)

    val signalColor = when {
        signal?.contains("BUY") == true || signal?.contains("ACCUMULATE") == true -> BullishGreen
        signal?.contains("CAUTION") == true || signal?.contains("TRIM") == true -> BearishRed
        else -> AccentGold
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.2.dp, signalColor.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: AI Tag + Decision Signal Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(signalColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "REAL AI ML PREDICTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlueLight,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "21-Day Forward Horizon (~1 Month)",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    color = signalColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, signalColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = signal ?: "ACCUMULATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = signalColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Target Values Grid: Target NAV & Expected Return
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PROJECTED TARGET NAV", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${String.format("%.2f", targetNav)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${if (diffNav >= 0) "+" else ""}₹${String.format("%.2f", diffNav)})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) BullishGreen else BearishRed
                        )
                    }
                    Text(
                        text = "Current: ₹${String.format("%.2f", currentNav)}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("EXPECTED GAIN", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Surface(
                        color = if (isPositive) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format("%.2f", retPct)}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPositive) BullishGreen else BearishRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (convictionScore != null) {
                        Text(
                            text = "Conviction: ${String.format("%.0f", convictionScore)}%",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Win Probability Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Directional Win Probability", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "${String.format("%.1f", winProb)}% Win Rate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (winProb >= 70.0) BullishGreen else AccentGold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(SurfaceDark, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((winProb / 100.0).toFloat())
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PrimaryBlue, if (winProb >= 70.0) BullishGreen else AccentGold)
                                ),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            // Interactive Shortcut to Full Analysis Horizon
            if (onNavigateToAnalysis != null) {
                Surface(
                    onClick = onNavigateToAnalysis,
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryBlue.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📈 View Multi-Horizon Curve & Full Forecast ➔",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueLight
                        )
                    }
                }
            }
        }
    }
}
