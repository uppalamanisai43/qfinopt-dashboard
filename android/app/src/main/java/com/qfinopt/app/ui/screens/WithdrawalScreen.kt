package com.qfinopt.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.components.*
import com.qfinopt.app.ui.theme.*
import com.qfinopt.app.ui.viewmodel.MainViewModel

@Composable
fun WithdrawalScreen(viewModel: MainViewModel) {
    val selectedFund by viewModel.selectedFund.collectAsState()
    val withdrawalResult by viewModel.withdrawalResult.collectAsState()
    val isLoading by viewModel.isWithdrawalLoading.collectAsState()
    val investmentAmount by viewModel.investmentAmount.collectAsState()
    val simCount by viewModel.withdrawalSimCount.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val riskLevels by viewModel.riskLevels.collectAsState()
    val selectedRiskLevels by viewModel.selectedRiskLevels.collectAsState()
    val funds by viewModel.funds.collectAsState()
    val context = LocalContext.current

    var showFilterSheet by remember { mutableStateOf(false) }

    var investInputText by remember(investmentAmount) {
        mutableStateOf(investmentAmount.toLong().toString())
    }

    // Auto-run if fund selected but result is null
    LaunchedEffect(selectedFund) {
        if (selectedFund.isNotBlank() && withdrawalResult == null && !isLoading) {
            viewModel.runWithdrawalSimulation()
        }
    }

    if (showFilterSheet) {
        FundFilterSheet(
            categories = categories,
            selectedCategories = selectedCategories,
            riskLevels = riskLevels,
            selectedRiskLevels = selectedRiskLevels,
            funds = funds,
            selectedFund = selectedFund,
            onCategoryToggle = { viewModel.toggleCategory(it) },
            onRiskLevelToggle = { viewModel.toggleRiskLevel(it) },
            onSelectAllCategories = { viewModel.selectAllCategories() },
            onClearCategories = { viewModel.clearCategories() },
            onSelectAllRiskLevels = { viewModel.selectAllRiskLevels() },
            onFundSelect = { viewModel.selectFund(it) },
            onDismiss = { showFilterSheet = false }
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
            text = "🎯 Optimal Withdrawal Timing",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Monte Carlo optimization calculates the exact day where risk-adjusted return peaks.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        // Fund Selection Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showFilterSheet = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SELECTED MUTUAL FUND",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedFund.ifBlank { "No Fund Selected - Tap to Select" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                        Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }
            }
        }

        // Investment Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "LUMP SUM INVESTMENT (₹)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                OutlinedTextField(
                    value = investInputText,
                    onValueChange = {
                        investInputText = it
                        val parsed = it.toDoubleOrNull()
                        if (parsed != null && parsed >= 500) {
                            viewModel.investmentAmount.value = parsed
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Quick Simulation count buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Simulations:", fontSize = 12.sp, color = TextSecondary)
                    listOf(1000, 5000, 10000).forEach { count ->
                        val isSel = simCount == count
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                viewModel.withdrawalSimCount.value = count
                                viewModel.runWithdrawalSimulation()
                            },
                            label = { Text("${count / 1000}k", fontSize = 11.sp) }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.runWithdrawalSimulation() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Calculate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isLoading) {
            LoadingView(message = "Simulating ${simCount} market paths...")
        } else if (withdrawalResult != null) {
            val res = withdrawalResult!!

            // Personalized Recommendation Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(2.dp, BullishGreen)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 Recommended Action",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BullishGreen
                        )
                        Surface(
                            color = BullishGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${res.profitProb}% Profit Chance",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BullishGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "INVEST ON",
                            value = res.investDate,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "WITHDRAW ON",
                            value = res.withdrawDate,
                            subValue = "Day ${res.optDay} (~${res.optMonths} mos)",
                            highlightColor = AccentGold,
                            borderColor = AccentGold.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "EXPECTED CORPUS",
                            value = "₹${String.format("%,.0f", res.optVal)}",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "EXPECTED GAIN",
                            value = "+₹${String.format("%,.0f", res.gain)}",
                            subValue = "+${String.format("%.1f", res.retPct)}% return",
                            highlightColor = BullishGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val ok = viewModel.createExitReminderFromMl()
                            if (ok) {
                                Toast.makeText(context, "🔔 Exit reminder scheduled for ${res.withdrawDate}!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BullishGreen.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, BullishGreen.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddAlert, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🔔 Schedule Exit Reminder (${res.withdrawDate})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                    }
                }
            }

            // Monte Carlo Paths Chart
            val chartSeries = listOf(
                ChartLineSeries("Expected", res.chartExpected, Color(0xFFFFA726), strokeWidth = 5f),
                ChartLineSeries("Best 95%", res.chartP95, BullishGreen, isDashed = true, strokeWidth = 3f),
                ChartLineSeries("Worst 5%", res.chartP5, BearishRed, isDashed = true, strokeWidth = 3f)
            )

            val optIdx = res.chartDays.indexOfFirst { it >= res.optDay }.coerceAtLeast(0)

            LineChartView(
                title = "Monte Carlo Simulation (₹)",
                seriesList = chartSeries,
                optimalIndex = optIdx,
                optimalLabel = "Exit: Day ${res.optDay}",
                baselineValue = res.investment,
                yUnitPrefix = "₹"
            )

            // Holding Period Returns
            Text(
                text = "📊 RETURNS BY HOLDING PERIOD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                res.holdingPeriodReturns.forEach { hp ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(hp.label, fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "₹${String.format("%,.0f", hp.expectedValue)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "+${String.format("%.1f", hp.returnPct)}%",
                                fontSize = 10.sp,
                                color = BullishGreen
                            )
                            Text(
                                "${String.format("%.0f", hp.profitProb)}% prob",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Scenario Breakdown at Optimal Day
            Text(
                text = "🎯 SCENARIO BREAKDOWN AT OPTIMAL DAY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                res.scenarioBreakdown.forEach { sc ->
                    val isPositive = sc.returnPct >= 0
                    val col = if (isPositive) BullishGreen else BearishRed
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(sc.label, fontSize = 9.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "₹${String.format("%,.0f", sc.value)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "${if (isPositive) "+" else ""}${String.format("%.1f", sc.returnPct)}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = col
                            )
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎯", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Simulation Ready",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedFund.isNotBlank()) "Tap below to run Monte Carlo optimization for:\n$selectedFund" else "Select a fund above to calculate optimal withdrawal timing.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.runWithdrawalSimulation() },
                        enabled = selectedFund.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Calculate Timing Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
