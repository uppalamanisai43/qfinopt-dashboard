package com.qfinopt.app.data.model

import com.google.gson.annotations.SerializedName

data class MarketIndex(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("change") val change: Double,
    @SerializedName("pct") val pct: Double
)

data class MarketResponse(
    @SerializedName("indices") val indices: Map<String, MarketIndex>,
    @SerializedName("sentiment") val sentiment: String,
    @SerializedName("month_return") val monthReturn: Double,
    @SerializedName("week_return") val weekReturn: Double,
    @SerializedName("last_updated") val lastUpdated: String
)

data class FundsFilterResponse(
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("risk_levels") val riskLevels: List<String>,
    @SerializedName("funds") val funds: List<String>,
    @SerializedName("total_count") val totalCount: Int
)

data class FundStatsResponse(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("category") val category: String,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("display_nav") val displayNav: Double,
    @SerializedName("nav_source") val navSource: String,
    @SerializedName("ret_1y") val ret1y: Double,
    @SerializedName("sharpe_val") val sharpeVal: Double,
    @SerializedName("alpha_val") val alphaVal: Double,
    @SerializedName("beta_val") val betaVal: Double,
    @SerializedName("expense") val expense: Double,
    @SerializedName("mu_real") val muReal: Double,
    @SerializedName("sigma_real") val sigmaReal: Double,
    @SerializedName("mu_adjusted") val muAdjusted: Double,
    @SerializedName("sentiment") val sentiment: String,
    @SerializedName("sortino_val") val sortinoVal: Double? = null,
    @SerializedName("mdd_val") val mddVal: Double? = null,
    @SerializedName("signal") val signal: String? = null,
    @SerializedName("conviction_score") val convictionScore: Double? = null,
    @SerializedName("pred_21d_ret_pct") val pred21dRetPct: Double? = null,
    @SerializedName("pred_target_nav") val predTargetNav: Double? = null,
    @SerializedName("win_probability") val winProbability: Double? = null,
    @SerializedName("key_drivers") val keyDrivers: List<String>? = null
)

data class HoldingPeriodReturn(
    @SerializedName("label") val label: String,
    @SerializedName("days") val days: Int,
    @SerializedName("expected_value") val expectedValue: Double,
    @SerializedName("return_pct") val returnPct: Double,
    @SerializedName("profit_prob") val profitProb: Double
)

data class ScenarioMetric(
    @SerializedName("label") val label: String,
    @SerializedName("percentile") val percentile: Int,
    @SerializedName("value") val value: Double,
    @SerializedName("return_pct") val returnPct: Double
)

data class WithdrawalRequest(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("invest_date") val investDate: String,
    @SerializedName("investment") val investment: Double,
    @SerializedName("n_sim") val nSim: Int = 5000
)

data class WithdrawalResponse(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("invest_date") val investDate: String,
    @SerializedName("withdraw_date") val withdrawDate: String,
    @SerializedName("opt_day") val optDay: Int,
    @SerializedName("opt_months") val optMonths: Int,
    @SerializedName("opt_val") val optVal: Double,
    @SerializedName("investment") val investment: Double,
    @SerializedName("gain") val gain: Double,
    @SerializedName("ret_pct") val retPct: Double,
    @SerializedName("profit_prob") val profitProb: Double,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("nav_source") val navSource: String,
    @SerializedName("beta_val") val betaVal: Double,
    @SerializedName("holding_period_returns") val holdingPeriodReturns: List<HoldingPeriodReturn>,
    @SerializedName("scenario_breakdown") val scenarioBreakdown: List<ScenarioMetric>,
    @SerializedName("chart_days") val chartDays: List<Int>,
    @SerializedName("chart_expected") val chartExpected: List<Double>,
    @SerializedName("chart_p5") val chartP5: List<Double>,
    @SerializedName("chart_p95") val chartP95: List<Double>,
    @SerializedName("chart_prob_pct") val chartProbPct: List<Double>
)

data class SipRequest(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("invest_date") val investDate: String,
    @SerializedName("sip_amount") val sipAmount: Double,
    @SerializedName("sip_years") val sipYears: Int,
    @SerializedName("annual_return_pct") val annualReturnPct: Double = 10.0,
    @SerializedName("annual_vol_pct") val annualVolPct: Double? = null,
    @SerializedName("n_sim") val nSim: Int = 5000
)

