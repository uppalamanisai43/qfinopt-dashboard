package com.qfinopt.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.ui.components.*
import com.qfinopt.app.ui.theme.*
import com.qfinopt.app.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val selectedFund by viewModel.selectedFund.collectAsState()
    val fundStats by viewModel.fundStats.collectAsState()
    val isStatsLoading by viewModel.isStatsLoading.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val riskLevels by viewModel.riskLevels.collectAsState()
    val selectedRiskLevels by viewModel.selectedRiskLevels.collectAsState()
    val funds by viewModel.funds.collectAsState()
    val watchlistedFunds by viewModel.watchlistedFunds.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeCategoryFilter by remember { mutableStateOf("All") }

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

    // Filter funds by search query
    val matchingFunds = remember(funds, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else funds.filter { it.contains(searchQuery, ignoreCase = true) }.take(5)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Server Connection Alert if 0 funds loaded
        if (funds.isEmpty() && !isStatsLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BearishRed.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, BearishRed.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Backend Server Offline (0 Funds Loaded)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BearishRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cannot connect to backend server at:\n${com.qfinopt.app.data.api.ApiClient.getBaseUrl()}\n\nMake sure the Python backend is running on your laptop and your phone is connected to the same Wi-Fi.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onOpenSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚙️ Server Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.loadInitialData() },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Text("🔄 Retry", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Instant Search & Category Chips Row
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SurfaceDark,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search funds (e.g. Quant, HDFC, SBI)...", color = TextMuted, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Quick Search Results Dropdown/Chips
            if (matchingFunds.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardElevated),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Search Results:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp, bottom = 4.dp))
                        matchingFunds.forEach { fund ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectFund(fund)
                                        searchQuery = ""
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(fund, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Quick Category Chips
            val quickCategories = listOf("All", "Small Cap", "Mid Cap", "Large Cap", "Flexi Cap", "Debt")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickCategories.forEach { cat ->
                    val isSelected = activeCategoryFilter == cat
                    Surface(
                        modifier = Modifier.clickable {
                            activeCategoryFilter = cat
                            viewModel.setSingleCategory(cat)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryBlue else SurfaceDark,
                        border = BorderStroke(1.dp, if (isSelected) PrimaryBlueLight else CardBorder)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Funds in selected category carousel
            if (funds.isNotEmpty() && activeCategoryFilter != "All") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "📁 ${funds.size} $activeCategoryFilter Funds (Tap to view):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        funds.forEach { fundName ->
                            val isCurrent = fundName == selectedFund
                            Surface(
                                modifier = Modifier.clickable { viewModel.selectFund(fundName) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) PrimaryBlue.copy(alpha = 0.22f) else CardElevated,
                                border = BorderStroke(1.dp, if (isCurrent) PrimaryBlue else CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCurrent) {
                                        Text("✓ ", fontSize = 11.sp, color = BullishGreen, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = fundName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) PrimaryBlueLight else TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hero Selected Fund Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showFilterSheet = true },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(CardElevated.copy(alpha = 0.6f), SurfaceDark)
                        )
                    )
                    .padding(18.dp)
            ) {
                // Fund Name & Action Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ACTIVE MUTUAL FUND",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueLight,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = BullishGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LIVE AMFI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BullishGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedFund.isNotBlank()) selectedFund else "Tap to choose a fund",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            maxLines = 2,
                            lineHeight = 22.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedFund.isNotBlank()) {
                            val isWatchlisted = watchlistedFunds.contains(selectedFund)
                            IconButton(onClick = { viewModel.toggleWatchlist(selectedFund) }) {
                                Icon(
                                    imageVector = if (isWatchlisted) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Watchlist",
                                    tint = if (isWatchlisted) AccentGold else TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.Tune, contentDescription = "Filter", tint = PrimaryBlueLight)
                        }
                    }
                }

                if (fundStats != null) {
                    val stats = fundStats!!
                    Spacer(modifier = Modifier.height(10.dp))

                    // Tags row: Category, Risk, AI Recommendation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = CardDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, CardBorder)
                        ) {
                            Text(
                                text = stats.category,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // AI Signal Badge
                        val (sigText, sigColor) = when {
                            stats.sharpeVal >= 1.5 || stats.ret1y >= 25.0 -> "ACCUMULATE" to BullishGreen
                            stats.sharpeVal >= 0.8 -> "HOLD" to AccentGold
                            else -> "REDUCE" to BearishRed
                        }
                        Surface(
                            color = sigColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, sigColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "🤖 $sigText",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = sigColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Price & Mini Trendline Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CURRENT NAV", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "₹${String.format("%.2f", stats.displayNav)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val isPos = stats.ret1y >= 0
                                Surface(
                                    color = if (isPos) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${if (isPos) "+" else ""}${String.format("%.1f", stats.ret1y)}% 1Y",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPos) BullishGreen else BearishRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Synthetic Sparkline curve based on 1Y trajectory
                        val baseNav = stats.displayNav / (1.0 + stats.ret1y / 100.0)
                        val sparkPoints = listOf(
                            baseNav,
                            baseNav * (1.0 + (stats.ret1y * 0.2) / 100.0),
                            baseNav * (1.0 + (stats.ret1y * 0.45) / 100.0),
                            baseNav * (1.0 + (stats.ret1y * 0.7) / 100.0),
                            stats.displayNav
                        )
                        MiniSparkline(points = sparkPoints, isPositive = stats.ret1y >= 0)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 52-Week Range Bar
                    val lowNav = stats.displayNav * 0.82
                    val highNav = stats.displayNav * 1.18
                    NavRangeBar(currentNav = stats.displayNav, lowNav = lowNav, highNav = highNav)
                }
            }
        }

        // Fund Key Metrics Grid
        if (isStatsLoading) {
            LoadingView(message = "Calculating AI & risk statistics...")
        } else if (fundStats != null) {
            val stats = fundStats!!
            val isNavLive = stats.navSource.contains("LIVE", ignoreCase = true)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        label = "NAV Today",
                        value = "₹${String.format("%.2f", stats.displayNav)}",
                        subValue = stats.navSource,
                        highlightColor = if (isNavLive) BullishGreen else TextPrimary,
                        borderColor = if (isNavLive) BullishGreen.copy(alpha = 0.4f) else CardBorder,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "1-Year Return",
                        value = "${if (stats.ret1y >= 0) "+" else ""}${String.format("%.2f", stats.ret1y)}%",
                        subValue = "Annual CAGR",
                        highlightColor = if (stats.ret1y >= 0) BullishGreen else BearishRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        label = "Sharpe Ratio",
                        value = String.format("%.3f", stats.sharpeVal),
                        subValue = "Risk-adjusted score",
                        highlightColor = AccentGold,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Alpha",
                        value = String.format("%.3f", stats.alphaVal),
                        subValue = "Excess alpha vs benchmark",
                        highlightColor = if (stats.alphaVal >= 0) BullishGreen else BearishRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        label = "Beta",
                        value = String.format("%.3f", stats.betaVal),
                        subValue = "Market volatility sensitivity",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Expense Ratio",
                        value = "${String.format("%.2f", stats.expense)}%",
                        subValue = "TER management fee",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Real AI Machine Learning Prediction Card
        if (fundStats != null) {
            val stats = fundStats!!
            RealAiPredictionCard(
                fundName = selectedFund,
                currentNav = stats.displayNav,
                pred21dRetPct = stats.pred21dRetPct,
                predTargetNav = stats.predTargetNav,
                winProbability = stats.winProbability,
                signal = stats.signal,
                convictionScore = stats.convictionScore,
                keyDrivers = stats.keyDrivers,
                onNavigateToAnalysis = onNavigateToAnalysis
            )
        }

        // 4 Clean Integrated Gateways (Zero Clutter)
        Text(
            text = "⚡ QUANT ADVISORY & PLANNING HUBS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        // Row 1: Portfolio Hub & AI Analysis Hub
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = onNavigateToPortfolio,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = CardDark,
                border = BorderStroke(1.dp, BullishGreen.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BullishGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💼", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("My Portfolio", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Holdings, P&L & PDF", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Surface(
                onClick = onNavigateToAnalysis,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = CardDark,
                border = BorderStroke(1.dp, PurpleAccent.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PurpleAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📈", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Analysis & Battle", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Forecast & Compare", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        // Row 2: Wealth Calculators & Explore Brokers Hub
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = onNavigateToPlanning,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = CardDark,
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎯", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Calculators", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Withdrawal & SIP", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Surface(
                onClick = onNavigateToExplore,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = CardDark,
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔍", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Explore & Brokers", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("AMFI Search & Guides", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}
