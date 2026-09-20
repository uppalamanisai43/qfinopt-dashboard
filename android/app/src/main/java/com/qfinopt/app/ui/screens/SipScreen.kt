package com.qfinopt.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.components.*
import com.qfinopt.app.ui.theme.*
import com.qfinopt.app.ui.viewmodel.MainViewModel

@Composable
fun SipScreen(viewModel: MainViewModel) {
    val selectedFund by viewModel.selectedFund.collectAsState()
    val sipResult by viewModel.sipResult.collectAsState()
    val isLoading by viewModel.isSipLoading.collectAsState()
    val sipAmount by viewModel.sipAmount.collectAsState()
    val sipYears by viewModel.sipYears.collectAsState()
    val annualReturn by viewModel.sipAnnualReturn.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val riskLevels by viewModel.riskLevels.collectAsState()
    val selectedRiskLevels by viewModel.selectedRiskLevels.collectAsState()
    val funds by viewModel.funds.collectAsState()
    val context = LocalContext.current

    var showFilterSheet by remember { mutableStateOf(false) }

    // Auto-run if fund selected but result is null
    LaunchedEffect(selectedFund) {
        if (selectedFund.isNotBlank() && sipResult == null && !isLoading) {
            viewModel.runSipSimulation()
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
            text = "📅 SIP Wealth Calculator",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Lognormal simulation with 21-day regular monthly deposits & exact XIRR.",
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

        // Interactive Sliders Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monthly SIP slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Monthly SIP", fontSize = 13.sp, color = TextSecondary)
                    Text("₹${String.format("%,.0f", sipAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
                Slider(
                    value = sipAmount.toFloat(),
                    onValueChange = {
                        viewModel.sipAmount.value = (Math.round(it / 500f) * 500).toDouble()
                    },
                    onValueChangeFinished = { viewModel.runSipSimulation() },
                    valueRange = 500f..100000f,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryBlue,
                        activeTrackColor = PrimaryBlue,
                        inactiveTrackColor = CardBorder
                    )
                )

                // Duration slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Duration", fontSize = 13.sp, color = TextSecondary)
                    Text("$sipYears Years (${sipYears * 12} installments)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                }
                Slider(
                    value = sipYears.toFloat(),
                    onValueChange = { viewModel.sipYears.value = it.toInt() },
                    onValueChangeFinished = { viewModel.runSipSimulation() },
                    valueRange = 1f..30f,
                    steps = 29,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGold,
                        activeTrackColor = AccentGold,
                        inactiveTrackColor = CardBorder
                    )
                )

                // Annual Return Assumption slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Expected Return Rate", fontSize = 13.sp, color = TextSecondary)
                    Text("${String.format("%.1f", annualReturn)}% per year", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                }
                Slider(
                    value = annualReturn.toFloat(),
                    onValueChange = { viewModel.sipAnnualReturn.value = it.toDouble() },
                    onValueChangeFinished = { viewModel.runSipSimulation() },
                    valueRange = 4f..15f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = BullishGreen,
                        activeTrackColor = BullishGreen,
                        inactiveTrackColor = CardBorder
                    )
                )
            }
        }

        if (isLoading) {
            LoadingView(message = "Simulating SIP cashflows & calculating XIRR...")
        } else if (sipResult != null) {
            val res = sipResult!!

            // Result Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "TOTAL INVESTED",
                    value = "₹${String.format("%,.0f", res.totalInvested)}",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "EXPECTED CORPUS",
                    value = "₹${String.format("%,.0f", res.expectedSip)}",
                    subValue = "+₹${String.format("%,.0f", res.gainSip)} gain",
                    highlightColor = BullishGreen,
                    borderColor = BullishGreen.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "ESTIMATED XIRR",
                    value = "${String.format("%.1f", res.expectedXirr)}%/yr",
                    subValue = "Money-weighted return",
                    highlightColor = AccentGold,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "PROFIT PROBABILITY",
                    value = "${String.format("%.1f", res.profitProbability)}%",
                    subValue = "Median: ₹${String.format("%,.0f", res.medianSip)}",
                    highlightColor = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    val ok = viewModel.createSipReminder(selectedFund, sipAmount, 5)
                    if (ok) {
                        Toast.makeText(context, "🔔 Monthly ₹${String.format("%,.0f", sipAmount)} SIP reminder set for 5th of every month!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AddAlert, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("🔔 Set Monthly SIP Reminder (₹${String.format("%,.0f", sipAmount)} on 5th)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueLight)
            }

            // SIP Chart
            val sipChartSeries = listOf(
                ChartLineSeries("Expected Corpus", res.chartExpected, Color(0xFFFFA726), strokeWidth = 5f),
                ChartLineSeries("Invested", res.chartInvested, BearishRed, isDashed = true, strokeWidth = 3f)
            )

            LineChartView(
                title = "SIP Growth Projection over $sipYears Years",
                seriesList = sipChartSeries,
                yUnitPrefix = "₹"
            )

            // Year by Year Projection Table
            Text(
                text = "📆 YEAR-BY-YEAR PROJECTION",
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
                            .padding(bottom = 8.dp)
                    ) {
                        Text("Year", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f))
                        Text("Invested", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.2f))
                        Text("Expected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.4f))
                        Text("Profit %", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f))
                    }
                    Divider(color = CardBorder, thickness = 1.dp)

                    res.projections.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(row.year, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Text("₹${String.format("%,.0f", row.invested)}", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1.2f))
                            Text("₹${String.format("%,.0f", row.expected)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BullishGreen, modifier = Modifier.weight(1.4f))
                            Text("${String.format("%.0f", row.profitProbability)}%", fontSize = 11.sp, color = AccentGold, modifier = Modifier.weight(1f))
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
                    Text("📈", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SIP Simulation Ready",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedFund.isNotBlank()) "Tap below to simulate ₹${String.format("%,.0f", sipAmount)}/mo growth over $sipYears years for:\n$selectedFund" else "Select a fund above to calculate SIP wealth projections.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.runSipSimulation() },
                        enabled = selectedFund.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Calculate SIP Wealth", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