data class SipProjectionRow(
    @SerializedName("year") val year: String,
    @SerializedName("invested") val invested: Double,
    @SerializedName("expected") val expected: Double,
    @SerializedName("median") val median: Double,
    @SerializedName("range_10_90") val range10to90: String,
    @SerializedName("profit_probability") val profitProbability: Double
)

data class SipResponse(
    @SerializedName("monthly_sip") val monthlySip: Double,
    @SerializedName("sip_years") val sipYears: Int,
    @SerializedName("total_invested") val totalInvested: Double,
    @SerializedName("expected_sip") val expectedSip: Double,
    @SerializedName("median_sip") val medianSip: Double,
    @SerializedName("gain_sip") val gainSip: Double,
    @SerializedName("expected_xirr") val expectedXirr: Double,
    @SerializedName("profit_probability") val profitProbability: Double,
    @SerializedName("p10") val p10: Double,
    @SerializedName("p90") val p90: Double,
    @SerializedName("projections") val projections: List<SipProjectionRow>,
    @SerializedName("chart_days") val chartDays: List<Int>,
    @SerializedName("chart_expected") val chartExpected: List<Double>,
    @SerializedName("chart_invested") val chartInvested: List<Double>,
    @SerializedName("distribution_bins") val distributionBins: List<Double>,
    @SerializedName("distribution_counts") val distributionCounts: List<Int>
)

data class FundHistoryResponse(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("scheme_code") val schemeCode: String,
    @SerializedName("period") val period: String,
    @SerializedName("source_label") val sourceLabel: String,
    @SerializedName("trend_label") val trendLabel: String,
    @SerializedName("trend_icon") val trendIcon: String,
    @SerializedName("current_pct") val currentPct: Double,
    @SerializedName("predicted_pct") val predictedPct: Double?,
    @SerializedName("predicted_nav") val predictedNav: Double?,
    @SerializedName("actual_dates") val actualDates: List<String>,
    @SerializedName("actual_pct") val actualPct: List<Double>,
    @SerializedName("forecast_dates") val forecastDates: List<String>,
    @SerializedName("forecast_pct") val forecastPct: List<Double>,
    @SerializedName("forecast_upper_pct") val forecastUpperPct: List<Double>? = null,
    @SerializedName("forecast_lower_pct") val forecastLowerPct: List<Double>? = null,
    @SerializedName("signal") val signal: String? = null,
    @SerializedName("conviction_score") val convictionScore: Double? = null,
    @SerializedName("pred_21d_ret_pct") val pred21dRetPct: Double? = null,
    @SerializedName("pred_target_nav") val predTargetNav: Double? = null,
    @SerializedName("win_probability") val winProbability: Double? = null,
    @SerializedName("key_drivers") val keyDrivers: List<String>? = null,
    @SerializedName("sortino") val sortino: Double? = null,
    @SerializedName("mdd") val mdd: Double? = null,
    @SerializedName("hist_return_bins") val histReturnBins: List<Double>,
    @SerializedName("hist_return_counts") val histReturnCounts: List<Int>,
    @SerializedName("stats") val stats: List<Map<String, String>>
)

data class CompareRequest(
    @SerializedName("fund_names") val fundNames: List<String>,
    @SerializedName("period") val period: String = "1Y",
    @SerializedName("show_forecast") val showForecast: Boolean = true,
    @SerializedName("forecast_days") val forecastDays: Int = 30
)

data class CompareSeries(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("color") val color: String,
    @SerializedName("actual_dates") val actualDates: List<String>,
    @SerializedName("actual_pct") val actualPct: List<Double>,
    @SerializedName("forecast_dates") val forecastDates: List<String>,
    @SerializedName("forecast_pct") val forecastPct: List<Double>
)

data class CompareFundStats(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("sharpe") val sharpe: Double,
    @SerializedName("ret_1y") val ret1y: Double,
    @SerializedName("alpha") val alpha: Double,
    @SerializedName("beta") val beta: Double,
    @SerializedName("expense") val expense: Double,
    @SerializedName("risk") val risk: String
)

