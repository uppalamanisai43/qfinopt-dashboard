from typing import List, Dict, Optional, Any
from pydantic import BaseModel, Field

class MarketIndex(BaseModel):
    name: str
    price: float
    change: float
    pct: float

class MarketResponse(BaseModel):
    indices: Dict[str, MarketIndex]
    sentiment: str
    month_return: float
    week_return: float
    last_updated: str

class FundsFilterResponse(BaseModel):
    categories: List[str]
    risk_levels: List[str]
    funds: List[str]
    total_count: int

class FundStatsResponse(BaseModel):
    fund_name: str
    category: str
    risk_level: str
    display_nav: float
    nav_source: str
    ret_1y: float
    sharpe_val: float
    alpha_val: float
    beta_val: float
    expense: float
    mu_real: float
    sigma_real: float
    mu_adjusted: float
    sentiment: str
    sortino_val: Optional[float] = None
    mdd_val: Optional[float] = None
    signal: Optional[str] = None
    conviction_score: Optional[float] = None
    pred_21d_ret_pct: Optional[float] = None
    pred_target_nav: Optional[float] = None
    win_probability: Optional[float] = None
    key_drivers: Optional[List[str]] = None

class HoldingPeriodReturn(BaseModel):
    label: str
    days: int
    expected_value: float
    return_pct: float
    profit_prob: float

class ScenarioMetric(BaseModel):
    label: str
    percentile: int
    value: float
    return_pct: float

class WithdrawalRequest(BaseModel):
    fund_name: str
    invest_date: str = Field(default="")  # YYYY-MM-DD or any parseable date
    investment: float = Field(default=100000.0, ge=100.0)
    n_sim: int = Field(default=5000, ge=100, le=10000)

class WithdrawalResponse(BaseModel):
    fund_name: str
    invest_date: str
    withdraw_date: str
    opt_day: int
    opt_months: int
    opt_val: float
    investment: float
    gain: float
    ret_pct: float
    profit_prob: float
    risk_level: str
    nav_source: str
    beta_val: float
    holding_period_returns: List[HoldingPeriodReturn]
    scenario_breakdown: List[ScenarioMetric]
    chart_days: List[int]
    chart_expected: List[float]
    chart_p5: List[float]
    chart_p95: List[float]
    chart_prob_pct: List[float]

class SipRequest(BaseModel):
    fund_name: str
    invest_date: str = Field(default="")  # YYYY-MM-DD or any parseable date
    sip_amount: float = Field(default=5000.0, ge=100.0)
    sip_years: int = Field(default=10, ge=1, le=30)
    annual_return_pct: float = Field(default=10.0, ge=1.0, le=50.0)
    annual_vol_pct: Optional[float] = None
    n_sim: int = Field(default=5000, ge=100, le=10000)

class SipProjectionRow(BaseModel):
    year: str
    invested: float
    expected: float
    median: float
    range_10_90: str
    profit_probability: float

class SipResponse(BaseModel):
    monthly_sip: float
    sip_years: int
    total_invested: float
    expected_sip: float
    median_sip: float
    gain_sip: float
    expected_xirr: float
    profit_probability: float
    p10: float
    p90: float
    projections: List[SipProjectionRow]
    chart_days: List[int]
    chart_expected: List[float]
    chart_invested: List[float]
    distribution_bins: List[float]
    distribution_counts: List[int]

class FundHistoryResponse(BaseModel):
    fund_name: str
    scheme_code: str
    period: str
    source_label: str
    trend_label: str
    trend_icon: str
    current_pct: float
    predicted_pct: Optional[float] = None
    predicted_nav: Optional[float] = None
    actual_dates: List[str]
    actual_pct: List[float]
    forecast_dates: List[str]
    forecast_pct: List[float]
    forecast_upper_pct: Optional[List[float]] = None
    forecast_lower_pct: Optional[List[float]] = None
    signal: Optional[str] = None
    conviction_score: Optional[float] = None
    pred_21d_ret_pct: Optional[float] = None
    pred_target_nav: Optional[float] = None
    win_probability: Optional[float] = None
    key_drivers: Optional[List[str]] = None
    sortino: Optional[float] = None
    mdd: Optional[float] = None
    hist_return_bins: List[float]
    hist_return_counts: List[int]
    stats: List[Dict[str, str]]

class CompareRequest(BaseModel):
    fund_names: List[str]
    period: str = "1Y"
    show_forecast: bool = True
    forecast_days: int = 30

