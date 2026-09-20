import io
import os
from fastapi import FastAPI, HTTPException, Query, Response
from fastapi.responses import HTMLResponse, FileResponse
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Optional
from datetime import datetime

from app.models import (
    MarketResponse,
    FundsFilterResponse,
    FundStatsResponse,
    WithdrawalRequest,
    WithdrawalResponse,
    SipRequest,
    SipResponse,
    FundHistoryResponse,
    CompareRequest,
    CompareResponse,
    NavSearchResponse,
    NavSearchResultItem,
    PlatformRecommendationResponse,
    PdfReportRequest,
    UserRegisterRequest,
    UserLoginRequest,
    GuestLoginRequest,
    UserResponse,
    AddHoldingRequest,
    HoldingItem,
    PortfolioResponse,
    WatchlistToggleRequest,
    WatchlistItem,
    WatchlistResponse,
    QAOAOptimizeRequest,
    QAOAOptimizeResponse,
)
from app.auth_service import (
    register_user,
    login_user,
    login_guest,
    get_user_by_token,
)
from app.market_service import fetch_live_market
from app.data_manager import (
    get_categories_and_risk_levels,
    filter_funds,
    get_live_nav_amfi,
    load_historical_data,
)
from app.fund_service import (
    get_fund_stats,
    get_fund_history,
    compare_funds,
    get_platform_recommendation,
)
from app.simulation_service import (
    run_withdrawal_simulation,
    run_sip_simulation,
)
from app.portfolio_service import (
    get_user_portfolio,
    add_user_holding,
    delete_user_holding,
    get_user_watchlist,
    toggle_user_watchlist,
)
from app.pdf_service import generate_pdf_report
from app.config import IST

