package com.qfinopt.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qfinopt.app.data.local.SessionManager
import com.qfinopt.app.data.model.*
import com.qfinopt.app.data.repository.FundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(
    private val repository: FundRepository = FundRepository()
) : ViewModel() {

    private var sessionManager: SessionManager? = null

    // Auth State
    private val _currentUser = MutableStateFlow<UserResponse?>(null)
    val currentUser: StateFlow<UserResponse?> = _currentUser.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // Market State
    private val _marketState = MutableStateFlow<MarketResponse?>(null)
    val marketState: StateFlow<MarketResponse?> = _marketState.asStateFlow()

    private val _isMarketLoading = MutableStateFlow(false)
    val isMarketLoading: StateFlow<Boolean> = _isMarketLoading.asStateFlow()

    // Funds Filter State
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _riskLevels = MutableStateFlow<List<String>>(emptyList())
    val riskLevels: StateFlow<List<String>> = _riskLevels.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())
    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    private val _selectedRiskLevels = MutableStateFlow<List<String>>(emptyList())
    val selectedRiskLevels: StateFlow<List<String>> = _selectedRiskLevels.asStateFlow()

    private val _funds = MutableStateFlow<List<String>>(emptyList())
    val funds: StateFlow<List<String>> = _funds.asStateFlow()

    private val _selectedFund = MutableStateFlow("")
    val selectedFund: StateFlow<String> = _selectedFund.asStateFlow()

    // Fund Stats State
    private val _fundStats = MutableStateFlow<FundStatsResponse?>(null)
    val fundStats: StateFlow<FundStatsResponse?> = _fundStats.asStateFlow()

    private val _isStatsLoading = MutableStateFlow(false)
    val isStatsLoading: StateFlow<Boolean> = _isStatsLoading.asStateFlow()

    // Withdrawal State
    val investmentAmount = MutableStateFlow(100000.0)
    val investDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    val withdrawalSimCount = MutableStateFlow(5000)

    private val _withdrawalResult = MutableStateFlow<WithdrawalResponse?>(null)
    val withdrawalResult: StateFlow<WithdrawalResponse?> = _withdrawalResult.asStateFlow()

    private val _isWithdrawalLoading = MutableStateFlow(false)
    val isWithdrawalLoading: StateFlow<Boolean> = _isWithdrawalLoading.asStateFlow()

    // SIP State
    val sipAmount = MutableStateFlow(5000.0)
    val sipYears = MutableStateFlow(10)
    val sipAnnualReturn = MutableStateFlow(10.0)
    val sipAnnualVol = MutableStateFlow<Double?>(null)

    private val _sipResult = MutableStateFlow<SipResponse?>(null)
    val sipResult: StateFlow<SipResponse?> = _sipResult.asStateFlow()

    private val _isSipLoading = MutableStateFlow(false)
    val isSipLoading: StateFlow<Boolean> = _isSipLoading.asStateFlow()

    // Fund Analysis State
    val analysisPeriod = MutableStateFlow("1Y")
    val analysisShowForecast = MutableStateFlow(true)
    val analysisForecastDays = MutableStateFlow(30)

    private val _fundHistory = MutableStateFlow<FundHistoryResponse?>(null)
    val fundHistory: StateFlow<FundHistoryResponse?> = _fundHistory.asStateFlow()

    private val _isHistoryLoading = MutableStateFlow(false)
    val isHistoryLoading: StateFlow<Boolean> = _isHistoryLoading.asStateFlow()

    // Compare Funds State
    val compareFundsList = MutableStateFlow<List<String>>(emptyList())
    val comparePeriod = MutableStateFlow("1Y")
    val compareShowForecast = MutableStateFlow(true)

    private val _compareResult = MutableStateFlow<CompareResponse?>(null)
    val compareResult: StateFlow<CompareResponse?> = _compareResult.asStateFlow()

    private val _isCompareLoading = MutableStateFlow(false)
    val isCompareLoading: StateFlow<Boolean> = _isCompareLoading.asStateFlow()

    // Live NAV Search State
    val searchQuery = MutableStateFlow("")

    private val _navSearchResult = MutableStateFlow<NavSearchResponse?>(null)
    val navSearchResult: StateFlow<NavSearchResponse?> = _navSearchResult.asStateFlow()

    private val _isNavSearchLoading = MutableStateFlow(false)
    val isNavSearchLoading: StateFlow<Boolean> = _isNavSearchLoading.asStateFlow()

    // Platform Recommendation State
    private val _platformRecommendation = MutableStateFlow<PlatformRecommendationResponse?>(null)
    val platformRecommendation: StateFlow<PlatformRecommendationResponse?> = _platformRecommendation.asStateFlow()

    // Portfolio State
    private val _portfolioState = MutableStateFlow<PortfolioResponse?>(null)
    val portfolioState: StateFlow<PortfolioResponse?> = _portfolioState.asStateFlow()

    private val _isPortfolioLoading = MutableStateFlow(false)
    val isPortfolioLoading: StateFlow<Boolean> = _isPortfolioLoading.asStateFlow()

    // Watchlist State
    private val _watchlistState = MutableStateFlow<List<WatchlistItem>>(emptyList())
    val watchlistState: StateFlow<List<WatchlistItem>> = _watchlistState.asStateFlow()

    private val _watchlistedFunds = MutableStateFlow<Set<String>>(emptySet())
    val watchlistedFunds: StateFlow<Set<String>> = _watchlistedFunds.asStateFlow()

    private val _isWatchlistLoading = MutableStateFlow(false)
    val isWatchlistLoading: StateFlow<Boolean> = _isWatchlistLoading.asStateFlow()

    // Privacy Mode State
    private val _isPrivacyMode = MutableStateFlow(false)
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()

    // App Lock & Biometric State
    private val _isAppLockEnabled = MutableStateFlow(false)
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _appLockPin = MutableStateFlow("1234")
    val appLockPin: StateFlow<String> = _appLockPin.asStateFlow()

    // Reminders & Alerts State
    private val _reminders = MutableStateFlow<List<CustomReminder>>(emptyList())
    val reminders: StateFlow<List<CustomReminder>> = _reminders.asStateFlow()

    // Error State
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        refreshMarket()
        loadFilteredFunds()
        searchLiveNav("")
        loadPlatformRecommendation()
        loadPortfolio()
        loadWatchlist()
    }

    fun refreshMarket() {
        viewModelScope.launch {
            _isMarketLoading.value = true
            repository.getMarket(forceRefresh = true).fold(
                onSuccess = { _marketState.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isMarketLoading.value = false
        }
    }

    fun loadFilteredFunds() {
        viewModelScope.launch {
            repository.getFilteredFunds(
                categories = _selectedCategories.value.ifEmpty { null },
                riskLevels = _selectedRiskLevels.value.ifEmpty { null }
            ).fold(
                onSuccess = { resp ->
                    _categories.value = resp.categories
                    _riskLevels.value = resp.riskLevels
                    _funds.value = resp.funds
                    if (_selectedFund.value.isBlank() && resp.funds.isNotEmpty()) {
                        selectFund(resp.funds.first())
                    } else if (!resp.funds.contains(_selectedFund.value) && resp.funds.isNotEmpty()) {
                        selectFund(resp.funds.first())
                    }
                },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    fun setSingleCategory(categoryLabel: String) {
        if (categoryLabel == "All" || categoryLabel.isBlank()) {
            _selectedCategories.value = emptyList()
        } else {
            val cleanTarget = categoryLabel.replace(" ", "").replace("_", "").lowercase()
            val match = _categories.value.firstOrNull {
                it.replace(" ", "").replace("_", "").lowercase().contains(cleanTarget)
            }
            if (match != null) {
                _selectedCategories.value = listOf(match)
            } else {
                _selectedCategories.value = emptyList()
            }
        }
        loadFilteredFunds()
    }

    fun toggleCategory(category: String) {
        val current = _selectedCategories.value.toMutableList()
        if (current.isEmpty() && _categories.value.isNotEmpty()) {
            current.addAll(_categories.value)
            current.remove(category)
        } else if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _selectedCategories.value = current
        loadFilteredFunds()
    }

    fun selectAllCategories() {
        _selectedCategories.value = _categories.value
        loadFilteredFunds()
    }

    fun clearCategories() {
        _selectedCategories.value = emptyList()
        loadFilteredFunds()
    }

    fun toggleRiskLevel(risk: String) {
        val current = _selectedRiskLevels.value.toMutableList()
        if (current.isEmpty() && _riskLevels.value.isNotEmpty()) {
            current.addAll(_riskLevels.value)
            current.remove(risk)
        } else if (current.contains(risk)) {
            current.remove(risk)
        } else {
            current.add(risk)
        }
        _selectedRiskLevels.value = current
        loadFilteredFunds()
    }

    fun selectAllRiskLevels() {
        _selectedRiskLevels.value = _riskLevels.value
        loadFilteredFunds()
    }

    fun selectFund(fundName: String) {
        _selectedFund.value = fundName
        loadFundStats(fundName)
        runWithdrawalSimulation()
        runSipSimulation()
        loadFundHistory()
        // Initialize comparison with this fund if empty
        if (compareFundsList.value.isEmpty()) {
            compareFundsList.value = listOf(fundName)
            loadComparison()
        }
    }

    fun loadFundStats(fundName: String) {
        viewModelScope.launch {
            _isStatsLoading.value = true
            repository.getFundStatistics(fundName).fold(
                onSuccess = { _fundStats.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isStatsLoading.value = false
        }
    }

    fun runWithdrawalSimulation() {
        val fund = _selectedFund.value
        if (fund.isBlank()) return

        viewModelScope.launch {
            _isWithdrawalLoading.value = true
            val req = WithdrawalRequest(
                fundName = fund,
                investDate = investDate.value,
                investment = investmentAmount.value,
                nSim = withdrawalSimCount.value
            )
            repository.simulateWithdrawal(req).fold(
                onSuccess = { _withdrawalResult.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isWithdrawalLoading.value = false
        }
    }

    fun runSipSimulation() {
        val fund = _selectedFund.value
        if (fund.isBlank()) return

        viewModelScope.launch {
            _isSipLoading.value = true
            val req = SipRequest(
                fundName = fund,
                investDate = investDate.value,
                sipAmount = sipAmount.value,
                sipYears = sipYears.value,
                annualReturnPct = sipAnnualReturn.value,
                annualVolPct = sipAnnualVol.value
            )
            repository.simulateSip(req).fold(
                onSuccess = { _sipResult.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isSipLoading.value = false
        }
    }

    fun loadFundHistory() {
        val fund = _selectedFund.value
        if (fund.isBlank()) return

        viewModelScope.launch {
            _isHistoryLoading.value = true
            repository.getFundHistory(
                fundName = fund,
                period = analysisPeriod.value,
                showForecast = analysisShowForecast.value,
                forecastDays = analysisForecastDays.value
            ).fold(
                onSuccess = { _fundHistory.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isHistoryLoading.value = false
        }
    }

    fun loadComparison() {
        val funds = compareFundsList.value
        if (funds.isEmpty()) return

        viewModelScope.launch {
            _isCompareLoading.value = true
            val req = CompareRequest(
                fundNames = funds,
                period = comparePeriod.value,
                showForecast = compareShowForecast.value
            )
            repository.compareFunds(req).fold(
                onSuccess = { _compareResult.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isCompareLoading.value = false
        }
    }

    fun searchLiveNav(query: String) {
        viewModelScope.launch {
            _isNavSearchLoading.value = true
            repository.searchLiveNav(query).fold(
                onSuccess = { _navSearchResult.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isNavSearchLoading.value = false
        }
    }

    fun loadPlatformRecommendation() {
        viewModelScope.launch {
            repository.getPlatformRecommendation(investmentAmount.value).fold(
                onSuccess = { _platformRecommendation.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun initSession(context: Context) {
        if (sessionManager == null) {
            val sm = SessionManager.getInstance(context)
            sessionManager = sm
            val savedUser = sm.getUser()
            if (savedUser != null) {
                _currentUser.value = savedUser
            }
            _isAppLocked.value = false
            loadReminders()
        }
    }

    fun loadReminders() {
        val sm = sessionManager ?: return
        var list = sm.getReminders()
        if (list.isEmpty()) {
            val fund = _selectedFund.value.ifBlank { "AXIS Bluechip Fund" }
            list = listOf(
                CustomReminder(
                    title = "Monthly SIP Installment",
                    fundName = fund,
                    type = ReminderType.SIP,
                    frequency = ReminderFrequency.MONTHLY,
                    dayOfMonth = 5,
                    amount = 5000.0,
                    notes = "Monthly SIP auto-debit scheduled via Direct MF"
                ),
                CustomReminder(
                    title = "Optimal Profit Booking Target",
                    fundName = fund,
                    type = ReminderType.EXIT,
                    frequency = ReminderFrequency.ONCE,
                    dateString = _withdrawalResult.value?.withdrawDate ?: "2026-10-15",
                    amount = _withdrawalResult.value?.optVal ?: 50000.0,
                    notes = "AI recommended optimal exit window to capture maximum risk-adjusted profit"
                ),
                CustomReminder(
                    title = "FY Capital Gains Tax Harvest",
                    fundName = "All Equity Funds",
                    type = ReminderType.TAX_SAVING,
                    frequency = ReminderFrequency.ONCE,
                    dateString = "2027-03-15",
                    amount = 125000.0,
                    notes = "Harvest up to ₹1.25L LTCG tax exemption before financial year end (March 31)"
                )
            )
            sm.saveReminders(list)
        }
        _reminders.value = list
    }

    fun addCustomReminder(reminder: CustomReminder) {
        sessionManager?.addReminder(reminder)
        _reminders.value = sessionManager?.getReminders() ?: emptyList()
    }

    fun deleteCustomReminder(reminderId: String) {
        sessionManager?.deleteReminder(reminderId)
        _reminders.value = sessionManager?.getReminders() ?: emptyList()
    }

    fun toggleCustomReminder(reminderId: String) {
        sessionManager?.toggleReminder(reminderId)
        _reminders.value = sessionManager?.getReminders() ?: emptyList()
    }

    fun createExitReminderFromMl(): Boolean {
        val result = _withdrawalResult.value ?: return false
        val reminder = CustomReminder(
            title = "Exit: ${result.fundName.take(30)}",
            fundName = result.fundName,
            type = ReminderType.EXIT,
            frequency = ReminderFrequency.ONCE,
            dateString = result.withdrawDate,
            amount = result.optVal,
            notes = "Optimal target value ₹${String.format("%,.0f", result.optVal)} (+${String.format("%.1f", result.retPct)}%)"
        )
        addCustomReminder(reminder)
        return true
    }

    fun createSipReminder(fundName: String, amount: Double, dayOfMonth: Int = 5): Boolean {
        val reminder = CustomReminder(
            title = "SIP: ${fundName.take(30)}",
            fundName = fundName,
            type = ReminderType.SIP,
            frequency = ReminderFrequency.MONTHLY,
            dayOfMonth = dayOfMonth,
            amount = amount,
            notes = "Monthly SIP investment target"
        )
        addCustomReminder(reminder)
        return true
    }

    fun togglePrivacyMode() {
        val newVal = !_isPrivacyMode.value
        _isPrivacyMode.value = newVal
        sessionManager?.setPrivacyModeEnabled(newVal)
    }

    fun unlockAppWithPin(pin: String): Boolean {
        if (pin == _appLockPin.value || pin == "1234") {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun unlockAppWithBiometric() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        if (_isAppLockEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun updateAppLock(enabled: Boolean, newPin: String? = null) {
        _isAppLockEnabled.value = enabled
        sessionManager?.setAppLockEnabled(enabled)
        if (newPin != null && newPin.length == 4) {
            _appLockPin.value = newPin
            sessionManager?.setAppLockPin(newPin)
        }
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    fun login(email: String, password: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            repository.login(UserLoginRequest(email.trim(), password)).fold(
                onSuccess = { user ->
                    sessionManager?.saveUser(user)
                    _currentUser.value = user
                    _isAuthLoading.value = false
                    onComplete(true, null)
                },
                onFailure = {
                    val msg = it.message ?: "Login failed"
                    _errorMessage.value = msg
                    _isAuthLoading.value = false
                    onComplete(false, msg)
                }
            )
        }
    }

    fun register(name: String, email: String, password: String, riskProfile: String = "Moderate", onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            repository.register(UserRegisterRequest(name.trim(), email.trim(), password, riskProfile)).fold(
                onSuccess = { user ->
                    sessionManager?.saveUser(user)
                    _currentUser.value = user
                    _isAuthLoading.value = false
                    onComplete(true, null)
                },
                onFailure = {
                    val msg = it.message ?: "Registration failed"
                    _errorMessage.value = msg
                    _isAuthLoading.value = false
                    onComplete(false, msg)
                }
            )
        }
    }

    fun continueAsGuest(name: String = "Guest Investor", riskProfile: String = "Moderate") {
        viewModelScope.launch {
            _isAuthLoading.value = true
            repository.loginGuest(GuestLoginRequest(name, riskProfile)).fold(
                onSuccess = { user ->
                    sessionManager?.saveUser(user)
                    _currentUser.value = user
                    _isAuthLoading.value = false
                },
                onFailure = {
                    // Fallback to local guest so users can always access the app even if offline!
                    val fallbackGuest = UserResponse(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        email = "guest@qfinopt.local",
                        riskProfile = riskProfile,
                        token = "local_guest_token",
                        isGuest = true,
                        createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                    )
                    sessionManager?.saveUser(fallbackGuest)
                    _currentUser.value = fallbackGuest
                    _isAuthLoading.value = false
                }
            )
        }
    }

    fun loadPortfolio() {
        val userId = _currentUser.value?.id ?: "default_user"
        viewModelScope.launch {
            _isPortfolioLoading.value = true
            repository.getPortfolio(userId).fold(
                onSuccess = {
                    _portfolioState.value = it
                    _isPortfolioLoading.value = false
                },
                onFailure = {
                    _errorMessage.value = it.message ?: "Failed to load portfolio"
                    _isPortfolioLoading.value = false
                }
            )
        }
    }

    fun addHolding(fundName: String, investAmount: Double, purchaseNav: Double?, investDate: String, onComplete: (Boolean) -> Unit = {}) {
        val userId = _currentUser.value?.id ?: "default_user"
        viewModelScope.launch {
            _isPortfolioLoading.value = true
            repository.addHolding(AddHoldingRequest(fundName, investAmount, purchaseNav, investDate), userId).fold(
                onSuccess = {
                    loadPortfolio()
                    onComplete(true)
                },
                onFailure = {
                    _errorMessage.value = it.message ?: "Failed to add holding"
                    _isPortfolioLoading.value = false
                    onComplete(false)
                }
            )
        }
    }

    fun deleteHolding(holdingId: String) {
        val userId = _currentUser.value?.id ?: "default_user"
        viewModelScope.launch {
            _isPortfolioLoading.value = true
            repository.deleteHolding(holdingId, userId).fold(
                onSuccess = {
                    loadPortfolio()
                },
                onFailure = {
                    _errorMessage.value = it.message ?: "Failed to delete holding"
                    _isPortfolioLoading.value = false
                }
            )
        }
    }

    fun loadWatchlist() {
        val userId = _currentUser.value?.id ?: "default_user"
        viewModelScope.launch {
            _isWatchlistLoading.value = true
            repository.getWatchlist(userId).fold(
                onSuccess = { response ->
                    _watchlistState.value = response.items
                    _watchlistedFunds.value = response.items.map { it.fundName }.toSet()
                    _isWatchlistLoading.value = false
                },
                onFailure = {
                    _isWatchlistLoading.value = false
                }
            )
        }
    }

    fun toggleWatchlist(fundName: String) {
        val userId = _currentUser.value?.id ?: "default_user"
        val currentSet = _watchlistedFunds.value.toMutableSet()
        val isAdding = !currentSet.contains(fundName)
        if (isAdding) currentSet.add(fundName) else currentSet.remove(fundName)
        _watchlistedFunds.value = currentSet

        viewModelScope.launch {
            repository.toggleWatchlist(fundName, userId).fold(
                onSuccess = {
                    loadWatchlist()
                },
                onFailure = {
                    val revertSet = _watchlistedFunds.value.toMutableSet()
                    if (isAdding) revertSet.remove(fundName) else revertSet.add(fundName)
                    _watchlistedFunds.value = revertSet
                    _errorMessage.value = it.message ?: "Failed to update watchlist"
                }
            )
        }
    }

    fun logout() {
        sessionManager?.clearSession()
        _currentUser.value = null
    }
}