class CompareSeries(BaseModel):
    fund_name: str
    color: str
    actual_dates: List[str]
    actual_pct: List[float]
    forecast_dates: List[str]
    forecast_pct: List[float]

class CompareFundStats(BaseModel):
    fund_name: str
    sharpe: float
    ret_1y: float
    alpha: float
    beta: float
    expense: float
    risk: str

class CompareResponse(BaseModel):
    period: str
    series: List[CompareSeries]
    table: List[CompareFundStats]

class NavSearchResultItem(BaseModel):
    code: str
    name: str
    nav: float
    date: str

class NavSearchResponse(BaseModel):
    results: List[NavSearchResultItem]
    total_matches: int
    total_funds_in_amfi: int
    lowest_nav: float
    highest_nav: float
    avg_nav: float

class PlatformItem(BaseModel):
    name: str
    url: str
    rating: str
    desc: str

class PlatformRecommendationResponse(BaseModel):
    investment_amount: float
    top_platform: str
    reason: str
    platforms: List[PlatformItem]
    action_plan_steps: List[str]

class PdfReportRequest(BaseModel):
    fund_name: str
    user_name: str = "Investor"
    investment: float = 100000.0
    invest_date: str
    sip_amount: float = 5000.0
    sip_years: int = 10

class UserRegisterRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=50)
    email: str = Field(..., min_length=5, max_length=100)
    password: str = Field(..., min_length=4, max_length=100)
    risk_profile: Optional[str] = "Moderate"

class UserLoginRequest(BaseModel):
    email: str
    password: str

class GuestLoginRequest(BaseModel):
    name: Optional[str] = "Guest Investor"
    risk_profile: Optional[str] = "Moderate"

class UserResponse(BaseModel):
    id: str
    name: str
    email: str
    risk_profile: str
    token: str
    is_guest: bool = False
    created_at: str

class AddHoldingRequest(BaseModel):
    fund_name: str
    invest_amount: float = Field(..., gt=0.0)
    purchase_nav: Optional[float] = Field(default=None, gt=0.0)
    invest_date: Optional[str] = ""

class HoldingItem(BaseModel):
    id: str
    fund_name: str
    category: str
    invest_date: str
    invest_amount: float
    purchase_nav: float
    units: float
    current_nav: float
    current_value: float
    total_pnl: float
    total_pnl_pct: float
    signal: str
    conviction_score: float

class PortfolioResponse(BaseModel):
    total_invested: float
    current_value: float
    total_pnl: float
    total_pnl_pct: float
    health_score: int
    health_status: str
    holdings_count: int
    holdings: List[HoldingItem]

class WatchlistToggleRequest(BaseModel):
    fund_name: str

class WatchlistItem(BaseModel):
    fund_name: str
    category: str
    current_nav: float
    ret_1y: float
    sharpe: float
    sortino: float
    signal: str
    conviction_score: float

class WatchlistResponse(BaseModel):
    items: List[WatchlistItem]
    total_count: int


# ── Quantum-Inspired QAOA Portfolio Optimizer ─────────────────────────────────

class QAOAOptimizeRequest(BaseModel):
    fund_names: List[str] = Field(..., min_length=2, description="List of fund names to optimize (2–15 funds)")
    k: int = Field(default=3, ge=2, description="Number of funds to select in optimal portfolio")
    qaoa_layers: int = Field(default=2, ge=1, le=4, description="QAOA circuit depth p (1–4 layers)")

class QAOABenchmarkRow(BaseModel):
    method: str
    type: str
    sharpe: float
    annual_return_pct: float
    volatility_pct: float
    time_sec: float
    complexity: str

class QAOAMetrics(BaseModel):
    sharpe_ratio: float
    expected_annual_return_pct: float
    annual_volatility_pct: float

class QAOAOptimizeResponse(BaseModel):
    algorithm: str
    n_funds_input: int
    k_funds_selected: int
    qaoa_layers: int
    qubo_size: str
    qubo_energy: float
    convergence_quality: float
    selected_funds: List[str]
    portfolio_weights_pct: Dict[str, float]
    qaoa_metrics: QAOAMetrics
    markowitz_selected: List[str]
    markowitz_metrics: QAOAMetrics
    benchmark_table: List[QAOABenchmarkRow]
    runtime_seconds: float
    quantum_advantage_note: str
