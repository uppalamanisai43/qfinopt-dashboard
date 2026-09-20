package com.qfinopt.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.theme.*

data class ChartLineSeries(
    val name: String,
    val points: List<Double>,
    val color: Color,
    val isDashed: Boolean = false,
    val strokeWidth: Float = 5f,
    val startIndex: Int = 0
)

@Composable
fun LineChartView(
    title: String,
    seriesList: List<ChartLineSeries>,
    modifier: Modifier = Modifier,
    optimalIndex: Int? = null,
    optimalLabel: String? = null,
    baselineValue: Double? = null,
    yUnitPrefix: String = "",
    yUnitSuffix: String = ""
) {
    val allValues = seriesList.flatMap { it.points }.filter { !it.isNaN() }
    if (allValues.isEmpty()) {
        Box(
            modifier = modifier
                .background(CardDark, RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No chart data available", color = TextSecondary, fontSize = 12.sp)
        }
        return
    }

    val minY = allValues.minOrNull() ?: 0.0
    val maxY = allValues.maxOrNull() ?: 100.0
    val yRange = if (maxY - minY == 0.0) 1.0 else maxY - minY

    Column(
        modifier = modifier
            .background(CardDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                seriesList.forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(s.color, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = s.name,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw 3 horizontal grid lines
                for (i in 0..2) {
                    val y = height * (i / 2f)
                    drawLine(
                        color = CardBorder,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                // Baseline if provided
                if (baselineValue != null && baselineValue in minY..maxY) {
                    val normBaseY = ((baselineValue - minY) / yRange).toFloat()
                    val baseY = height - (normBaseY * height)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(0f, baseY),
                        end = Offset(width, baseY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                // Total points across all series taking startIndex into account
                val maxLen = seriesList.maxOfOrNull { it.startIndex + it.points.size } ?: 1
                val totalSpan = (maxLen - 1).coerceAtLeast(1)

                // Draw forecast separator if a forward series exists
                val forwardSeries = seriesList.firstOrNull { it.isDashed && it.startIndex > 0 }
                if (forwardSeries != null) {
                    val sepX = width * (forwardSeries.startIndex.toFloat() / totalSpan)
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(sepX, 0f),
                        end = Offset(sepX, height),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }

                // Draw each series
                seriesList.forEach { series ->
                    val pts = series.points
                    if (pts.size >= 2) {
                        val path = Path()
                        val fillPath = Path()

                        for (i in pts.indices) {
                            val globalIdx = series.startIndex + i
                            val x = width * (globalIdx.toFloat() / totalSpan)
                            val normY = ((pts[i] - minY) / yRange).toFloat()
                            val y = height - (normY * height)

                            if (i == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, height)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                            if (i == pts.size - 1) {
                                fillPath.lineTo(x, height)
                                fillPath.close()
                            }
                        }

                        // Gradient fill for the primary series
                        if (!series.isDashed) {
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(series.color.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                        }

                        drawPath(
                            path = path,
                            color = series.color,
                            style = Stroke(
                                width = series.strokeWidth,
                                pathEffect = if (series.isDashed) PathEffect.dashPathEffect(floatArrayOf(12f, 8f)) else null
                            )
                        )
                    }
                }

                // Highlight optimal index
                if (optimalIndex != null) {
                    val maxLen = seriesList.maxOfOrNull { it.points.size } ?: 1
                    if (optimalIndex in 0 until maxLen && maxLen > 1) {
                        val optX = width * (optimalIndex.toFloat() / (maxLen - 1))
                        drawLine(
                            color = AccentGold,
                            start = Offset(optX, 0f),
                            end = Offset(optX, height),
                            strokeWidth = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                        )
                        drawCircle(
                            color = AccentGold,
                            radius = 6f,
                            center = Offset(optX, height * 0.3f)
                        )
                    }
                }
            }

            // Min/Max Labels
            Text(
                text = "$yUnitPrefix${String.format("%,.0f", maxY)}$yUnitSuffix",
                fontSize = 9.sp,
                color = TextMuted,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                text = "$yUnitPrefix${String.format("%,.0f", minY)}$yUnitSuffix",
                fontSize = 9.sp,
                color = TextMuted,
                modifier = Modifier.align(Alignment.BottomStart)
            )

            if (optimalLabel != null) {
                Text(
                    text = optimalLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
