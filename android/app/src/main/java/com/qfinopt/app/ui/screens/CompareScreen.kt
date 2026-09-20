package com.qfinopt.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
fun CompareScreen(viewModel: MainViewModel) {
    val compareResult by viewModel.compareResult.collectAsState()
    val isLoading by viewModel.isCompareLoading.collectAsState()
    val compareFundsList by viewModel.compareFundsList.collectAsState()
    val allFunds by viewModel.funds.collectAsState()
    val period by viewModel.comparePeriod.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        var query by remember { mutableStateOf("") }
        val candidates = remember(allFunds, query) {
            allFunds.filter { !compareFundsList.contains(it) && (query.isBlank() || it.contains(query, ignoreCase = true)) }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Fund to Compare", color = TextPrimary) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        candidates.take(20).forEach { f ->
                            TextButton(
                                onClick = {
                                    viewModel.compareFundsList.value = compareFundsList + f
                                    viewModel.loadComparison()
                                    showAddDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(f, fontSize = 12.sp, color = TextPrimary, maxLines = 1)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            },
            containerColor = SurfaceDark
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚖️ Compare Mutual Funds",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Selected funds chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            compareFundsList.forEach { fund ->
                InputChip(
                    selected = true,
                    onClick = { },
                    label = { Text(fund.take(20), fontSize = 11.sp) },
                    trailingIcon = {
                        if (compareFundsList.size > 1) {
                            IconButton(
                                onClick = {
                                    viewModel.compareFundsList.value = compareFundsList - fund
                                    viewModel.loadComparison()
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextPrimary)
                            }
                        }
                    }
                )
            }
            if (compareFundsList.size < 10) {
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Fund", fontSize = 11.sp)
                }
            }
        }

        // Period chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("1M", "3M", "6M", "1Y", "2Y", "3Y", "ALL").forEach { p ->
                val isSel = period == p
                FilterChip(
                    selected = isSel,
                    onClick = {
                        viewModel.comparePeriod.value = p
                        viewModel.loadComparison()
                    },
                    label = { Text(p, fontSize = 11.sp) }
                )
            }
        }

        if (isLoading) {
            LoadingView(message = "Aligning fund histories & computing returns...")
        } else if (compareResult != null) {
            val res = compareResult!!

            // Chart
            val palette = listOf(PrimaryBlue, BullishGreen, AccentGold, PurpleAccent, BearishRed, Color(0xFF00BCD4))
            val seriesList = mutableListOf<ChartLineSeries>()
            res.series.forEachIndexed { idx, s ->
                val color = palette[idx % palette.size]
                if (s.actualPct.isNotEmpty()) {
                    seriesList.add(
                        ChartLineSeries(
                            name = s.fundName.take(15),
                            points = s.actualPct,
                            color = color,
                            strokeWidth = 3.5f,
                            startIndex = 0
                        )
                    )
                }
                if (s.forecastPct.isNotEmpty() && s.actualPct.isNotEmpty()) {
                    val combined = listOf(s.actualPct.last()) + s.forecastPct
                    val startIndex = (s.actualPct.size - 1).coerceAtLeast(0)
                    seriesList.add(
                        ChartLineSeries(
                            name = "${s.fundName.take(10)} (fc)",
                            points = combined,
                            color = color,
                            isDashed = true,
                            strokeWidth = 3.5f,
                            startIndex = startIndex
                        )
                    )
                }
            }

            LineChartView(
                title = "Comparative % Return - $period",
                seriesList = seriesList,
                yUnitSuffix = "%",
                baselineValue = 0.0
            )

            // Comparison Summary Table
            Text(
                text = "📊 PERFORMANCE METRICS TABLE",
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
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Text("Fund", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(2f))
                        Text("1Y Ret", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f))
                        Text("Sharpe", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f))
                        Text("Beta", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(0.8f))
                    }
                    Divider(color = CardBorder, thickness = 1.dp)

                    res.table.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(row.fundName.take(24), fontSize = 11.sp, color = TextPrimary, modifier = Modifier.weight(2f), maxLines = 1)
                            Text(
                                "${if (row.ret1y >= 0) "+" else ""}${String.format("%.1f", row.ret1y)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (row.ret1y >= 0) BullishGreen else BearishRed,
                                modifier = Modifier.weight(1f)
                            )
                            Text(String.format("%.2f", row.sharpe), fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                            Text(String.format("%.2f", row.beta), fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(0.8f))
                        }
                    }
                }
            }
        }
    }
}