data class CompareResponse(
    @SerializedName("period") val period: String,
    @SerializedName("series") val series: List<CompareSeries>,
    @SerializedName("table") val table: List<CompareFundStats>
)

data class NavSearchResultItem(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("nav") val nav: Double,
    @SerializedName("date") val date: String
)

data class NavSearchResponse(
    @SerializedName("results") val results: List<NavSearchResultItem>,
    @SerializedName("total_matches") val totalMatches: Int,
    @SerializedName("total_funds_in_amfi") val totalFundsInAmfi: Int,
    @SerializedName("lowest_nav") val lowestNav: Double,
    @SerializedName("highest_nav") val highestNav: Double,
    @SerializedName("avg_nav") val avgNav: Double
)

data class PlatformItem(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String,
    @SerializedName("rating") val rating: String,
    @SerializedName("desc") val desc: String
)

data class PlatformRecommendationResponse(
    @SerializedName("investment_amount") val investmentAmount: Double,
    @SerializedName("top_platform") val topPlatform: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("platforms") val platforms: List<PlatformItem>,
    @SerializedName("action_plan_steps") val actionPlanSteps: List<String>
)

data class PdfReportRequest(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("user_name") val userName: String = "Investor",
    @SerializedName("investment") val investment: Double = 100000.0,
    @SerializedName("invest_date") val investDate: String,
    @SerializedName("sip_amount") val sipAmount: Double = 5000.0,
    @SerializedName("sip_years") val sipYears: Int = 10
)

data class UserRegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("risk_profile") val riskProfile: String = "Moderate"
)

data class UserLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class GuestLoginRequest(
    @SerializedName("name") val name: String = "Guest Investor",
    @SerializedName("risk_profile") val riskProfile: String = "Moderate"
)

data class UserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("risk_profile") val riskProfile: String,
    @SerializedName("token") val token: String,
    @SerializedName("is_guest") val isGuest: Boolean = false,
    @SerializedName("created_at") val createdAt: String
)

data class AddHoldingRequest(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("invest_amount") val investAmount: Double,
    @SerializedName("purchase_nav") val purchaseNav: Double? = null,
    @SerializedName("invest_date") val investDate: String = ""
)

data class HoldingItem(
    @SerializedName("id") val id: String,
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("category") val category: String,
    @SerializedName("invest_date") val investDate: String,
    @SerializedName("invest_amount") val investAmount: Double,
    @SerializedName("purchase_nav") val purchaseNav: Double,
    @SerializedName("units") val units: Double,
    @SerializedName("current_nav") val currentNav: Double,
    @SerializedName("current_value") val currentValue: Double,
    @SerializedName("total_pnl") val totalPnl: Double,
    @SerializedName("total_pnl_pct") val totalPnlPct: Double,
    @SerializedName("signal") val signal: String,
    @SerializedName("conviction_score") val convictionScore: Double
)

data class PortfolioResponse(
    @SerializedName("total_invested") val totalInvested: Double,
    @SerializedName("current_value") val currentValue: Double,
    @SerializedName("total_pnl") val totalPnl: Double,
    @SerializedName("total_pnl_pct") val totalPnlPct: Double,
    @SerializedName("health_score") val healthScore: Int,
    @SerializedName("health_status") val healthStatus: String,
    @SerializedName("holdings_count") val holdingsCount: Int,
    @SerializedName("holdings") val holdings: List<HoldingItem>
)

data class WatchlistToggleRequest(
    @SerializedName("fund_name") val fundName: String
)

data class WatchlistItem(
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("category") val category: String,
    @SerializedName("current_nav") val currentNav: Double,
    @SerializedName("ret_1y") val ret1y: Double,
    @SerializedName("sharpe") val sharpe: Double,
    @SerializedName("sortino") val sortino: Double,
    @SerializedName("signal") val signal: String,
    @SerializedName("conviction_score") val convictionScore: Double
)

data class WatchlistResponse(
    @SerializedName("items") val items: List<WatchlistItem>,
    @SerializedName("total_count") val totalCount: Int
)