import threading
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Warm up background caches asynchronously on startup."""
    def warmup():
        try:
            load_historical_data()
            get_live_nav_amfi()
        except Exception as e:
            print(f"Warning: Initial dataset warm-up deferred: {e}")
    threading.Thread(target=warmup, daemon=True).start()
    yield

app = FastAPI(
    title="Q-FinOpt API",
    description="High-performance backend API for Q-FinOpt Mutual Fund Advisor",
    version="3.0.0",
    lifespan=lifespan
)

# Enable CORS for all origins (Android app, local development, etc.)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def _get_apk_path() -> Optional[str]:
    candidate_paths = [
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "QFinOpt.apk")),
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "QFinOpt.apk")),
        os.path.abspath(os.path.join(os.getcwd(), "QFinOpt.apk")),
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "android", "app", "build", "outputs", "apk", "debug", "app-debug.apk")),
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "android", "app", "build", "outputs", "apk", "debug", "app-debug.apk")),
    ]
    for p in candidate_paths:
        if os.path.exists(p) and os.path.getsize(p) > 0:
            return p
    return None

def _get_report_pdf_path() -> Optional[str]:
    candidate_paths = [
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "Q_FinOpt_Project_Report.pdf")),
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "Q_FinOpt_Project_Report.pdf")),
        os.path.abspath(os.path.join(os.getcwd(), "Q_FinOpt_Project_Report.pdf")),
    ]
    for p in candidate_paths:
        if os.path.exists(p) and os.path.getsize(p) > 0:
            return p
    return None

@app.get("/", response_class=HTMLResponse)
def home_download_page():
    """Interactive download page for the Q-FinOpt Android mobile app."""
    apk_path = _get_apk_path()
    apk_size_mb = 0.0
    if apk_path and os.path.exists(apk_path):
        apk_size_mb = round(os.path.getsize(apk_path) / (1024 * 1024), 2)
    
    html = f"""
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Q-FinOpt APK Download</title>
        <style>
            * {{ box-sizing: border-box; }}
            body {{
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                background-color: #0A0E17;
                color: #E2E8F0;
                margin: 0;
                padding: 24px 16px;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                min-height: 100vh;
            }}
            .card {{
                background: #111827;
                border: 1px solid #1E293B;
                border-radius: 24px;
                max-width: 440px;
                width: 100%;
                padding: 32px 24px;
                box-shadow: 0 20px 40px rgba(0,0,0,0.6);
                text-align: center;
            }}
            .logo {{
                font-size: 52px;
                margin-bottom: 12px;
            }}
            h1 {{
                font-size: 26px;
                margin: 0 0 6px 0;
                color: #F8FAFC;
                font-weight: 800;
            }}
            .badge {{
                display: inline-block;
                background: rgba(16, 185, 129, 0.15);
                color: #10B981;
                font-size: 13px;
                font-weight: 600;
                padding: 6px 14px;
                border-radius: 9999px;
                margin-bottom: 18px;
                border: 1px solid rgba(16, 185, 129, 0.3);
            }}
            p {{
                font-size: 14px;
                color: #94A3B8;
                line-height: 1.5;
                margin: 0 0 26px 0;
            }}
            .btn {{
                display: block;
                background: linear-gradient(135deg, #10B981 0%, #059669 100%);
                color: #FFFFFF;
                text-decoration: none;
                font-weight: 700;
                font-size: 18px;
                padding: 16px 20px;
                border-radius: 16px;
                box-shadow: 0 6px 20px rgba(16, 185, 129, 0.35);
                transition: transform 0.15s ease, box-shadow 0.15s ease;
            }}
            .btn:active {{
                transform: scale(0.97);
            }}
            .instructions {{
                background: #1E293B;
                border-radius: 14px;
                padding: 18px 20px;
                margin-top: 26px;
                text-align: left;
                font-size: 13px;
                color: #CBD5E1;
            }}
            .instructions strong {{
                color: #F1F5F9;
                display: block;
                margin-bottom: 10px;
                font-size: 14px;
            }}
            .instructions ol {{
                margin: 0;
                padding-left: 20px;
            }}
            .instructions li {{
                margin-bottom: 10px;
                line-height: 1.4;
            }}
            .instructions li:last-child {{
                margin-bottom: 0;
            }}
            .footer {{
                margin-top: 24px;
                font-size: 12px;
                color: #64748B;
            }}
        </style>
    </head>
    <body>
        <div class="card">
            <div class="logo">📈</div>
            <h1>Q-FinOpt</h1>
            <div class="badge">v3.0.0 &bull; {apk_size_mb} MB &bull; APK Ready</div>
            <p>Smart Mutual Fund & Systematic Withdrawal Advisor with real-time AMFI NAV analytics & user account management.</p>
            
            <a href="/QFinOpt.apk" class="btn" download="QFinOpt.apk">📲 Download QFinOpt.apk</a>
            
            <div class="instructions">
                <strong>📲 How to Install on Your Phone:</strong>
                <ol>
                    <li>Tap the green <strong>Download</strong> button above.</li>
                    <li>If Chrome warns <em>"File might be harmful"</em>, tap <strong>"Download anyway"</strong>.</li>
                    <li>When the download completes, tap the notification to <strong>Install / Update</strong>.</li>
                </ol>
            </div>
        </div>
        <div class="footer">
            Q-FinOpt Server running &bull; Port 8000
        </div>
    </body>
    </html>
    """
    return HTMLResponse(content=html)

@app.get("/download")
@app.get("/download/apk")
@app.get("/QFinOpt.apk")
def download_apk():
    """Direct binary download endpoint for the Android APK."""
    apk_path = _get_apk_path()
    if apk_path and os.path.exists(apk_path):
        return FileResponse(
            apk_path,
            media_type="application/vnd.android.package-archive",
            filename="QFinOpt.apk",
            headers={"Content-Disposition": "attachment; filename=QFinOpt.apk"}
        )
    raise HTTPException(status_code=404, detail="QFinOpt.apk not found on server")

@app.get("/report/project")
@app.get("/Q_FinOpt_Project_Report.pdf")
def download_project_report_pdf():
    """Download the comprehensive Q-FinOpt System Technical Report PDF."""
    pdf_path = _get_report_pdf_path()
    if pdf_path and os.path.exists(pdf_path):
        return FileResponse(
            pdf_path,
            media_type="application/pdf",
            filename="Q_FinOpt_Project_Report.pdf",
            headers={"Content-Disposition": "attachment; filename=Q_FinOpt_Project_Report.pdf"}
        )
    raise HTTPException(status_code=404, detail="Project report PDF not found")

@app.get("/health")
def health_check():
    """Service health and timestamp check."""
    return {
        "status": "ok",
        "service": "Q-FinOpt API",
        "timestamp": datetime.now(IST).isoformat(),
        "version": "3.0.0"
    }

@app.post("/api/auth/register", response_model=UserResponse)
def auth_register(req: UserRegisterRequest):
    """Register a new user account."""
    try:
        return register_user(req)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/auth/login", response_model=UserResponse)
def auth_login(req: UserLoginRequest):
    """Login with existing email and password credentials."""
    try:
        return login_user(req)
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/auth/guest", response_model=UserResponse)
def auth_guest(req: GuestLoginRequest = GuestLoginRequest()):
    """Create an instant guest investor session."""
    try:
        return login_guest(req)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/auth/profile", response_model=UserResponse)
def auth_profile(token: str = Query(..., description="Session token")):
    """Fetch user profile by session token."""
    user = get_user_by_token(token)
    if not user:
        raise HTTPException(status_code=401, detail="Invalid or expired session token.")
    return user

@app.get("/api/market", response_model=MarketResponse)
def get_market(refresh: bool = False):
    """Retrieve live market indices (Nifty, Sensex, etc.) and market sentiment."""
    try:
        return fetch_live_market(force_refresh=refresh)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/funds/filter", response_model=FundsFilterResponse)
def get_filtered_funds(
    categories: Optional[List[str]] = Query(None),
    risk_levels: Optional[List[str]] = Query(None)
):
    """Retrieve available categories, risk levels, and filtered funds list."""
    try:
        all_cats, all_risks = get_categories_and_risk_levels()
        selected_cats = categories if categories else all_cats
        selected_risks = risk_levels if risk_levels else all_risks

        funds = filter_funds(categories=selected_cats, risk_levels=selected_risks)
        return FundsFilterResponse(
            categories=all_cats,
            risk_levels=all_risks,
            funds=funds,
            total_count=len(funds)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/funds/{fund_name}/stats", response_model=FundStatsResponse)
def get_fund_statistics(fund_name: str):
    """Retrieve statistical indicators (Sharpe, Alpha, Beta, NAV, 1Y Return) for a fund."""
    try:
        return get_fund_stats(fund_name)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/simulate/withdrawal", response_model=WithdrawalResponse)
def simulate_withdrawal(req: WithdrawalRequest):
    """Run Monte Carlo withdrawal timing simulation for optimal exit recommendation."""
    try:
        return run_withdrawal_simulation(req)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/simulate/sip", response_model=SipResponse)
def simulate_sip(req: SipRequest):
    """Run SIP growth projection simulation with XIRR and year-by-year projections."""
    try:
        return run_sip_simulation(req)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/funds/{fund_name}/history", response_model=FundHistoryResponse)
def get_fund_history_data(
    fund_name: str,
    period: str = "1Y",
    show_forecast: bool = True,
    forecast_days: int = 30
):
    """Retrieve historical % change and trend forecast for a fund."""
    try:
        return get_fund_history(
            fund_name=fund_name,
            period=period,
            show_forecast=show_forecast,
            forecast_days=forecast_days
        )
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/funds/compare", response_model=CompareResponse)
def compare_multiple_funds(req: CompareRequest):
    """Compare multiple mutual funds with % change curves and summary table."""
    try:
        return compare_funds(req)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/nav/search", response_model=NavSearchResponse)
def search_live_nav(q: str = Query("", description="Search term for AMFI funds")):
    """Search official AMFI India live NAV for all active mutual funds."""
    try:
        nav_today = get_live_nav_amfi()
        results: List[NavSearchResultItem] = []
        q_lower = q.lower().strip()

        nav_values = []
        for name, data in nav_today.items():
            nav_values.append(data["nav"])
            if not q_lower or q_lower in name.lower():
                results.append(
                    NavSearchResultItem(
                        code=data["code"],
                        name=name,
                        nav=data["nav"],
                        date=data["date"]
                    )
                )

        lowest_nav = min(nav_values) if nav_values else 0.0
        highest_nav = max(nav_values) if nav_values else 0.0
        avg_nav = (sum(nav_values) / len(nav_values)) if nav_values else 0.0

        return NavSearchResponse(
            results=results[:100],  # Limit to 100 for fast mobile rendering
            total_matches=len(results),
            total_funds_in_amfi=len(nav_today),
            lowest_nav=round(lowest_nav, 2),
            highest_nav=round(highest_nav, 2),
            avg_nav=round(avg_nav, 2)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/platform/recommendation", response_model=PlatformRecommendationResponse)
def platform_recommendation(investment: float = 100000.0):
    """Get platform recommendations (Groww, Kuvera, Zerodha Coin) and personalized action steps."""
    return get_platform_recommendation(investment)

@app.post("/api/report/pdf")
def download_pdf_report(req: PdfReportRequest):
    """Generate and return a downloadable PDF investment report."""
    try:
        pdf_bytes = generate_pdf_report(req)
        clean_name = req.fund_name[:15].replace(" ", "_").replace("/", "_")
        filename = f"QFinOpt_{clean_name}_{datetime.now().strftime('%Y%m%d')}.pdf"
        return Response(
            content=pdf_bytes,
            media_type="application/pdf",
            headers={"Content-Disposition": f"attachment; filename={filename}"}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/download/apk")
def download_apk():
    """Download the QFinOpt Android APK directly to any phone or browser."""
    import os
    from fastapi.responses import FileResponse
    apk_path = _get_apk_path()
    if apk_path and os.path.exists(apk_path):
        return FileResponse(apk_path, media_type="application/vnd.android.package-archive", filename="QFinOpt.apk")
    raise HTTPException(status_code=404, detail="APK file not found")

# ==============================================================================
# Portfolio & Watchlist Endpoints
# ==============================================================================

@app.get("/api/portfolio", response_model=PortfolioResponse)
def api_get_portfolio(user_id: str = Query(default="default_user")):
    """Get real-time valued user portfolio with live profit/loss and AI health score."""
    try:
        return get_user_portfolio(user_id)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/portfolio/holding", response_model=HoldingItem)
def api_add_holding(req: AddHoldingRequest, user_id: str = Query(default="default_user")):
    """Add a new mutual fund investment holding to the portfolio."""
    try:
        return add_user_holding(user_id, req)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/api/portfolio/holding/{holding_id}")
def api_delete_holding(holding_id: str, user_id: str = Query(default="default_user")):
    """Delete an investment holding from the user's portfolio."""
    success = delete_user_holding(user_id, holding_id)
    if not success:
        raise HTTPException(status_code=404, detail="Holding not found")
    return {"status": "ok", "deleted": holding_id}

@app.get("/api/watchlist", response_model=WatchlistResponse)
def api_get_watchlist(user_id: str = Query(default="default_user")):
    """Get all watchlisted mutual funds with live NAV and ML recommendation signals."""
    try:
        return get_user_watchlist(user_id)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/watchlist/toggle")
def api_toggle_watchlist(req: WatchlistToggleRequest, user_id: str = Query(default="default_user")):
    """Add or remove a fund from user's personal watchlist."""
    try:
        return toggle_user_watchlist(user_id, req.fund_name)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ==============================================================================
# Quantum-Inspired QAOA Portfolio Optimizer
# ==============================================================================

@app.post("/api/qaoa/optimize", response_model=QAOAOptimizeResponse)
def qaoa_portfolio_optimize(req: QAOAOptimizeRequest):
    """
    🔬 Quantum-Inspired Portfolio Optimizer (QAOA — Quantum Approximate Optimization Algorithm)

    Formulates mutual fund portfolio selection as a QUBO (Quadratic Unconstrained
    Binary Optimization) problem and solves it using a simulated QAOA circuit
    with p layers. Returns optimal fund selection, Sharpe ratio, and a full
    benchmark table comparing QAOA vs Classical Markowitz vs Brute-Force.
    """
    try:
        from app.qaoa_optimizer import run_qaoa_portfolio_optimizer
        from app.data_manager import load_historical_data

        fund_names = req.fund_names
        n = len(fund_names)

        if n < 2:
            raise HTTPException(status_code=400, detail="Need at least 2 funds to optimize.")
        if n > 15:
            raise HTTPException(status_code=400, detail="Maximum 15 funds supported for QAOA simulation.")
        if req.k >= n:
            raise HTTPException(status_code=400, detail=f"k ({req.k}) must be less than number of funds ({n}).")

        # Load historical data to compute real return and volatility per fund
        df = load_historical_data()

        fund_returns = []
        fund_vols = []
        valid_names = []
        fund_series = {}

        for fname in fund_names:
            fdf = df[df["Scheme_Name"] == fname]
            if fdf.empty:
                # Try fuzzy match
                from difflib import get_close_matches
                matches = get_close_matches(fname, df["Scheme_Name"].unique().tolist(), n=1, cutoff=0.5)
                if matches:
                    fdf = df[df["Scheme_Name"] == matches[0]]
                    fname = matches[0]

            if fdf.empty or len(fdf) < 30:
                continue

            fdf = fdf.sort_values("Date")
            if "Daily_Return_%" in fdf.columns:
                daily_rets = fdf["Daily_Return_%"].dropna().values / 100.0
            else:
                navs = fdf["NAV_Value"].values
                daily_rets = np.diff(navs) / np.maximum(navs[:-1], 1e-6)

            daily_rets = daily_rets[np.isfinite(daily_rets)]
            if len(daily_rets) < 20:
                continue

            fund_returns.append(float(np.mean(daily_rets)))
            fund_vols.append(float(np.std(daily_rets)))
            fund_series[fname] = daily_rets
            valid_names.append(fname)

        if len(valid_names) < 2:
            raise HTTPException(
                status_code=404,
                detail="Not enough historical data found for the given funds. Try different fund names."
            )

        # Build correlation matrix from aligned return series
        import numpy as np
        min_len = min(len(s) for s in fund_series.values())
        aligned = np.array([s[-min_len:] for s in fund_series.values()])
        corr_matrix = np.corrcoef(aligned).tolist()

        k = min(req.k, len(valid_names) - 1)

        result = run_qaoa_portfolio_optimizer(
            fund_names=valid_names,
            fund_returns=fund_returns,
            fund_volatilities=fund_vols,
            correlations=corr_matrix,
            k=k,
            qaoa_layers=req.qaoa_layers,
        )

        # Parse benchmark table into response model
        benchmark_rows = []
        for row in result["benchmark_table"]:
            benchmark_rows.append({
                "method": row["method"],
                "type": row["type"],
                "sharpe": row["sharpe"],
                "annual_return_pct": row["annual_return_pct"],
                "volatility_pct": row["volatility_pct"],
                "time_sec": row["time_sec"],
                "complexity": row["complexity"],
            })

        return QAOAOptimizeResponse(
            algorithm=result["algorithm"],
            n_funds_input=result["n_funds_input"],
            k_funds_selected=result["k_funds_selected"],
            qaoa_layers=result["qaoa_layers"],
            qubo_size=result["qubo_size"],
            qubo_energy=result["qubo_energy"],
            convergence_quality=result["convergence_quality"],
            selected_funds=result["selected_funds"],
            portfolio_weights_pct=result["portfolio_weights_pct"],
            qaoa_metrics=result["qaoa_metrics"],
            markowitz_selected=result["markowitz_selected"],
            markowitz_metrics=result["markowitz_metrics"],
            benchmark_table=benchmark_rows,
            runtime_seconds=result["runtime_seconds"],
            quantum_advantage_note=result["quantum_advantage_note"],
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"QAOA optimizer error: {str(e)}")



