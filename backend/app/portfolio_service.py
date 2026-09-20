import os
import json
import uuid
from datetime import datetime
from typing import Dict, Any, List, Optional
from app.models import (
    AddHoldingRequest,
    HoldingItem,
    PortfolioResponse,
    WatchlistItem,
    WatchlistResponse,
)
from app.fund_service import get_fund_stats
from app.data_manager import load_historical_data, match_fund_amfi_nav, get_live_nav_amfi

DATA_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "data"))
PORTFOLIO_FILE = os.path.join(DATA_DIR, "portfolios.json")

def _ensure_portfolio_storage():
    """Ensure data directory and portfolios.json exist."""
    os.makedirs(DATA_DIR, exist_ok=True)
    if not os.path.exists(PORTFOLIO_FILE):
        with open(PORTFOLIO_FILE, "w", encoding="utf-8") as f:
            json.dump({}, f)

def _load_portfolios() -> Dict[str, Dict[str, Any]]:
    """Load portfolios dictionary keyed by user_id."""
    _ensure_portfolio_storage()
    try:
        with open(PORTFOLIO_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception:
        return {}

def _save_portfolios(data: Dict[str, Dict[str, Any]]):
    """Save portfolios dictionary to file."""
    _ensure_portfolio_storage()
    with open(PORTFOLIO_FILE, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

def _get_user_entry(user_id: str) -> Dict[str, Any]:
    """Get or initialize user portfolio and watchlist entry."""
    data = _load_portfolios()
    user_key = str(user_id).strip()
    if user_key not in data:
        data[user_key] = {
            "holdings": [],
            "watchlist": []
        }
    return data[user_key]

def get_user_portfolio(user_id: str) -> PortfolioResponse:
    """Calculate real-time portfolio valuation, profit/loss, and AI health score."""
    entry = _get_user_entry(user_id)
    raw_holdings = entry.get("holdings", [])

    if not raw_holdings:
        return PortfolioResponse(
            total_invested=0.0,
            current_value=0.0,
            total_pnl=0.0,
            total_pnl_pct=0.0,
            health_score=100,
            health_status="🟢 Ready • Add Your First Fund",
            holdings_count=0,
            holdings=[]
        )

    holdings_items: List[HoldingItem] = []
    total_invested = 0.0
    total_current_value = 0.0
    signal_scores: List[float] = []

    for h in raw_holdings:
        fund_name = h.get("fund_name", "")
        invest_amount = float(h.get("invest_amount", 0.0))
        purchase_nav = float(h.get("purchase_nav", 10.0))
        units = float(h.get("units", invest_amount / max(purchase_nav, 0.01)))
        invest_date = str(h.get("invest_date", ""))

        # Fetch live stats and ML signal
        try:
            stats = get_fund_stats(fund_name)
            current_nav = stats.display_nav
            category = stats.category
            signal = stats.signal or "HOLD"
            conviction = stats.conviction_score or 50.0
        except Exception:
            current_nav = purchase_nav
            category = "Equity"
            signal = "HOLD"
            conviction = 50.0

        current_value = round(units * current_nav, 2)
        total_pnl = round(current_value - invest_amount, 2)
        total_pnl_pct = round((total_pnl / max(invest_amount, 1.0)) * 100.0, 2)

        total_invested += invest_amount
        total_current_value += current_value

        # Score signal for portfolio health
        if signal == "STRONG BUY":
            signal_scores.append(100.0)
        elif signal == "ACCUMULATE":
            signal_scores.append(85.0)
        elif signal == "HOLD":
            signal_scores.append(70.0)
        elif signal == "TRIM":
            signal_scores.append(45.0)
        else:  # REDUCE
            signal_scores.append(30.0)

        holdings_items.append(
            HoldingItem(
                id=h.get("id", str(uuid.uuid4())),
                fund_name=fund_name,
                category=category,
                invest_date=invest_date,
                invest_amount=invest_amount,
                purchase_nav=purchase_nav,
                units=round(units, 4),
                current_nav=current_nav,
                current_value=current_value,
                total_pnl=total_pnl,
                total_pnl_pct=total_pnl_pct,
                signal=signal,
                conviction_score=conviction
            )
        )

    portfolio_pnl = round(total_current_value - total_invested, 2)
    portfolio_pnl_pct = round((portfolio_pnl / max(total_invested, 1.0)) * 100.0, 2)

    # Compute aggregate AI health score
    avg_signal = float(sum(signal_scores) / len(signal_scores)) if signal_scores else 75.0
    pnl_factor = max(min(portfolio_pnl_pct * 0.5, 15.0), -20.0)
    health_score = int(round(max(min(avg_signal + pnl_factor, 100.0), 20.0)))

    if health_score >= 80:
        health_status = "🟢 Excellent • Strong Growth Potential"
    elif health_score >= 65:
        health_status = "🟡 Good • Balanced Momentum"
    else:
        health_status = "🔴 Caution • Rebalancing Recommended"

    return PortfolioResponse(
        total_invested=round(total_invested, 2),
        current_value=round(total_current_value, 2),
        total_pnl=portfolio_pnl,
        total_pnl_pct=portfolio_pnl_pct,
        health_score=health_score,
        health_status=health_status,
        holdings_count=len(holdings_items),
        holdings=holdings_items
    )

def add_user_holding(user_id: str, req: AddHoldingRequest) -> HoldingItem:
    """Add a new mutual fund holding to the user's portfolio."""
    data = _load_portfolios()
    user_key = str(user_id).strip()
    if user_key not in data:
        data[user_key] = {"holdings": [], "watchlist": []}

    # Resolve purchase NAV if not provided
    purchase_nav = req.purchase_nav
    try:
        stats = get_fund_stats(req.fund_name)
        current_nav = stats.display_nav
        category = stats.category
        signal = stats.signal or "HOLD"
        conviction = stats.conviction_score or 50.0
    except Exception:
        current_nav = 100.0
        category = "Equity"
        signal = "HOLD"
        conviction = 50.0

    if not purchase_nav or purchase_nav <= 0.0:
        purchase_nav = current_nav

    invest_amount = float(req.invest_amount)
    units = round(invest_amount / max(purchase_nav, 0.01), 4)
    invest_date = req.invest_date.strip() if req.invest_date and req.invest_date.strip() else datetime.now().strftime("%Y-%m-%d")
    holding_id = str(uuid.uuid4())

    new_entry = {
        "id": holding_id,
        "fund_name": req.fund_name.strip(),
        "invest_amount": invest_amount,
        "purchase_nav": purchase_nav,
        "units": units,
        "invest_date": invest_date
    }

    data[user_key]["holdings"].append(new_entry)
    _save_portfolios(data)

    current_value = round(units * current_nav, 2)
    pnl = round(current_value - invest_amount, 2)
    pnl_pct = round((pnl / max(invest_amount, 1.0)) * 100.0, 2)

    return HoldingItem(
        id=holding_id,
        fund_name=req.fund_name.strip(),
        category=category,
        invest_date=invest_date,
        invest_amount=invest_amount,
        purchase_nav=purchase_nav,
        units=units,
        current_nav=current_nav,
        current_value=current_value,
        total_pnl=pnl,
        total_pnl_pct=pnl_pct,
        signal=signal,
        conviction_score=conviction
    )

def delete_user_holding(user_id: str, holding_id: str) -> bool:
    """Delete a holding from user's portfolio."""
    data = _load_portfolios()
    user_key = str(user_id).strip()
    if user_key not in data:
        return False

    orig_len = len(data[user_key].get("holdings", []))
    data[user_key]["holdings"] = [h for h in data[user_key].get("holdings", []) if h.get("id") != holding_id]
    if len(data[user_key]["holdings"]) < orig_len:
        _save_portfolios(data)
        return True
    return False

def get_user_watchlist(user_id: str) -> WatchlistResponse:
    """Get all watchlisted mutual funds with live metrics and AI recommendations."""
    entry = _get_user_entry(user_id)
    fund_names = entry.get("watchlist", [])

    items: List[WatchlistItem] = []
    for name in fund_names:
        try:
            stats = get_fund_stats(name)
            items.append(
                WatchlistItem(
                    fund_name=name,
                    category=stats.category,
                    current_nav=stats.display_nav,
                    ret_1y=stats.ret_1y,
                    sharpe=stats.sharpe_val,
                    sortino=stats.sortino_val or 0.0,
                    signal=stats.signal or "HOLD",
                    conviction_score=stats.conviction_score or 50.0
                )
            )
        except Exception:
            items.append(
                WatchlistItem(
                    fund_name=name,
                    category="Mutual Fund",
                    current_nav=100.0,
                    ret_1y=0.0,
                    sharpe=1.0,
                    sortino=1.0,
                    signal="HOLD",
                    conviction_score=50.0
                )
            )

    return WatchlistResponse(items=items, total_count=len(items))

def toggle_user_watchlist(user_id: str, fund_name: str) -> Dict[str, Any]:
    """Toggle a fund in or out of the user's watchlist."""
    data = _load_portfolios()
    user_key = str(user_id).strip()
    if user_key not in data:
        data[user_key] = {"holdings": [], "watchlist": []}

    watchlist = data[user_key].get("watchlist", [])
    clean_name = fund_name.strip()

    if clean_name in watchlist:
        watchlist.remove(clean_name)
        is_watchlisted = False
    else:
        watchlist.append(clean_name)
        is_watchlisted = True

    data[user_key]["watchlist"] = watchlist
    _save_portfolios(data)

    return {
        "fund_name": clean_name,
        "is_watchlisted": is_watchlisted,
        "total_watchlist_count": len(watchlist)
    }
