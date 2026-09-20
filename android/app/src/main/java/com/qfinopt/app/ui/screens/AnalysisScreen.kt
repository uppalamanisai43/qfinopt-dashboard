package com.qfinopt.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.components.*
import com.qfinopt.app.ui.theme.*
import com.qfinopt.app.ui.viewmodel.MainViewModel

@Composable
fun AnalysisScreen(viewModel: MainViewModel) {
    val selectedFund by viewModel.selectedFund.collectAsState()
    val fundStats by viewModel.fundStats.collectAsState()
    val historyResult by viewModel.fundHistory.collectAsState()
    val isLoading by viewModel.isHistoryLoading.collectAsState()
    val period by viewModel.analysisPeriod.collectAsState()
    val showForecast by viewModel.analysisShowForecast.collectAsState()
    val forecastDays by viewModel.analysisForecastDays.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📈 NAV History & Forecast",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = selectedFund,
            fontSize = 13.sp,
            color = PrimaryBlue,
            fontWeight = FontWeight.SemiBold
        )

        // Period Selection Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("1M", "3M", "6M", "1Y", "2Y", "3Y", "5Y", "ALL").forEach { p ->
                val isSel = period == p
                FilterChip(
                    selected = isSel,
                    onClick = {
                        viewModel.analysisPeriod.value = p
                        viewModel.loadFundHistory()
                    },
                    label = { Text(p, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Forecast Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show Predicted Trend", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = showForecast,
                        onCheckedChange = {
                            viewModel.analysisShowForecast.value = it
                            viewModel.loadFundHistory()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentGold, checkedTrackColor = AccentGold.copy(alpha = 0.5f))
                    )
                }

                if (showForecast) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Forecast Window", fontSize = 12.sp, color = TextSecondary)
                        Text("+${forecastDays} Trading Days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    }
                    Slider(
                        value = forecastDays.toFloat(),
                        onValueChange = { viewModel.analysisForecastDays.value = it.toInt() },
                        onValueChangeFinished = { viewModel.loadFundHistory() },
                        valueRange = 5f..90f,
                        steps = 16
                    )
                }
            }
        }

        if (isLoading) {
            LoadingView(message = "Fetching daily NAV history and calculating trend...")
        } else if (historyResult != null) {
            val h = historyResult!!

            // Headline metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "CURRENT % CHANGE",
                    value = "${if (h.currentPct >= 0) "+" else ""}${String.format("%.2f", h.currentPct)}%",
                    highlightColor = if (h.currentPct >= 0) BullishGreen else BearishRed,
                    modifier = Modifier.weight(1f)
                )

                if (h.predictedPct != null) {
                    val delta = h.predictedPct - h.currentPct
                    MetricCard(
                        label = "PREDICTED (+${forecastDays}d)",
                        value = "${if (h.predictedPct >= 0) "+" else ""}${String.format("%.2f", h.predictedPct)}%",
                        subValue = if (h.predictedNav != null) {
                            "NAV: ₹${String.format("%.2f", h.predictedNav)} (${if (delta >= 0) "+" else ""}${String.format("%.2f", delta)}%)"
                        } else null,
                        highlightColor = AccentGold,
                        borderColor = AccentGold.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Real AI Machine Learning Prediction Card
            val currentNav = fundStats?.displayNav ?: (h.predictedNav ?: 100.0)
            RealAiPredictionCard(
                fundName = selectedFund,
                currentNav = currentNav,
                pred21dRetPct = h.pred21dRetPct,
                predTargetNav = h.predTargetNav,
                winProbability = h.winProbability,
                signal = h.signal,
                convictionScore = h.convictionScore,
                keyDrivers = h.keyDrivers
            )

            // Trend Banner
            Surface(
                color = when {
                    h.trendLabel.contains("UP") || h.trendLabel.contains("Bullish") || h.trendLabel.contains("Growth") -> BullishGreen.copy(alpha = 0.15f)
                    h.trendLabel.contains("DOWN") || h.trendLabel.contains("Bearish") || h.trendLabel.contains("Pullback") -> BearishRed.copy(alpha = 0.15f)
                    else -> NeutralYellow.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(h.trendIcon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${h.trendLabel}: ${h.sourceLabel}",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }

            // AI Signal & Conviction Rating Badge
            if (!h.signal.isNullOrBlank()) {
                val badgeColor = when {
                    h.signal.contains("BUY") || h.signal.contains("ACCUMULATE") -> BullishGreen
                    h.signal.contains("CAUTION") || h.signal.contains("TRIM") -> BearishRed
                    else -> NeutralYellow
                }
                Surface(
                    color = badgeColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI SIGNAL: ${h.signal}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                        if (h.convictionScore != null) {
                            Text(
                                text = "Conviction: ${h.convictionScore}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // History Canvas Line Chart
            val series = mutableListOf<ChartLineSeries>()
            if (h.actualPct.isNotEmpty()) {
                series.add(
                    ChartLineSeries(
                        name = "Actual %",
                        points = h.actualPct,
                        color = PrimaryBlue,
                        strokeWidth = 4f,
                        startIndex = 0
                    )
                )
            }
            if (showForecast && h.forecastPct.isNotEmpty() && h.actualPct.isNotEmpty()) {
                val startIndex = (h.actualPct.size - 1).coerceAtLeast(0)
                val lastActual = h.actualPct.last()

                // Upper Bull Case Corridor (95% CI)
                if (!h.forecastUpperPct.isNullOrEmpty()) {
                    val combinedUpper = listOf(lastActual) + h.forecastUpperPct
                    series.add(
                        ChartLineSeries(
                            name = "Bull (95%)",
                            points = combinedUpper,
                            color = BullishGreen.copy(alpha = 0.6f),
                            isDashed = true,
                            strokeWidth = 2.5f,
                            startIndex = startIndex
                        )
                    )
                }

                // AI Expected Forecast
                val combined = listOf(lastActual) + h.forecastPct
                series.add(
                    ChartLineSeries(
                        name = "AI Forecast",
                        points = combined,
                        color = AccentGold,
                        isDashed = true,
                        strokeWidth = 4f,
                        startIndex = startIndex
                    )
                )

                // Lower Bear Case Corridor (5% CI)
                if (!h.forecastLowerPct.isNullOrEmpty()) {
                    val combinedLower = listOf(lastActual) + h.forecastLowerPct
                    series.add(
                        ChartLineSeries(
                            name = "Bear (5%)",
                            points = combinedLower,
                            color = BearishRed.copy(alpha = 0.6f),
                            isDashed = true,
                            strokeWidth = 2.5f,
                            startIndex = startIndex
                        )
                    )
                }
            }

            LineChartView(
                title = "% Change - $period",
                seriesList = series,
                yUnitSuffix = "%",
                baselineValue = 0.0
            )

            // Comprehensive Fund Statistics Table
            Text(
                text = "📊 FUND STATISTICAL METRICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    h.stats.forEachIndexed { index, statMap ->
                        val metric = statMap["metric"] ?: ""
                        val value = statMap["value"] ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(metric, fontSize = 12.sp, color = TextSecondary)
                            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        if (index < h.stats.size - 1) {
                            Divider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}
