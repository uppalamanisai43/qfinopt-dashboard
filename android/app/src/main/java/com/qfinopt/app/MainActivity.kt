package com.qfinopt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qfinopt.app.config.AppConfig
import com.qfinopt.app.data.api.ApiClient
import com.qfinopt.app.ui.components.AppTopBar
import com.qfinopt.app.ui.components.ServerSettingsDialog
import com.qfinopt.app.ui.components.UserProfileDialog
import com.qfinopt.app.ui.screens.*
import androidx.compose.ui.text.font.FontWeight
import com.qfinopt.app.ui.theme.*
import com.qfinopt.app.ui.viewmodel.MainViewModel

enum class NavDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Default.Dashboard),
    PORTFOLIO("Portfolio", Icons.Default.AccountBalanceWallet),
    ANALYSIS("Analysis", Icons.Default.Analytics),
    PLANNING("Planning", Icons.Default.TrendingUp),
    EXPLORE("Explore", Icons.Default.Search),
    QUANTUM("Quantum", Icons.Default.AutoAwesome)
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session manager
        viewModel.initSession(this)

        // Initialize base URL from saved preferences (or default Wi-Fi IP)
        val savedUrl = AppConfig.getSavedBaseUrl(this)
        ApiClient.setBaseUrl(savedUrl)

        setContent {
            QFinOptTheme {
                val currentUser by viewModel.currentUser.collectAsState()
                var currentDestination by remember { mutableStateOf(NavDestination.DASHBOARD) }
                var showServerSettings by remember { mutableStateOf(false) }
                var showProfileDialog by remember { mutableStateOf(false) }
                val marketData by viewModel.marketState.collectAsState()
                val isMarketLoading by viewModel.isMarketLoading.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearError()
                    }
                }

                if (showServerSettings) {
                    ServerSettingsDialog(
                        onDismiss = { showServerSettings = false },
                        onReconnected = { viewModel.loadInitialData() }
                    )
                }

                if (showProfileDialog && currentUser != null) {
                    UserProfileDialog(
                        user = currentUser!!,
                        onDismiss = { showProfileDialog = false },
                        onLogout = {
                            showProfileDialog = false
                            viewModel.logout()
                        }
                    )
                }

                if (currentUser == null) {
                    AuthScreen(
                        viewModel = viewModel,
                        onOpenServerSettings = { showServerSettings = true }
                    )
                } else {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            AppTopBar(
                                marketIndices = marketData?.indices ?: emptyMap(),
                                sentiment = marketData?.sentiment ?: "Neutral",
                                monthReturn = marketData?.monthReturn ?: 0.0,
                                isRefreshing = isMarketLoading,
                                currentUser = currentUser,
                                onRefresh = { viewModel.refreshMarket() },
                                onOpenProfile = { showProfileDialog = true },
                                onOpenPdf = { currentDestination = NavDestination.QUANTUM },
                                onOpenSettings = { showServerSettings = true }
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = SurfaceDark,
                                tonalElevation = 8.dp
                            ) {
                                val bottomItems = listOf(
                                    NavDestination.DASHBOARD,
                                    NavDestination.PORTFOLIO,
                                    NavDestination.ANALYSIS,
                                    NavDestination.PLANNING,
                                    NavDestination.EXPLORE,
                                    NavDestination.QUANTUM
                                )

                                bottomItems.forEach { dest ->
                                    val isSelected = currentDestination == dest
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentDestination = dest },
                                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                                        label = { Text(dest.label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                }
                            }
                        },
                        containerColor = DarkNavy
                    ) { innerPadding ->
                        var analysisSubTab by remember { mutableIntStateOf(0) }
                        var planningSubTab by remember { mutableIntStateOf(0) }
                        var exploreSubTab by remember { mutableIntStateOf(0) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentDestination) {
                                NavDestination.DASHBOARD -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToPortfolio = { currentDestination = NavDestination.PORTFOLIO },
                                    onNavigateToAnalysis = {
                                        analysisSubTab = 0
                                        currentDestination = NavDestination.ANALYSIS
                                    },
                                    onNavigateToPlanning = {
                                        planningSubTab = 0
                                        currentDestination = NavDestination.PLANNING
                                    },
                                    onNavigateToExplore = {
                                        exploreSubTab = 0
                                        currentDestination = NavDestination.EXPLORE
                                    },
                                    onNavigateToQuantum = { currentDestination = NavDestination.QUANTUM },
                                    onOpenSettings = { showServerSettings = true }
                                )
                                NavDestination.PORTFOLIO -> PortfolioScreen(
                                    viewModel = viewModel,
                                    onNavigateToAnalysis = {
                                        analysisSubTab = 0
                                        currentDestination = NavDestination.ANALYSIS
                                    },
                                    onNavigateToReminders = { currentDestination = NavDestination.QUANTUM }
                                )
                                NavDestination.ANALYSIS -> Column(modifier = Modifier.fillMaxSize()) {
                                    TabRow(
                                        selectedTabIndex = analysisSubTab,
                                        containerColor = SurfaceDark,
                                        contentColor = TextPrimary
                                    ) {
                                        Tab(
                                            selected = analysisSubTab == 0,
                                            onClick = { analysisSubTab = 0 },
                                            text = { Text("📈 Fund Forecast", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            selectedContentColor = PurpleAccent,
                                            unselectedContentColor = TextSecondary
                                        )
                                        Tab(
                                            selected = analysisSubTab == 1,
                                            onClick = { analysisSubTab = 1 },
                                            text = { Text("⚔️ Compare Battle", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            selectedContentColor = CyanAccent,
                                            unselectedContentColor = TextSecondary
                                        )
                                    }
                                    if (analysisSubTab == 0) {
                                        AnalysisScreen(viewModel = viewModel)
                                    } else {
                                        CompareScreen(viewModel = viewModel)
                                    }
                                }
                                NavDestination.PLANNING -> Column(modifier = Modifier.fillMaxSize()) {
                                    TabRow(
                                        selectedTabIndex = planningSubTab,
                                        containerColor = SurfaceDark,
                                        contentColor = TextPrimary
                                    ) {
                                        Tab(
                                            selected = planningSubTab == 0,
                                            onClick = { planningSubTab = 0 },
                                            text = { Text("🎯 Withdrawal Timing", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            selectedContentColor = BullishGreen,
                                            unselectedContentColor = TextSecondary
                                        )
                                        Tab(
                                            selected = planningSubTab == 1,
                                            onClick = { planningSubTab = 1 },
                                            text = { Text("📅 SIP Compounder", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            selectedContentColor = PrimaryBlue,
                                            unselectedContentColor = TextSecondary
                                        )
                                    }
                                    if (planningSubTab == 0) {
                                        WithdrawalScreen(viewModel = viewModel)
                                    } else {
                                        SipScreen(viewModel = viewModel)
                                    }
                                }
                                NavDestination.EXPLORE -> Column(modifier = Modifier.fillMaxSize()) {
                                    TabRow(
                                        selectedTabIndex = exploreSubTab,
                                        containerColor = SurfaceDark,
                                        contentColor = TextPrimary
                                    ) {
                                        Tab(
                                            selected = exploreSubTab == 0,
                                            onClick = { exploreSubTab = 0 },
                                            text = { Text("🔍 AMFI Live Search", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            selectedContentColor = TextPrimary,
                                            unselectedContentColor = TextSecondary
                                        )
                                        Tab(
                                            selected = exploreSubTab == 1,
                                            onClick = { exploreSubTab = 1 },
                                            text = { Text("🏦 Brokers Guide", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            selectedContentColor = AccentGold,
                                            unselectedContentColor = TextSecondary
                                        )
                                    }
                                    if (exploreSubTab == 0) {
                                        NavSearchScreen(viewModel = viewModel)
                                    } else {
                                        PlatformScreen(viewModel = viewModel)
                                    }
                                }
                                NavDestination.QUANTUM -> QAOAScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
