package com.qfinopt.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.data.model.HoldingItem
import com.qfinopt.app.data.model.WatchlistItem
import com.qfinopt.app.ui.components.*
import com.qfinopt.app.ui.theme.*
import com.qfinopt.app.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PortfolioScreen(
    viewModel: MainViewModel,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToReminders: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val portfolio by viewModel.portfolioState.collectAsState()
    val isPortfolioLoading by viewModel.isPortfolioLoading.collectAsState()
    val watchlist by viewModel.watchlistState.collectAsState()
    val isWatchlistLoading by viewModel.isWatchlistLoading.collectAsState()
    val availableFunds by viewModel.funds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPortfolio()
        viewModel.loadWatchlist()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "💼 My Wealth & Watchlist",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Real-time AMFI valuation & AI health score",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = {
                    if (selectedTab == 0) viewModel.loadPortfolio() else viewModel.loadWatchlist()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = PrimaryBlueLight
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Holdings (${portfolio?.holdingsCount ?: 0})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                selectedContentColor = BullishGreen,
                unselectedContentColor = TextSecondary
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Watchlist (${watchlist.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                selectedContentColor = AccentGold,
                unselectedContentColor = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // TAB 1: HOLDINGS
            if (isPortfolioLoading && portfolio == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BullishGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        PortfolioSummaryCard(
                            totalInvested = portfolio?.totalInvested ?: 0.0,
                            currentValue = portfolio?.currentValue ?: 0.0,
                            totalPnl = portfolio?.totalPnl ?: 0.0,
                            totalPnlPct = portfolio?.totalPnlPct ?: 0.0,
                            healthScore = portfolio?.healthScore ?: 100,
                            healthStatus = portfolio?.healthStatus ?: "Ready",
                            onAddHoldingClick = { showAddDialog = true },
                            onExportPdfClick = onNavigateToReminders
                        )
                    }

                    // Asset Allocation Section
                    val holdings = portfolio?.holdings ?: emptyList()
                    if (holdings.isNotEmpty()) {
                        item {
                            val totalVal = portfolio?.currentValue ?: 1.0
                            val categoryColors = listOf(PrimaryBlue, BullishGreen, PurpleAccent, AccentGold, CyanAccent)
                            val categoryGroups = holdings.groupBy {
                                val cat = it.category
                                when {
                                    cat.contains("Small", ignoreCase = true) -> "Small Cap"
                                    cat.contains("Mid", ignoreCase = true) -> "Mid Cap"
                                    cat.contains("Large", ignoreCase = true) -> "Large Cap"
                                    cat.contains("Flexi", ignoreCase = true) -> "Flexi Cap"
                                    cat.contains("Debt", ignoreCase = true) -> "Debt"
                                    else -> "Equity"
                                }
                            }

                            val segments = categoryGroups.entries.mapIndexed { idx, entry ->
                                val sumVal = entry.value.sumOf { it.currentValue }
                                val pct = if (totalVal > 0) (sumVal / totalVal).toFloat() else 0f
                                AllocationSegment(
                                    label = entry.key,
                                    percentage = pct,
                                    color = categoryColors[idx % categoryColors.size]
                                )
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Box(modifier = Modifier.padding(14.dp)) {
                                    AssetAllocationBar(segments = segments)
                                }
                            }
                        }
                    }

                    if (portfolio?.holdings.isNullOrEmpty()) {
                        item {
                            EmptyPortfolioCard(onAddClick = { showAddDialog = true })
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your Holdings",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BullishGreen),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Fund", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        items(portfolio?.holdings ?: emptyList(), key = { it.id }) { holding ->
                            HoldingCard(
                                holding = holding,
                                onDelete = { viewModel.deleteHolding(holding.id) },
                                onSelectFund = {
                                    viewModel.selectFund(holding.fundName)
                                    onNavigateToAnalysis()
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // TAB 2: WATCHLIST
            if (isWatchlistLoading && watchlist.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentGold)
                }
            } else if (watchlist.isEmpty()) {
                EmptyWatchlistCard()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(watchlist, key = { it.fundName }) { item ->
                        WatchlistCard(
                            item = item,
                            onRemove = { viewModel.toggleWatchlist(item.fundName) },
                            onSelect = {
                                viewModel.selectFund(item.fundName)
                                onNavigateToAnalysis()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHoldingDialog(
            availableFunds = availableFunds,
            onDismiss = { showAddDialog = false },
            onAdd = { fundName, amount, nav, date ->
                viewModel.addHolding(fundName, amount, nav, date)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PortfolioSummaryCard(
    totalInvested: Double,
    currentValue: Double,
    totalPnl: Double,
    totalPnlPct: Double,
    healthScore: Int,
    healthStatus: String,
    onAddHoldingClick: () -> Unit,
    onExportPdfClick: () -> Unit = {}
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val isPositive = totalPnl >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(CardElevated.copy(alpha = 0.7f), SurfaceDark)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Valuation & PnL
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL PORTFOLIO VALUE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currencyFormat.format(currentValue),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Profit / Loss Pill
                    Surface(
                        color = if (isPositive) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isPositive) BullishGreen.copy(alpha = 0.4f) else BearishRed.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isPositive) BullishGreen else BearishRed,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isPositive) "+" else ""}${currencyFormat.format(totalPnl)} (${String.format("%.2f", totalPnlPct)}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) BullishGreen else BearishRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Invested: ${currencyFormat.format(totalInvested)}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Right Column: Radial Health Gauge
                RadialHealthGauge(
                    score = healthScore,
                    size = 92.dp,
                    strokeWidth = 7.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = healthStatus,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onExportPdfClick,
                        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("📄 PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    }

                    Button(
                        onClick = onAddHoldingClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Fund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HoldingCard(
    holding: HoldingItem,
    onDelete: () -> Unit,
    onSelectFund: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val isProfit = holding.totalPnl >= 0
    val initial = holding.fundName.firstOrNull()?.uppercase() ?: "F"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectFund() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar badge
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = PrimaryBlue.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PrimaryBlueLight)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlueLight
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = holding.fundName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${holding.category} • ${holding.investDate.ifEmpty { "Recent" }}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Invested: ${currencyFormat.format(holding.investAmount)}", fontSize = 11.sp, color = TextSecondary)
                    Text("Units: ${String.format("%.2f", holding.units)} @ ₹${String.format("%.2f", holding.purchaseNav)}", fontSize = 11.sp, color = TextSecondary)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormat.format(holding.currentValue),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${if (isProfit) "+" else ""}${currencyFormat.format(holding.totalPnl)} (${String.format("%.1f", holding.totalPnlPct)}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) BullishGreen else BearishRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Signal Badge & Live NAV Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (holding.signal) {
                        "STRONG BUY", "ACCUMULATE" -> BullishGreen.copy(alpha = 0.15f)
                        "HOLD" -> AccentGold.copy(alpha = 0.15f)
                        else -> BearishRed.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        0.5.dp,
                        when (holding.signal) {
                            "STRONG BUY", "ACCUMULATE" -> BullishGreen.copy(alpha = 0.4f)
                            "HOLD" -> AccentGold.copy(alpha = 0.4f)
                            else -> BearishRed.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Text(
                        text = "🤖 ${holding.signal} (${holding.convictionScore.toInt()}%)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (holding.signal) {
                            "STRONG BUY", "ACCUMULATE" -> BullishGreen
                            "HOLD" -> AccentGold
                            else -> BearishRed
                        },
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "Live NAV: ₹${String.format("%.2f", holding.currentNav)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlueLight
                )
            }
        }
    }
}

@Composable
fun WatchlistCard(
    item: WatchlistItem,
    onRemove: () -> Unit,
    onSelect: () -> Unit
) {
    val initial = item.fundName.firstOrNull()?.uppercase() ?: "F"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = AccentGold.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initial,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fundName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.category} • NAV ₹${String.format("%.2f", item.currentNav)}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (item.signal) {
                            "STRONG BUY", "ACCUMULATE" -> BullishGreen.copy(alpha = 0.15f)
                            "HOLD" -> AccentGold.copy(alpha = 0.15f)
                            else -> BearishRed.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.signal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.signal) {
                                "STRONG BUY", "ACCUMULATE" -> BullishGreen
                                "HOLD" -> AccentGold
                                else -> BearishRed
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "1Y: ${if (item.ret1y >= 0) "+" else ""}${String.format("%.1f", item.ret1y)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.ret1y >= 0) BullishGreen else BearishRed
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Remove from Watchlist",
                    tint = AccentGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyPortfolioCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = BullishGreen,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("No Mutual Funds Added Yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Track your actual mutual fund investments with real-time AMFI NAV valuation and AI health analytics.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = BullishGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Your First Investment", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyWatchlistCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Your Watchlist is Empty", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tap the star (⭐) icon on any mutual fund card in the Dashboard to pin it here for quick monitoring.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHoldingDialog(
    availableFunds: List<String>,
    onDismiss: () -> Unit,
    onAdd: (fundName: String, amount: Double, nav: Double?, date: String) -> Unit
) {
    var selectedFund by remember { mutableStateOf(availableFunds.firstOrNull() ?: "") }
    var investAmountStr by remember { mutableStateOf("50000") }
    var purchaseNavStr by remember { mutableStateOf("") }
    var investDateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text("➕ Add Mutual Fund Holding", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Mutual Fund:", fontSize = 12.sp, color = TextSecondary)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFund,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BullishGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableFunds.take(40).forEach { fund ->
                            DropdownMenuItem(
                                text = { Text(fund, fontSize = 12.sp) },
                                onClick = {
                                    selectedFund = fund
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = investAmountStr,
                    onValueChange = { investAmountStr = it },
                    label = { Text("Total Invested Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BullishGreen,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = purchaseNavStr,
                    onValueChange = { purchaseNavStr = it },
                    label = { Text("Purchase NAV (Leave blank for current NAV)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BullishGreen,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = investDateStr,
                    onValueChange = { investDateStr = it },
                    label = { Text("Investment Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BullishGreen,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = investAmountStr.toDoubleOrNull() ?: 50000.0
                    val nav = purchaseNavStr.toDoubleOrNull()
                    if (selectedFund.isNotBlank()) {
                        onAdd(selectedFund, amount, nav, investDateStr)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BullishGreen)
            ) {
                Text("Save Holding", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
