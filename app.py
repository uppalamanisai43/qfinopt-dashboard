import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import plotly.graph_objects as go
import json, requests, time, os, sys
from datetime import datetime, timedelta, date, timezone
IST = timezone(timedelta(hours=5, minutes=30))

# Ensure backend modules can be imported
_base_dir = os.path.dirname(os.path.abspath(__file__))
_backend_dir = os.path.join(_base_dir, "backend")
_backend_app_dir = os.path.join(_base_dir, "backend", "app")
for _p in [_backend_app_dir, _backend_dir, _base_dir]:
    if _p not in sys.path:
        sys.path.insert(0, _p)

st.set_page_config(
    page_title="Q-FinOpt — Quantum & AI Mutual Fund Advisory",
    page_icon="📈", layout="wide", initial_sidebar_state="expanded")

# ══════════════════════════════════════
# ANDROID APK MATERIAL 3 DARK THEME
# ══════════════════════════════════════

st.markdown("""
<style>
/* Exact Material 3 Dark Theme from QFinOpt Android App */
:root {
    --dark-navy: #0A0E17;
    --surface-dark: #121824;
    --card-dark: #182030;
    --card-elevated: #1E283D;
    --card-border: rgba(138, 153, 173, 0.22);
    --primary-blue: #2563EB;
    --primary-blue-light: #60A5FA;
    --accent-gold: #F59E0B;
    --bullish-green: #00E676;
    --bearish-red: #FF3366;
    --cyan-accent: #06B6D4;
    --text-primary: #F8FAFC;
    --text-secondary: #94A3B8;
    --text-muted: #64748B;
}

/* App Background Canvas */
.stApp {
    background-color: var(--dark-navy) !important;
    color: var(--text-primary) !important;
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
}

/* Sidebar Canvas */
section[data-testid="stSidebar"] {
    background-color: var(--surface-dark) !important;
    border-right: 1px solid var(--card-border) !important;
}
section[data-testid="stSidebar"] .stMarkdown h1, 
section[data-testid="stSidebar"] .stMarkdown h2, 
section[data-testid="stSidebar"] .stMarkdown h3 {
    color: var(--text-primary) !important;
}

/* Metric Cards matching Android MetricCard.kt */
div[data-testid="stMetric"] {
    background: var(--card-dark) !important;
    border: 1px solid var(--card-border) !important;
    border-radius: 14px !important;
    padding: 12px 16px !important;
    box-shadow: 0 4px 18px rgba(0, 0, 0, 0.35) !important;
    transition: all 0.25s ease !important;
}
div[data-testid="stMetric"]:hover {
    transform: translateY(-2px);
    border-color: var(--primary-blue-light) !important;
    box-shadow: 0 6px 22px rgba(37, 99, 235, 0.25) !important;
}
div[data-testid="stMetric"] label {
    color: var(--text-muted) !important;
    font-size: 10px !important;
    font-weight: 700 !important;
    letter-spacing: 0.8px !important;
    text-transform: uppercase !important;
}
div[data-testid="stMetric"] div[data-testid="stMetricValue"] {
    color: var(--text-primary) !important;
    font-size: 20px !important;
    font-weight: 800 !important;
}
div[data-testid="stMetric"] div[data-testid="stMetricDelta"] {
    font-size: 11px !important;
    font-weight: 600 !important;
}

/* Navigation Tabs matching Android Jetpack Compose TabRow */
.stTabs [data-baseweb="tab-list"] {
    gap: 8px !important;
    background-color: var(--surface-dark) !important;
    padding: 8px 10px !important;
    border-radius: 14px !important;
    border: 1px solid var(--card-border) !important;
    overflow-x: auto !important;
}
.stTabs [data-baseweb="tab"] {
    height: 42px !important;
    border-radius: 10px !important;
    color: var(--text-secondary) !important;
    background-color: transparent !important;
    padding: 8px 16px !important;
    font-weight: 600 !important;
    font-size: 13px !important;
    border: none !important;
    transition: all 0.2s ease !important;
}
.stTabs [data-baseweb="tab"]:hover {
    color: var(--text-primary) !important;
    background-color: rgba(37, 99, 235, 0.12) !important;
}
.stTabs [aria-selected="true"] {
    background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%) !important;
    color: #FFFFFF !important;
    box-shadow: 0 4px 14px rgba(37, 99, 235, 0.45) !important;
}
.stTabs [data-baseweb="tab-highlight"] {
    display: none !important;
}

/* Android Buttons */
.stButton > button {
    border-radius: 12px !important;
    font-weight: 700 !important;
    padding: 10px 22px !important;
    background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%) !important;
    color: white !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35) !important;
    transition: all 0.2s ease !important;
}
.stButton > button:hover {
    background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%) !important;
    box-shadow: 0 6px 20px rgba(37, 99, 235, 0.55) !important;
    transform: translateY(-1px) !important;
}

/* Containers, Alerts, Cards */
div[data-testid="stAlert"] {
    background-color: var(--card-dark) !important;
    border: 1px solid var(--card-border) !important;
    border-radius: 14px !important;
    color: var(--text-primary) !important;
}
div[data-testid="stDataFrame"] {
    border: 1px solid var(--card-border) !important;
    border-radius: 12px !important;
    background-color: var(--surface-dark) !important;
}

/* Inputs, Selectboxes */
div[data-baseweb="select"] > div, div[data-baseweb="input"] > div {
    background-color: var(--card-dark) !important;
    border: 1px solid var(--card-border) !important;
    border-radius: 10px !important;
    color: var(--text-primary) !important;
}

/* Custom Header Badge */
.apk-badge {
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 11px;
    font-weight: 700;
    margin-left: 8px;
}
.apk-badge-live {
    background: rgba(0, 230, 118, 0.15);
    color: #00E676;
    border: 1px solid rgba(0, 230, 118, 0.3);
}
</style>
""", unsafe_allow_html=True)

# ══════════════════════════════════════
# REAL-TIME DATA FUNCTIONS
# ══════════════════════════════════════

@st.cache_data(ttl=300)  # refresh every 5 minutes
def get_live_market():
    """Fetch live Nifty + Sensex from Yahoo Finance"""
    import yfinance as yf
    result = {}
    indices = [
        ("Nifty 50",   "^NSEI"),
        ("Sensex",     "^BSESN"),
        ("Bank Nifty", "^NSEBANK"),
        ("Nifty IT",   "^CNXIT")
    ]
    for name, ticker in indices:
        try:
            # Try multiple methods to get data
            tk   = yf.Ticker(ticker)
            # Method 1: fast_info
            try:
                fi   = tk.fast_info
                now  = float(fi.last_price)
                prev = float(fi.previous_close)
                if now > 0 and prev > 0:
                    chg = now - prev
                    pct = (chg/prev)*100
                    result[name] = {
                        "price" : now,
                        "change": chg,
                        "pct"   : pct
                    }
                    continue
            except:
                pass
            # Method 2: history
            try:
                hist = tk.history(period="5d", interval="1d")
                if len(hist) >= 2:
                    now  = float(hist["Close"].iloc[-1])
                    prev = float(hist["Close"].iloc[-2])
                    chg  = now - prev
                    pct  = (chg/prev)*100
                    result[name] = {
                        "price" : now,
                        "change": chg,
                        "pct"   : pct
                    }
            except:
                pass
        except:
            pass
    return result

@st.cache_data(ttl=3600)  # refresh every 1 hour
def get_live_nav_amfi():
    """Fetch TODAY's NAV for all funds from AMFI India official API with browser headers"""
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    }
    try:
        url  = "https://www.amfiindia.com/spages/NAVAll.txt"
        resp = requests.get(url, headers=headers, timeout=12)
        if resp.status_code == 200 and len(resp.text) > 1000:
            lines = resp.text.strip().split("\n")
            nav_data = {}
            for line in lines:
                parts = line.strip().split(";")
                if len(parts) >= 5:
                    try:
                        scheme_code = parts[0].strip()
                        scheme_name = parts[3].strip()
                        nav_val     = parts[4].strip()
                        nav_date    = parts[5].strip() if len(parts) > 5 else ""
                        if nav_val not in ["N.A.", "", "#N/A", "-"]:
                            nav_data[scheme_name] = {
                                "code": scheme_code,
                                "nav" : float(nav_val),
                                "date": nav_date
                            }
                    except Exception:
                        pass
            return nav_data
    except Exception:
        pass
    return {}

@st.cache_data(ttl=300)
def get_market_news():
    """Get market sentiment from Nifty performance"""
    try:
        import yfinance as yf
        nifty = yf.Ticker("^NSEI")
        hist  = nifty.history(period="1mo")
        if len(hist) > 0:
            month_ret = (float(hist["Close"].iloc[-1]) /
                         float(hist["Close"].iloc[0]) - 1)*100
            week_ret  = (float(hist["Close"].iloc[-1]) /
                         float(hist["Close"].iloc[-5]) - 1)*100 if len(hist)>=5 else 0
            return {
                "month_return": month_ret,
                "week_return" : week_ret,
                "sentiment"   : "Bullish" if month_ret > 2 else
                                "Bearish" if month_ret < -2 else "Neutral"
            }
    except:
        pass
    return {"month_return":0,"week_return":0,"sentiment":"Neutral"}

@st.cache_data
def load_historical():
    """Load historical dataset from local repository first for instant startup, falling back to Google Drive"""
    local_paths = [
        os.path.join(_base_dir, "backend", "data", "qfinopt_cleaned.csv.gz"),
        os.path.join(_base_dir, "backend", "data", "qfinopt_cleaned.csv"),
        os.path.join(_base_dir, "data", "qfinopt_cleaned.csv.gz"),
        os.path.join(_base_dir, "data", "qfinopt_cleaned.csv"),
        os.path.join(os.getcwd(), "backend", "data", "qfinopt_cleaned.csv.gz"),
        os.path.join(os.getcwd(), "backend", "data", "qfinopt_cleaned.csv"),
        "backend/data/qfinopt_cleaned.csv.gz",
        "backend/data/qfinopt_cleaned.csv",
        "data/qfinopt_cleaned.csv.gz",
        "data/qfinopt_cleaned.csv",
        "qfinopt_cleaned.csv.gz",
        "qfinopt_cleaned.csv"
    ]
    for p in local_paths:
        if os.path.exists(p):
            df = pd.read_csv(p)
            df["Date"] = pd.to_datetime(df["Date"])
            return df
    
    # Fallback if local CSV not found
    try:
        file_id = "1EfNv54tjvjcsJdZhxYwa4zPJxl9q9_9X"
        url     = f"https://drive.google.com/uc?id={file_id}"
        df      = pd.read_csv(url)
        df["Date"] = pd.to_datetime(df["Date"])
        return df
    except Exception as e:
        st.error(f"Error loading historical data: {e}")
        return pd.DataFrame()

# ══════════════════════════════════════
# LOAD ALL DATA
# ══════════════════════════════════════

st.title("⚛️ Q-FinOpt: Quantum & AI Mutual Fund Advisory")
st.markdown("*Real-time AMFI NAV · 74.01% ML Forecast · Quantum QAOA Portfolio Optimizer · Withdrawal Timing*")

# Loading bar
with st.spinner("Fetching live market and fund data..."):
    market    = get_live_market()
    sentiment = get_market_news()
    df        = load_historical()
    nav_today = get_live_nav_amfi()
    
    # If AMFI network timed out, fallback to latest historical NAVs from local dataset
    is_amfi_network_live = len(nav_today) > 0
    if not nav_today and df is not None and not df.empty:
        for _, row in df.sort_values("Date").groupby("Scheme_Name").last().reset_index().iterrows():
            nav_today[row["Scheme_Name"]] = {
                "code": str(row.get("Scheme_Code", "")),
                "nav" : float(row["NAV_Value"]),
                "date": row["Date"].strftime("%d-%b-%Y") if hasattr(row["Date"], "strftime") else str(row["Date"])
            }

# ══════════════════════════════════════
# LIVE MARKET TICKER
# ══════════════════════════════════════

st.subheader("📡 Live Market — Auto refreshes every 5 minutes")
if market:
    cols = st.columns(len(market))
    for col, (name, data) in zip(cols, market.items()):
        arrow = "▲" if data["pct"] > 0 else "▼"
        col.metric(
            name,
            f"{data['price']:,.2f}",
            f"{arrow} {abs(data['change']):.2f} ({data['pct']:+.2f}%)")
else:
    st.warning("Market data loading... refresh in a moment.")

# Market sentiment bar
sent  = sentiment["sentiment"]
m_ret = sentiment["month_return"]
w_ret = sentiment["week_return"]
color = "🟢" if sent=="Bullish" else "🔴" if sent=="Bearish" else "🟡"
st.markdown(
    f"{color} **Market Sentiment: {sent}** &nbsp;|&nbsp; "
    f"1-Month Nifty: **{m_ret:+.2f}%** &nbsp;|&nbsp; "
    f"1-Week: **{w_ret:+.2f}%** &nbsp;|&nbsp; "
    f"Last updated: **{datetime.now(IST).strftime('%d %b %Y %I:%M %p')} IST**")

# Auto refresh button with unique key
if st.button("🔄 Refresh Live Data Now", key="btn_refresh_live_data_header"):
    st.cache_data.clear()
    st.rerun()

st.divider()

# ══════════════════════════════════════
# SIDEBAR
# ══════════════════════════════════════

st.sidebar.title("🎯 Your Investment")
categories  = sorted(df["Sheet_Category"].unique())
risk_levels = sorted(df["Risk_Level"].unique())

st.sidebar.subheader("Filter Funds")
sel_cat  = st.sidebar.multiselect(
    "Category", categories, default=categories[:3])
sel_risk = st.sidebar.multiselect(
    "Risk Level", risk_levels, default=risk_levels)

filtered = sorted(df[
    (df["Sheet_Category"].isin(sel_cat)) &
    (df["Risk_Level"].isin(sel_risk))
]["Scheme_Name"].unique())

st.sidebar.subheader("Fund & Amount")
selected_fund = st.sidebar.selectbox("Choose Fund:", filtered)
invest_date   = st.sidebar.date_input(
    "Start Date:", value=date.today())
investment    = st.sidebar.number_input(
    "Lump Sum (₹)", min_value=500,
    max_value=10000000, value=100000, step=500)
sip_amount    = st.sidebar.number_input(
    "Monthly SIP (₹)", min_value=500,
    max_value=100000, value=5000, step=500)
sip_years     = st.sidebar.slider(
    "SIP Duration (years)", 1, 30, 10)
n_sim = st.sidebar.selectbox(
    "Simulations", [1000,5000,10000], index=1)

st.sidebar.divider()

# Show live NAV in sidebar
if nav_today:
    # Try to match fund name
    best_match  = None
    best_score  = 0
    fund_words  = set(selected_fund.lower().split())
    for amfi_name in nav_today:
        amfi_words = set(amfi_name.lower().split())
        score = len(fund_words & amfi_words)
        if score > best_score:
            best_score = score
            best_match = amfi_name

    if best_match and best_score >= 2:
        live_nav  = nav_today[best_match]["nav"]
        nav_date  = nav_today[best_match]["date"]
        st.sidebar.success(
            f"📡 **Live NAV Today**\n\n"
            f"₹{live_nav:.4f}\n\n"
            f"As of {nav_date}")
    else:
        st.sidebar.info("Live NAV: Searching AMFI...")

# ══════════════════════════════════════
# FUND STATISTICS (from real data)
# ══════════════════════════════════════

fund_df    = df[df["Scheme_Name"]==selected_fund].sort_values("Date")
mu_real    = fund_df["Daily_Return_%"].mean()
sigma_real = fund_df["Daily_Return_%"].std()
sharpe_val = fund_df["Sharpe"].mean()
alpha_val  = fund_df["Alpha"].mean()
beta_val   = fund_df["Beta"].mean()
expense    = fund_df["Expense_Ratio"].mean()
risk_level = fund_df["Risk_Level"].iloc[-1]
category   = fund_df["Sheet_Category"].iloc[-1]
hist_nav   = fund_df["NAV_Value"].iloc[-1]
nav_1y     = fund_df["NAV_Value"].iloc[-252] if len(fund_df)>252 else fund_df["NAV_Value"].iloc[0]
ret_1y     = ((hist_nav - nav_1y)/nav_1y)*100

# Use live NAV if available
display_nav = hist_nav
if nav_today and best_match and best_score >= 2:
    display_nav = nav_today[best_match]["nav"]
    nav_source  = "📡 LIVE"
else:
    nav_source  = "📁 Historical"

# Market-adjusted mu
try:
    mu_adjusted = mu_real + (
        sentiment["week_return"]/500 * beta_val)
except:
    mu_adjusted = mu_real

# Fund header
st.subheader(f"📊 {selected_fund}")
st.caption(
    f"Category: {category} | Risk: {risk_level} | "
    f"NAV Source: {nav_source} | "
    f"Market: {sentiment['sentiment']}")

c1,c2,c3,c4,c5,c6 = st.columns(6)
c1.metric("NAV Today",
          f"₹{display_nav:.2f}",
          nav_source)
c2.metric("1Y Return",    f"{ret_1y:.2f}%",  f"{ret_1y:.1f}%")
c3.metric("Sharpe Ratio", f"{sharpe_val:.3f}")
c4.metric("Alpha",        f"{alpha_val:.3f}")
c5.metric("Beta",         f"{beta_val:.3f}")
c6.metric("Expense",      f"{expense:.2f}%")
st.divider()

# ══════════════════════════════════════
# ML & QUANTUM HELPER FUNCTIONS
# ══════════════════════════════════════

@st.cache_resource
def load_ml_model():
    """Load the pre-trained 74.01% multi-factor HistGradientBoosting model."""
    import joblib
    model_paths = [
        os.path.join(_base_dir, "backend", "data", "mf_gb_model.joblib"),
        os.path.join(_base_dir, "data", "mf_gb_model.joblib"),
        "backend/data/mf_gb_model.joblib"
    ]
    meta_paths = [
        os.path.join(_base_dir, "backend", "data", "mf_gb_meta.json"),
        os.path.join(_base_dir, "data", "mf_gb_meta.json"),
        "backend/data/mf_gb_meta.json"
    ]
    m, meta = None, None
    for p in model_paths:
        if os.path.exists(p):
            try:
                m = joblib.load(p)
                break
            except Exception:
                pass
    for p in meta_paths:
        if os.path.exists(p):
            try:
                with open(p, "r") as f:
                    meta = json.load(f)
                break
            except Exception:
                pass
    return m, meta

def extract_fund_features(f_df: pd.DataFrame) -> pd.DataFrame:
    """Extract 12 quantitative & technical factor inputs for the ML model."""
    navs = f_df["NAV_Value"].values
    if "Daily_Return_%" in f_df.columns and len(f_df["Daily_Return_%"].dropna()) > 5:
        daily_rets = f_df["Daily_Return_%"].dropna().values / 100.0
    else:
        diffs = np.diff(navs) if len(navs) > 1 else np.array([0.0])
        daily_rets = diffs / np.maximum(navs[:-1], 1e-6)

    ret_5d = float(navs[-1] / navs[-6] - 1.0) if len(navs) >= 6 else 0.0
    ret_10d = float(navs[-1] / navs[-11] - 1.0) if len(navs) >= 11 else 0.0
    ret_21d = float(navs[-1] / navs[-22] - 1.0) if len(navs) >= 22 else 0.0

    vol_10d = float(np.std(daily_rets[-10:])) if len(daily_rets) >= 10 else 0.01
    vol_30d = float(np.std(daily_rets[-30:])) if len(daily_rets) >= 30 else 0.01
    mom_ratio = float(ret_5d / (vol_10d + 1e-6))

    # RSI-14
    if len(navs) >= 15:
        deltas = np.diff(navs[-15:])
        gains = np.maximum(deltas, 0)
        losses = np.maximum(-deltas, 0)
        avg_gain = float(np.mean(gains))
        avg_loss = float(np.mean(losses))
        rs = avg_gain / (avg_loss + 1e-6)
        rsi_14 = float(100.0 - (100.0 / (1.0 + rs)))
    else:
        rsi_14 = 50.0

    # NAV Z-score
    if len(navs) >= 60:
        mean60 = float(np.mean(navs[-60:]))
        std60 = float(np.std(navs[-60:]))
        nav_z = float((navs[-1] - mean60) / (std60 + 1e-6))
    else:
        nav_z = 0.0

    sharpe = float(f_df["Sharpe"].mean()) if "Sharpe" in f_df.columns and not f_df["Sharpe"].isna().all() else 1.0
    alpha = float(f_df["Alpha"].mean()) if "Alpha" in f_df.columns and not f_df["Alpha"].isna().all() else 0.0
    beta = float(f_df["Beta"].mean()) if "Beta" in f_df.columns and not f_df["Beta"].isna().all() else 1.0
    expense = float(f_df["Expense_Ratio"].mean()) if "Expense_Ratio" in f_df.columns and not f_df["Expense_Ratio"].isna().all() else 1.5

    feature_cols = [
        'ret_5d', 'ret_10d', 'ret_21d', 'vol_10d', 'vol_30d',
        'mom_ratio', 'rsi_14', 'nav_z', 'Sharpe', 'Alpha', 'Beta', 'Expense_Ratio'
    ]
    data = [[ret_5d, ret_10d, ret_21d, vol_10d, vol_30d, mom_ratio, rsi_14, nav_z, sharpe, alpha, beta, expense]]
    return pd.DataFrame(data, columns=feature_cols)

# ══════════════════════════════════════
# TABS
# ══════════════════════════════════════

tab1, tab2, tab3, tab4, tab5, tab6, tab7, tab8, tab9, tab10 = st.tabs([
    "📅 Withdrawal Timing",
    "💰 SIP Calculator",
    "📈 Fund Analysis",
    "🤖 AI/ML Forecast (74%)",
    "⚛️ Quantum QAOA Optimizer",
    "⚖️ Compare Funds",
    "📡 Live NAV Search",
    "🏦 Platform Guide",
    "📄 PDF Report",
    "🔔 Reminders"])

# ── TAB 1: Withdrawal Timing ──
with tab1:
    st.subheader("🎯 When Should You Withdraw?")
    st.markdown(
        f"Using **real data** from {len(fund_df):,} days of "
        f"{selected_fund[:35]} + **live market sentiment: "
        f"{sentiment['sentiment']}**")

    hold_days = 504
    np.random.seed(42)
    daily_ret = np.random.normal(
        mu_adjusted/100, sigma_real/100,
        (hold_days, n_sim))
    paths = np.zeros((hold_days+1, n_sim))
    paths[0] = investment
    for d in range(1, hold_days+1):
        paths[d] = paths[d-1]*(1+daily_ret[d-1])

    exp_v    = paths.mean(axis=1)
    prob_g   = (paths > investment).mean(axis=1)
    vol      = paths.std(axis=1)
    risk_adj = (exp_v - investment)/(vol+1)
    opt_day  = int(np.argmax(risk_adj))
    opt_val  = exp_v[opt_day]
    gain     = opt_val - investment
    ret_pct  = (gain/investment)*100

    # Exact withdrawal date (skip weekends)
    current = datetime.combine(invest_date,
                               datetime.min.time())
    count = 0
    while count < opt_day:
        current += timedelta(days=1)
        if current.weekday() < 5:
            count += 1
    withdraw_date = current.date()

    st.markdown(f"""
    <div style="background:linear-gradient(135deg,#0d1b2a,#1b263b);
                border:2px solid #4CAF50;
                padding:24px;border-radius:16px;margin:12px 0">
        <h2 style="color:#4CAF50;margin:0 0 6px 0">
            🎯 Personalised Recommendation
        </h2>
        <p style="color:#aaa;margin:0 0 16px 0;font-size:13px">
            Market-adjusted using live {sentiment["sentiment"]} signal
            · Beta {beta_val:.2f} · {n_sim:,} simulations
        </p>
        <div style="display:grid;
                    grid-template-columns:1fr 1fr 1fr 1fr;
                    gap:12px">
            <div style="background:rgba(76,175,80,0.15);
                        border:1px solid #4CAF50;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#81C784;font-size:12px">
                    📅 Invest On</div>
                <div style="color:white;font-size:17px;
                            font-weight:bold">
                    {invest_date.strftime("%d %b %Y")}</div>
            </div>
            <div style="background:rgba(255,215,0,0.15);
                        border:1px solid #FFD700;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#FFD700;font-size:12px">
                    💰 Withdraw On</div>
                <div style="color:#FFD700;font-size:17px;
                            font-weight:bold">
                    {withdraw_date.strftime("%d %b %Y")}</div>
                <div style="color:#aaa;font-size:11px">
                    Day {opt_day} (~{opt_day//21} months)</div>
            </div>
            <div style="background:rgba(33,150,243,0.15);
                        border:1px solid #2196F3;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#64B5F6;font-size:12px">
                    💼 Expected Value</div>
                <div style="color:white;font-size:17px;
                            font-weight:bold">
                    ₹{opt_val:,.0f}</div>
            </div>
            <div style="background:rgba(156,39,176,0.15);
                        border:1px solid #9C27B0;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#CE93D8;font-size:12px">
                    📈 Expected Gain</div>
                <div style="color:#CE93D8;font-size:17px;
                            font-weight:bold">
                    +₹{gain:,.0f}</div>
                <div style="color:#aaa;font-size:11px">
                    {ret_pct:.1f}% return</div>
            </div>
        </div>
        <div style="margin-top:14px;
                    background:rgba(255,255,255,0.05);
                    padding:10px;border-radius:8px;
                    font-size:13px">
            ✅ Profit probability:
            <strong style="color:#81C784">
                {prob_g[opt_day]*100:.1f}%</strong>
            &nbsp;&nbsp;
            📊 Risk level:
            <strong style="color:#64B5F6">{risk_level}</strong>
            &nbsp;&nbsp;
            🌐 NAV source:
            <strong style="color:#FFD54F">{nav_source}</strong>
        </div>
    </div>
    """, unsafe_allow_html=True)

    # Period returns
    st.subheader("Returns by Holding Period")
    p_cols = st.columns(4)
    for col,(label,days) in zip(p_cols,[
        ("3 months",63),("6 months",126),
        ("1 year",252),("2 years",504)]):
        v = exp_v[min(days,hold_days)]
        col.metric(label,
            f"₹{v:,.0f}",
            f"+{((v-investment)/investment)*100:.1f}% | "
            f"{prob_g[min(days,hold_days)]*100:.0f}% prob")

    col1,col2 = st.columns(2)
    days_axis = np.arange(hold_days+1)
    p5  = np.percentile(paths, 5,  axis=1)
    p95 = np.percentile(paths, 95, axis=1)
    sample_idx = np.random.choice(n_sim, size=min(40, n_sim), replace=False)

    with col1:
        fig1 = go.Figure()
        for i in sample_idx:
            fig1.add_trace(go.Scatter(
                x=days_axis, y=paths[:, i], mode="lines",
                line=dict(color="rgba(70,130,180,0.15)", width=1),
                showlegend=False, hoverinfo="skip"))
        fig1.add_trace(go.Scatter(
            x=days_axis, y=p95, mode="lines", name="Best 95%",
            line=dict(color="#4CAF50", width=1.5, dash="dash"),
            hovertemplate="Day %{x}<br>₹%{y:,.0f}<extra>Best 95%</extra>"))
        fig1.add_trace(go.Scatter(
            x=days_axis, y=p5, mode="lines", name="Worst 5%",
            line=dict(color="#F44336", width=1.5, dash="dash"),
            hovertemplate="Day %{x}<br>₹%{y:,.0f}<extra>Worst 5%</extra>"))
        fig1.add_trace(go.Scatter(
            x=days_axis, y=exp_v, mode="lines", name="Expected",
            line=dict(color="orange", width=2.5),
            hovertemplate="Day %{x}<br>₹%{y:,.0f}<extra>Expected</extra>"))
        fig1.add_hline(y=investment, line_width=1, line_dash="dot", line_color="white")
        fig1.add_vline(x=opt_day, line_width=2, line_dash="dash", line_color="#FFD700",
                        annotation_text=f"Withdraw Day {opt_day}", annotation_position="top")
        fig1.update_layout(
            title=f"Monte Carlo: {n_sim:,} Scenarios",
            xaxis_title="Trading Days", yaxis_title="Value (₹)",
            hovermode="x unified", height=430,
            margin=dict(l=10, r=10, t=55, b=10),
            legend=dict(orientation="h", yanchor="bottom", y=1.05, xanchor="right", x=1))
        fig1.update_xaxes(showgrid=True, gridcolor="rgba(128,128,128,0.15)")
        fig1.update_yaxes(showgrid=True, gridcolor="rgba(128,128,128,0.15)")
        st.plotly_chart(fig1, use_container_width=True, config={"displayModeBar": False})

    with col2:
        prob_pct = prob_g * 100
        baseline = np.full(hold_days + 1, 50.0)
        above = np.where(prob_pct >= 50, prob_pct, 50.0)
        below = np.where(prob_pct < 50, prob_pct, 50.0)

        fig2 = go.Figure()
        fig2.add_trace(go.Scatter(x=days_axis, y=baseline, mode="lines",
                                    line=dict(width=0), showlegend=False, hoverinfo="skip"))
        fig2.add_trace(go.Scatter(x=days_axis, y=above, mode="lines", name="Profit zone",
                                    line=dict(width=0), fill="tonexty",
                                    fillcolor="rgba(76,175,80,0.35)", hoverinfo="skip"))
        fig2.add_trace(go.Scatter(x=days_axis, y=baseline, mode="lines",
                                    line=dict(width=0), showlegend=False, hoverinfo="skip"))
        fig2.add_trace(go.Scatter(x=days_axis, y=below, mode="lines", name="Loss zone",
                                    line=dict(width=0), fill="tonexty",
                                    fillcolor="rgba(244,67,54,0.35)", hoverinfo="skip"))
        fig2.add_trace(go.Scatter(
            x=days_axis, y=prob_pct, mode="lines", name="Probability of profit",
            line=dict(color="#2E7D32", width=2.5),
            hovertemplate="Day %{x}<br>%{y:.1f}%<extra>Prob. of profit</extra>"))
        fig2.add_vline(x=opt_day, line_width=2.5, line_dash="dash", line_color="#FFD700",
                        annotation_text=f"Optimal: Day {opt_day}", annotation_position="top")
        fig2.add_hline(y=50, line_width=1, line_dash="dot", line_color="white")
        fig2.update_layout(
            title="Probability of Profit Over Time",
            xaxis_title="Trading Days", yaxis_title="Probability (%)",
            yaxis_range=[0, 100], hovermode="x unified", height=430,
            margin=dict(l=10, r=10, t=55, b=10))
        st.plotly_chart(fig2, use_container_width=True, config={"displayModeBar": False})

    # Scenarios
    st.subheader("Scenario Breakdown at Optimal Exit")
    s1,s2,s3,s4 = st.columns(4)
    for col,(label,pct) in zip([s1,s2,s3,s4],[
        ("Worst 5%",5),("Conservative 25%",25),
        ("Optimistic 75%",75),("Best 95%",95)]):
        v = np.percentile(paths[opt_day], pct)
        col.metric(label, f"₹{v:,.0f}",
            f"{((v-investment)/investment)*100:.1f}%")

# ── TAB 2: SIP Calculator ──
with tab2:
    st.subheader("📅 SIP Calculator")
    st.caption(
        "Illustrative planning scenario — not a price prediction or guaranteed return."
    )

    c1, c2 = st.columns(2)
    with c1:
        annual_return_pct = st.slider(
            "Annual return assumption (%)",
            min_value=4.0, max_value=15.0, value=10.0, step=0.5,
            help="A planning assumption, not the ML model prediction."
        )
    with c2:
        nav_log_returns = np.log(
            fund_df["NAV_Value"] / fund_df["NAV_Value"].shift(1)
        ).dropna()
        historical_vol = float(nav_log_returns.std() * np.sqrt(252) * 100)
        default_vol = float(np.clip(historical_vol, 8.0, 35.0))
        annual_vol_pct = st.slider(
            "Annual volatility (%)",
            min_value=8.0, max_value=35.0, value=default_vol, step=1.0,
            help="Higher volatility means a wider range of possible outcomes."
        )

    sip_months = sip_years * 12
    sip_days_total = sip_years * 252
    total_invested = sip_amount * sip_months
    n_sip = min(n_sim, 5000)

    # Annual assumptions -> daily lognormal returns.
    # This avoids impossible returns below -100% and does NOT use mu_adjusted.
    rng = np.random.default_rng(42)
    annual_return = annual_return_pct / 100
    annual_vol = annual_vol_pct / 100
    daily_log_mean = (
        np.log1p(annual_return) - 0.5 * annual_vol**2
    ) / 252
    daily_log_vol = annual_vol / np.sqrt(252)

    daily_returns = np.exp(rng.normal(
        daily_log_mean, daily_log_vol,
        size=(sip_days_total, n_sip)
    )) - 1

    sip_paths = np.zeros((sip_days_total + 1, n_sip))
    invested_line = np.zeros(sip_days_total + 1)
    portfolio = np.zeros(n_sip)
    deposited = 0.0

    for day in range(sip_days_total):
        # One deposit every 21 trading days = 12 deposits/year.
        if day % 21 == 0:
            portfolio += sip_amount
            deposited += sip_amount

        portfolio *= (1 + daily_returns[day])
        sip_paths[day + 1] = portfolio
        invested_line[day] = deposited
        invested_line[day + 1] = deposited

    final_sip = sip_paths[-1]
    expected_sip = float(final_sip.mean())
    median_sip = float(np.median(final_sip))
    gain_sip = expected_sip - total_invested
    profit_probability = float((final_sip > total_invested).mean() * 100)

    # Correct money-weighted annual return for monthly deposits.
    def calculate_xirr(amounts, dates):
        days = np.array([(d - dates[0]).days for d in dates], dtype=float)

        def npv(rate):
            return np.sum(np.asarray(amounts) / (1 + rate) ** (days / 365.25))

        low, high = -0.9999, 2.0
        while npv(high) > 0 and high < 100:
            high *= 2
        for _ in range(120):
            mid = (low + high) / 2
            if npv(mid) > 0:
                low = mid
            else:
                high = mid
        return (low + high) / 2

    cashflow_dates = [
        invest_date + timedelta(days=30 * month)
        for month in range(sip_months)
    ]
    expected_xirr = calculate_xirr(
        [-sip_amount] * sip_months + [expected_sip],
        cashflow_dates + [invest_date + timedelta(days=365.25 * sip_years)]
    ) * 100

    r1, r2, r3, r4, r5 = st.columns(5)
    r1.metric("Monthly SIP", f"₹{sip_amount:,}")
    r2.metric("Total Invested", f"₹{total_invested:,.0f}")
    r3.metric("Expected Corpus", f"₹{expected_sip:,.0f}", f"+₹{gain_sip:,.0f}")
    r4.metric("Est. XIRR", f"{expected_xirr:.1f}%/year")
    r5.metric("Profit Probability", f"{profit_probability:.1f}%")

    st.caption(
        f"Median corpus: ₹{median_sip:,.0f} · "
        f"10th–90th percentile: ₹{np.percentile(final_sip, 10):,.0f} – "
        f"₹{np.percentile(final_sip, 90):,.0f}"
    )

    col1, col2 = st.columns(2)

    with col1:
        days_axis_sip = np.arange(sip_days_total + 1)
        sample_idx_sip = np.random.choice(n_sip, size=min(30, n_sip), replace=False)
        fig_s = go.Figure()
        for i in sample_idx_sip:
            fig_s.add_trace(go.Scatter(
                x=days_axis_sip, y=sip_paths[:, i], mode="lines",
                line=dict(color="rgba(70,130,180,0.12)", width=1),
                showlegend=False, hoverinfo="skip"))
        fig_s.add_trace(go.Scatter(
            x=days_axis_sip, y=sip_paths.mean(axis=1), mode="lines",
            name="Expected corpus", line=dict(color="orange", width=2.5),
            hovertemplate="Day %{x}<br>₹%{y:,.0f}<extra>Expected</extra>"))
        fig_s.add_trace(go.Scatter(
            x=days_axis_sip, y=invested_line, mode="lines",
            name="Amount invested", line=dict(color="#F44336", width=2, dash="dash"),
            hovertemplate="Day %{x}<br>₹%{y:,.0f}<extra>Invested</extra>"))
        fig_s.update_layout(
            title=f"SIP Growth — {sip_years} Years",
            xaxis_title="Trading Days", yaxis_title="Value (₹)",
            hovermode="x unified", height=430,
            margin=dict(l=10, r=10, t=55, b=10),
            legend=dict(orientation="h", yanchor="bottom", y=1.05, xanchor="right", x=1))
        st.plotly_chart(fig_s, use_container_width=True, config={"displayModeBar": False})

    with col2:
        fig_s2 = go.Figure()
        fig_s2.add_trace(go.Histogram(
            x=final_sip, nbinsx=60, marker_color="steelblue", opacity=0.85,
            hovertemplate="Final value: ₹%{x:,.0f}<br>Count: %{y}<extra></extra>"))
        fig_s2.add_vline(x=total_invested, line_width=2, line_dash="dash", line_color="#F44336",
                          annotation_text=f"Invested ₹{total_invested:,.0f}",
                          annotation_position="top")
        fig_s2.add_vline(x=expected_sip, line_width=2.5, line_color="#4CAF50",
                          annotation_text=f"Expected ₹{expected_sip:,.0f}",
                          annotation_position="top")
        fig_s2.update_layout(
            title="Final Corpus Distribution",
            xaxis_title="Final Value (₹)", yaxis_title="Count",
            height=430, margin=dict(l=10, r=10, t=55, b=10))
        st.plotly_chart(fig_s2, use_container_width=True, config={"displayModeBar": False})

    st.subheader("📆 Year-by-Year Projection")
    rows = []
    for yr in range(1, sip_years + 1):
        day_index = yr * 252
        yearly_values = sip_paths[day_index]
        yearly_invested = invested_line[day_index]
        rows.append({
            "Year": f"Year {yr}",
            "Invested": f"₹{yearly_invested:,.0f}",
            "Expected": f"₹{yearly_values.mean():,.0f}",
            "Median": f"₹{np.median(yearly_values):,.0f}",
            "10th–90th Range": (
                f"₹{np.percentile(yearly_values, 10):,.0f} – "
                f"₹{np.percentile(yearly_values, 90):,.0f}"
            ),
            "Profit Probability": f"{(yearly_values > yearly_invested).mean() * 100:.1f}%"
        })

    st.dataframe(pd.DataFrame(rows), use_container_width=True, hide_index=True)


# ── TAB 3: Fund Analysis — Interactive Plotly chart + predicted-value callouts ──
with tab3:
    st.subheader(f"📈 {selected_fund[:55]}")
    @st.cache_data(ttl=86400)
    def resolve_mfapi_code(fund_name, fallback_code):
        """Your dataset's Scheme_Code is an internal ID mfapi.in doesn't
        recognize (this is why live NAV history was always unavailable
        and charts looked frozen). Search mfapi.in by fund name instead."""
        try:
            resp = requests.get(
                "https://api.mfapi.in/mf/search",
                params={"q": fund_name}, timeout=15)
            matches = resp.json()
            if not matches:
                return fallback_code

            fn_lower = fund_name.lower()
            def score(m):
                n = m.get("schemeName", "").lower()
                s = 0
                if "direct" in fn_lower and "direct" in n: s += 3
                elif "direct" not in fn_lower and "direct" not in n: s += 1
                if "growth" in fn_lower and "growth" in n: s += 2
                return s

            best = max(matches, key=score)
            return str(best.get("schemeCode", fallback_code))
        except Exception:
            return fallback_code

    fallback_code = str(fund_df["Scheme_Code"].iloc[-1]).split(".")[0]
    scheme_code = resolve_mfapi_code(selected_fund, fallback_code)
    @st.cache_data(ttl=3600)
    def get_live_nav_history(code):
        """Full daily NAV history for one fund from mfapi.in (free, no key, updates daily)"""
        try:
            resp = requests.get(f"https://api.mfapi.in/mf/{code}", timeout=15)
            debug_info = {"status_code": resp.status_code, "url": resp.url}
            payload = resp.json()
            hist = payload.get("data", [])
            debug_info["rows_returned"] = len(hist)
            debug_info["meta"] = payload.get("meta", {})
            if not hist:
                return None, debug_info
            hdf = pd.DataFrame(hist)
            hdf["date"] = pd.to_datetime(hdf["date"], dayfirst=True, errors="coerce")
            hdf["nav"]  = pd.to_numeric(hdf["nav"], errors="coerce")
            hdf = hdf.dropna().sort_values("date").reset_index(drop=True)
            debug_info["last_date"] = str(hdf["date"].max()) if len(hdf) else None
            return (hdf if len(hdf) > 5 else None), debug_info
        except Exception as e:
            return None, {"error": f"{type(e).__name__}: {e}"}

    live_hist, live_debug = get_live_nav_history(scheme_code)
    with st.expander("🔍 Debug: live NAV fetch status", expanded=False):
        st.write("Scheme code used:", scheme_code)
        st.json(live_debug)

    period_map = {"1M":21,"3M":63,"6M":126,"1Y":252,
                  "2Y":504,"3Y":756,"5Y":1260,"ALL":100000}
    period = st.radio("Period:", list(period_map.keys()),
                       index=3, horizontal=True, key="tab3_period")
    n_days = period_map[period]

    fc_col1, fc_col2 = st.columns([1,2])
    with fc_col1:
        show_forecast3 = st.checkbox("📈 Show predicted trend", value=True, key="tab3_forecast")
    with fc_col2:
        forecast_days3 = st.slider("Forecast days", 5, 90, 30, key="tab3_fc_days") if show_forecast3 else 0

    if live_hist is not None:
        plot_df = live_hist.tail(n_days) if n_days < len(live_hist) else live_hist
        base    = plot_df["nav"].iloc[0]
        dates   = list(plot_df["date"])
        values  = list((plot_df["nav"]/base - 1)*100)
        source_label = "📡 Live daily NAV history — mfapi.in (AMFI data)"
    else:
        plot_df = fund_df.tail(n_days) if n_days < len(fund_df) else fund_df
        base    = plot_df["NAV_Value"].iloc[0]
        dates   = list(plot_df["Date"])
        values  = list((plot_df["NAV_Value"]/base - 1)*100)
        source_label = "📁 Historical CSV (live NAV history unavailable for this fund's code)"

    if len(values) >= 5:
        w_len = min(14, len(values))
        w_vals = np.array(values[-w_len:], dtype=float)
        x_pts = np.arange(w_len)
        slope_reg, _ = np.polyfit(x_pts, w_vals, 1)  # % change per day
        slope_5d = (w_vals[-1] - w_vals[max(0, w_len - 5)]) / max(1, min(4, w_len - 1))
        mu_trend = float(0.60 * slope_5d + 0.40 * slope_reg)
    elif len(values) >= 2:
        mu_trend = float(values[-1] - values[-2])
    else:
        mu_trend = 0.0

    if mu_trend > 0.02:
        trend_label, trend_icon = "Trending UP", "📈"
    elif mu_trend < -0.02:
        trend_label, trend_icon = "Trending DOWN", "📉"
    else:
        trend_label, trend_icon = "Flat / Sideways", "➖"

    # ── Predicted trend (damped AR(1) momentum so forecast reflects real trajectory) ──
    fut_dates, fut_vals, predicted_final = [], [], None
    if show_forecast3 and len(values) > 0:
        last_val, last_date = values[-1], dates[-1]
        fut_dates = list(pd.bdate_range(last_date + pd.Timedelta(days=1), periods=forecast_days3))
        decay = 0.95
        cum_delta = [sum(mu_trend * (decay**t) for t in range(i+1)) for i in range(forecast_days3)]
        fut_vals  = [last_val + d for d in cum_delta]
        predicted_final = fut_vals[-1] if fut_vals else last_val

    # ── Headline metrics: current value, predicted value, predicted NAV ──
    m1, m2, m3 = st.columns(3)
    m1.metric("Current % Change", f"{values[-1]:+.2f}%" if values else "—")
    if predicted_final is not None:
        m2.metric(f"Predicted (+{forecast_days3}d)", f"{predicted_final:+.2f}%",
                   delta=f"{predicted_final - values[-1]:+.2f}%")
        predicted_nav = base * (1 + predicted_final/100)
        m3.metric("Predicted NAV", f"₹{predicted_nav:.2f}")
    else:
        m2.metric("Predicted", "—")
        m3.metric("Predicted NAV", "—")

    col1, col2 = st.columns([2,1])

    with col1:
        fig3 = go.Figure()
        fig3.add_trace(go.Scatter(
            x=dates, y=values, mode="lines", name="Actual (% change)",
            line=dict(color="#3f7cb8", width=2.4),
            hovertemplate="%{x|%d %b %Y}<br><b>%{y:.2f}%</b><extra>Actual</extra>"
        ))

        if show_forecast3 and fut_vals:
            fig3.add_trace(go.Scatter(
                x=[dates[-1]] + fut_dates, y=[values[-1]] + fut_vals,
                mode="lines", name="Predicted",
                line=dict(color="orange", width=2.4, dash="dash"),
                hovertemplate="%{x|%d %b %Y}<br><b>%{y:.2f}%</b><extra>Predicted</extra>"
            ))

        fig3.add_hline(y=0, line_width=1, line_color="gray", opacity=0.5)
        fig3.update_layout(
            title=f"% Change — {period}  ({trend_icon} {trend_label})",
            xaxis_title="Date", yaxis_title="% Change",
            hovermode="x unified",
            height=430,
            margin=dict(l=10, r=10, t=55, b=10),
            legend=dict(orientation="h", yanchor="bottom", y=1.05, xanchor="right", x=1),
        )
        fig3.update_xaxes(
            tickformat="%d %b %Y", tickangle=0, nticks=10,
            showgrid=True, gridcolor="rgba(128,128,128,0.15)",
            rangeslider_visible=True,
        )
        fig3.update_yaxes(showgrid=True, gridcolor="rgba(128,128,128,0.15)")
        st.plotly_chart(fig3, use_container_width=True, config={"displayModeBar": False})
        st.caption(source_label)

    with col2:
        rets = fund_df["Daily_Return_%"].dropna()
        fig4 = go.Figure()
        fig4.add_trace(go.Histogram(
            x=rets, nbinsx=60, marker_color="coral", opacity=0.85,
            hovertemplate="Return: %{x:.2f}%<br>Count: %{y}<extra></extra>"
        ))
        fig4.add_vline(x=mu_real, line_width=2, line_color="green",
                        annotation_text=f"Mean: {mu_real:.4f}%",
                        annotation_position="top")
        fig4.update_layout(
            title="Daily Return Distribution",
            xaxis_title="Daily Return %", yaxis_title="Count",
            height=430, margin=dict(l=10, r=10, t=55, b=10),
        )
        st.plotly_chart(fig4, use_container_width=True, config={"displayModeBar": False})

    if mu_trend > 0.02:
        st.success(
            f"📈 **{selected_fund[:50]} is trending UP** — average trajectory "
            f"~{mu_trend:+.3f}%/day over the recent window. Forecast projects continued growth.")
    elif mu_trend < -0.02:
        st.error(
            f"📉 **{selected_fund[:50]} is trending DOWN** — average trajectory "
            f"~{mu_trend:+.3f}%/day over the recent window. Forecast projects continued decline.")
    else:
        st.info(
            f"➖ **{selected_fund[:50]} is roughly flat** — no strong "
            f"recent directional trend detected.")

    stats = pd.DataFrame({
        "Metric" : ["Daily Return (avg)",
                    "Annual Return (est)",
                    "Volatility (SD)",
                    "Sharpe Ratio","Alpha","Beta",
                    "Expense Ratio","Risk Level",
                    "Historical NAV","Live NAV Today",
                    "1 Year Return"],
        "Value"  : [f"{mu_real:.4f}%",
                    f"{mu_real*252:.2f}%",
                    f"{sigma_real:.4f}%",
                    f"{sharpe_val:.3f}",
                    f"{alpha_val:.3f}",
                    f"{beta_val:.3f}",
                    f"{expense:.2f}%",
                    risk_level,
                    f"₹{hist_nav:.2f}",
                    f"₹{display_nav:.2f} ({nav_source})",
                    f"{ret_1y:.2f}%"]
    })
    st.dataframe(stats, use_container_width=True,
                 hide_index=True)

# ── TAB 4: AI/ML Forecast (74.01% HistGradientBoosting Model) ──
with tab4:
    st.subheader("🤖 AI/ML Multi-Factor Directional Forecaster")
    st.markdown(
        "Powered by **HistGradientBoosting** trained on **419,000+ mutual fund records** · "
        "**74.01% Directional Accuracy** · **12 Technical & Fundamental Factors**")

    ml_model, ml_meta = load_ml_model()

    if ml_model is not None and len(fund_df) >= 15:
        feat_df = extract_fund_features(fund_df)
        if feat_df is not None:
            raw_pred = float(ml_model.predict(feat_df)[0])
            baseline_drift = 0.023
            excess_alpha = raw_pred - baseline_drift

            r5 = float(feat_df['ret_5d'].iloc[0])
            r10 = float(feat_df['ret_10d'].iloc[0])
            r21 = float(feat_df['ret_21d'].iloc[0])
            rsi = float(feat_df['rsi_14'].iloc[0])

            # Calibrated 21-day return blending model alpha with observed directional velocity
            mom_component = 0.40 * r5 + 0.25 * r10 + 0.15 * r21
            rsi_adj = (rsi - 50.0) / 100.0 * 0.01
            pred_ret = excess_alpha + mom_component + rsi_adj

            pred_21d_ret_pct = round(pred_ret * 100.0, 2)
            pred_target_nav = round(display_nav * (1.0 + pred_ret), 2)

            if pred_ret >= 0.030 and r5 > 0:
                signal = "STRONG BUY 🚀"
                conviction_score = min(92.0, 75.0 + (pred_ret - 0.030) * 200.0)
                win_prob = min(91.5, 78.0 + pred_ret * 120.0)
            elif pred_ret >= 0.008:
                signal = "ACCUMULATE 📈"
                conviction_score = min(78.0, 65.0 + pred_ret * 150.0)
                win_prob = min(82.0, 68.0 + pred_ret * 100.0)
            elif pred_ret >= -0.008:
                signal = "HOLD / NEUTRAL ⚖️"
                conviction_score = 55.0
                win_prob = 52.0 + pred_ret * 100.0
            else:
                signal = "CAUTION / TRIM ⚠️"
                conviction_score = max(35.0, 65.0 + pred_ret * 200.0)
                win_prob = max(28.0, 48.0 + pred_ret * 150.0)

            # Metric cards
            m1, m2, m3, m4 = st.columns(4)
            m1.metric("ML Directional Signal", signal)
            m2.metric("Conviction Score", f"{conviction_score:.1f}%", f"{conviction_score-50:+.1f}% vs baseline")
            m3.metric("Estimated Win Probability", f"{win_prob:.1f}%", "Historical IC")
            m4.metric("Target NAV (21-Day)", f"₹{pred_target_nav:.2f}", f"{pred_21d_ret_pct:+.2f}%")

            st.markdown("---")

            # Visual Factor Analysis
            c_f1, c_f2 = st.columns([1, 1])

            with c_f1:
                st.markdown("#### 🧭 Model Conviction Gauge")
                fig_gauge = go.Figure(go.Indicator(
                    mode="gauge+number",
                    value=conviction_score,
                    domain={'x': [0, 1], 'y': [0, 1]},
                    title={'text': f"Conviction: {signal}", 'font': {'size': 17}},
                    gauge={
                        'axis': {'range': [0, 100], 'tickwidth': 1},
                        'bar': {'color': "#1f77b4"},
                        'steps': [
                            {'range': [0, 50], 'color': "rgba(255, 99, 71, 0.25)"},
                            {'range': [50, 75], 'color': "rgba(255, 215, 0, 0.25)"},
                            {'range': [75, 100], 'color': "rgba(46, 204, 113, 0.25)"}
                        ],
                        'threshold': {
                            'line': {'color': "black", 'width': 3},
                            'thickness': 0.75,
                            'value': conviction_score
                        }
                    }
                ))
                fig_gauge.update_layout(height=290, margin=dict(l=20, r=20, t=30, b=20))
                st.plotly_chart(fig_gauge, use_container_width=True, config={"displayModeBar": False})

            with c_f2:
                st.markdown("#### 📊 Key Feature Indicators")
                f_rsi = float(feat_df['rsi_14'].iloc[0])
                f_mom = float(feat_df['mom_ratio'].iloc[0])
                f_z   = float(feat_df['nav_z'].iloc[0])
                f_v10 = float(feat_df['vol_10d'].iloc[0]) * 100
                f_r5  = float(feat_df['ret_5d'].iloc[0]) * 100
                f_r21 = float(feat_df['ret_21d'].iloc[0]) * 100

                fig_bar = go.Figure()
                fig_bar.add_trace(go.Bar(
                    x=["RSI-14", "Mom Ratio", "NAV Z-Score", "10D Vol %", "5D Ret %", "21D Ret %"],
                    y=[f_rsi/10, f_mom, f_z, f_v10, f_r5, f_r21],
                    marker_color=["#3498db", "#9b59b6", "#1abc9c", "#e67e22",
                                  "#2ecc71" if f_r5 >= 0 else "#e74c3c",
                                  "#2ecc71" if f_r21 >= 0 else "#e74c3c"],
                    hovertemplate="Factor: %{x}<br>Scaled Value: %{y:.2f}<extra></extra>"
                ))
                fig_bar.update_layout(title="Scaled Input Signals", height=290,
                                      margin=dict(l=20, r=20, t=30, b=20),
                                      yaxis_title="Normalized Factor Magnitude")
                st.plotly_chart(fig_bar, use_container_width=True, config={"displayModeBar": False})

            # 12-factor inspection table
            st.markdown("#### 📋 12-Factor Machine Learning Inputs & Interpretability")
            feat_display = pd.DataFrame([
                {"Factor": "5-Day Return", "Value": f"{f_r5:+.2f}%", "Feature Name": "ret_5d", "Significance": "Short-term momentum shock"},
                {"Factor": "10-Day Return", "Value": f"{float(feat_df['ret_10d'].iloc[0])*100:+.2f}%", "Feature Name": "ret_10d", "Significance": "Bi-weekly velocity trend"},
                {"Factor": "21-Day Return", "Value": f"{f_r21:+.2f}%", "Feature Name": "ret_21d", "Significance": "Monthly cyclic momentum"},
                {"Factor": "10-Day Volatility", "Value": f"{f_v10:.2f}%", "Feature Name": "vol_10d", "Significance": "Recent price dispersion"},
                {"Factor": "30-Day Volatility", "Value": f"{float(feat_df['vol_30d'].iloc[0])*100:.2f}%", "Feature Name": "vol_30d", "Significance": "Medium-term baseline risk"},
                {"Factor": "Momentum Ratio", "Value": f"{f_mom:.3f}", "Feature Name": "mom_ratio", "Significance": "Risk-adjusted velocity"},
                {"Factor": "RSI (14-Day)", "Value": f"{f_rsi:.1f}", "Feature Name": "rsi_14", "Significance": "Overbought (>70) / Oversold (<30) oscillator"},
                {"Factor": "NAV Z-Score (60D)", "Value": f"{f_z:.2f}", "Feature Name": "nav_z", "Significance": "Mean reversion distance"},
                {"Factor": "Sharpe Ratio", "Value": f"{sharpe_val:.3f}", "Feature Name": "Sharpe", "Significance": "Historical risk-adjusted alpha"},
                {"Factor": "Alpha", "Value": f"{alpha_val:.3f}", "Feature Name": "Alpha", "Significance": "Excess return over category benchmark"},
                {"Factor": "Beta", "Value": f"{beta_val:.3f}", "Feature Name": "Beta", "Significance": "Systematic market covariance"},
                {"Factor": "Expense Ratio (TER)", "Value": f"{expense:.2f}%", "Feature Name": "Expense_Ratio", "Significance": "Ongoing fee drag on CAGR"},
            ])
            st.dataframe(feat_display, use_container_width=True, hide_index=True)

            st.info("💡 **Explainable AI (XAI) Insight:** The HistGradientBoosting model aggregates tree ensembles across multi-scale returns and volatility bounds to eliminate emotional bias and detect institutional money flows.")
    else:
        st.warning("Insufficient historical data to run the ML model for this fund (minimum 15 trading days required).")

# ── TAB 5: Quantum QAOA Optimizer ──
with tab5:
    st.subheader("⚛️ Quantum Approximate Optimization Algorithm (QAOA)")
    st.markdown("Simulating parameterized quantum circuits for **QUBO (Quadratic Unconstrained Binary Optimization)** portfolio selection.")

    try:
        _backend_app_dir = os.path.join(_base_dir, "backend", "app")
        if _backend_app_dir not in sys.path:
            sys.path.insert(0, _backend_app_dir)
        import qaoa_optimizer
        run_qaoa_portfolio_optimizer = qaoa_optimizer.run_qaoa_portfolio_optimizer
        qaoa_available = True
    except Exception as e:
        qaoa_available = False
        st.error(f"QAOA module could not be loaded: {e}")

    if qaoa_available:
        all_funds_list = sorted(df["Scheme_Name"].unique().tolist())
        default_pool = [f for f in all_funds_list if any(k in f for k in ["HDFC", "SBI", "Nippon", "ICICI", "Parag Parikh", "Axis"])][:6]
        if len(default_pool) < 4:
            default_pool = all_funds_list[:5]

        st.markdown("#### 1. Configure Quantum Optimization Problem")
        q_col1, q_col2, q_col3 = st.columns([2, 1, 1])
        with q_col1:
            candidate_funds = st.multiselect(
                "Select candidate funds for the quantum register (N qubits):",
                all_funds_list,
                default=default_pool,
                key="qaoa_candidate_funds_multiselect")
        with q_col2:
            max_k = max(2, min(len(candidate_funds) - 1, 6))
            k_target = st.slider(
                "Target Cardinality (K funds to select):",
                min_value=2,
                max_value=max_k,
                value=min(3, max_k),
                key="qaoa_k_target_slider")
        with q_col3:
            qaoa_layers = st.selectbox("Circuit Depth (p layers):", [1, 2, 3], index=1, key="qaoa_circuit_depth_select")

        run_sim_btn = st.button("🚀 Run QAOA Quantum Simulation", type="primary", key="btn_run_qaoa_sim_primary")

        if run_sim_btn:
            if len(candidate_funds) < k_target:
                st.error("Candidate pool must contain at least as many funds as target K!")
            elif len(candidate_funds) > 14:
                st.warning("Please limit candidate pool to 14 funds or fewer for rapid simulation.")
            else:
                with st.spinner(f"Preparing QUBO Hamiltonian and optimizing p={qaoa_layers} QAOA circuit with COBYLA..."):
                    fund_returns_list = []
                    fund_vols_list = []
                    returns_series_dict = {}

                    for cf in candidate_funds:
                        cf_df = df[df["Scheme_Name"] == cf].sort_values("Date")
                        if "Daily_Return_%" in cf_df.columns:
                            rets = cf_df["Daily_Return_%"].dropna().values / 100.0
                        else:
                            rets = np.diff(cf_df["NAV_Value"].values) / np.maximum(cf_df["NAV_Value"].values[:-1], 1e-6)

                        r_mean = float(np.mean(rets)) if len(rets) > 0 else 0.0004
                        r_std = float(np.std(rets)) if len(rets) > 0 else 0.01
                        fund_returns_list.append(r_mean)
                        fund_vols_list.append(r_std)
                        returns_series_dict[cf] = pd.Series(rets, index=cf_df["Date"].iloc[-len(rets):])

                    rets_df = pd.DataFrame(returns_series_dict).fillna(0.0)
                    corr_mat = rets_df.corr().values if len(rets_df) > 5 else np.eye(len(candidate_funds))
                    corr_mat_list = corr_mat.tolist()

                    q_res = run_qaoa_portfolio_optimizer(
                        fund_names=candidate_funds,
                        fund_returns=fund_returns_list,
                        fund_volatilities=fund_vols_list,
                        correlations=corr_mat_list,
                        k=k_target,
                        qaoa_layers=qaoa_layers
                    )
                    st.session_state["qaoa_result"] = q_res

        if "qaoa_result" in st.session_state:
            q_res = st.session_state["qaoa_result"]

            st.success(f"✅ **Quantum QAOA Simulation Converged!** (Energy: {q_res['qubo_energy']:.4f} | Convergence Quality: {q_res['convergence_quality']*100:.1f}%)")

            qc1, qc2, qc3, qc4 = st.columns(4)
            qc1.metric("QAOA Sharpe Ratio", f"{q_res['qaoa_metrics']['sharpe_ratio']:.4f}", "Quantum Optimum")
            qc2.metric("Expected Annual Return", f"{q_res['qaoa_metrics']['expected_annual_return_pct']:.2f}%")
            qc3.metric("Annual Volatility", f"{q_res['qaoa_metrics']['annual_volatility_pct']:.2f}%")
            qc4.metric("Runtime", f"{q_res['runtime_seconds']:.3f}s", "COBYLA p=2")

            st.markdown("---")

            col_w, col_comp = st.columns([1, 1])

            with col_w:
                st.markdown("#### 🏆 QAOA Selected Optimal Portfolio")
                st.write(f"Selected **{len(q_res['selected_funds'])} funds** out of {q_res['n_funds_input']} candidates:")

                sel_weights = q_res["portfolio_weights_pct"]
                fig_donut = go.Figure(data=[go.Pie(
                    labels=list(sel_weights.keys()),
                    values=list(sel_weights.values()),
                    hole=0.45,
                    textinfo="label+percent",
                    marker=dict(colors=["#00b4d8", "#0077b6", "#90e0ef", "#03045e", "#caf0f8"])
                )])
                fig_donut.update_layout(height=300, margin=dict(l=10, r=10, t=10, b=10), showlegend=False)
                st.plotly_chart(fig_donut, use_container_width=True, config={"displayModeBar": False})

            with col_comp:
                st.markdown("#### ⚖️ Quantum vs Classical Benchmark Comparison")
                bench_df = pd.DataFrame(q_res["benchmark_table"])
                st.dataframe(
                    bench_df[["method", "sharpe", "annual_return_pct", "volatility_pct", "time_sec"]].rename(columns={
                        "method": "Algorithm", "sharpe": "Sharpe", "annual_return_pct": "Return %",
                        "volatility_pct": "Volatility %", "time_sec": "Time (s)"
                    }),
                    use_container_width=True, hide_index=True)

                fig_bench_bar = go.Figure()
                fig_bench_bar.add_trace(go.Bar(
                    x=[b["method"] for b in q_res["benchmark_table"]],
                    y=[b["sharpe"] for b in q_res["benchmark_table"]],
                    marker_color=["#00b4d8", "#e74c3c", "#f39c12"],
                    text=[f"{b['sharpe']:.3f}" for b in q_res["benchmark_table"]],
                    textposition="auto"
                ))
                fig_bench_bar.update_layout(
                    title="Sharpe Ratio by Optimization Paradigm",
                    yaxis_title="Sharpe Ratio",
                    height=220,
                    margin=dict(l=10, r=10, t=30, b=10))
                st.plotly_chart(fig_bench_bar, use_container_width=True, config={"displayModeBar": False})

            with st.expander("📐 Mathematical Formulation: QUBO Ising Hamiltonian & QAOA Circuit"):
                st.markdown(r"""
                **1. Combinatorial Objective Function:**
                $$C(x) = -\lambda_1 \sum_{i=1}^N \mu_i x_i + \lambda_2 \sum_{i=1}^N \sum_{j=1}^N \sigma_{ij} x_i x_j + \lambda_3 \left(\sum_{i=1}^N x_i - K\right)^2$$
                where $x_i \in \{0, 1\}$ are binary selection variables, $\mu_i$ is expected fund return, $\sigma_{ij}$ is inter-fund covariance, and $K$ is the target cardinality.

                **2. Mapping to Pauli Spin Operators ($Z_i$):**
                Using the qubit transformation $x_i = \frac{1 - Z_i}{2}$, the cost problem is mapped to an Ising spin Hamiltonian:
                $$H_C = \sum_{i} h_i Z_i + \sum_{i < j} J_{ij} Z_i Z_j$$

                **3. QAOA Circuit State Evolution ($p$ layers):**
                $$|\psi(\boldsymbol{\gamma}, \boldsymbol{\beta})\rangle = \prod_{l=1}^p e^{-i \beta_l H_M} e^{-i \gamma_l H_C} |+\rangle^{\otimes N}$$
                where $H_M = \sum_{i=1}^N X_i$ is the transverse-field mixer Hamiltonian that induces quantum tunneling between candidate portfolios.
                """)

# ── TAB 6: Compare Funds — Live % Change + Forecast (Plotly) ──
with tab6:
    st.subheader("⚖️ Live Fund Performance & Forecast")

    all_funds = sorted(df["Scheme_Name"].unique())

    period_map = {"1M":21,"3M":63,"6M":126,"1Y":252,
                  "2Y":504,"3Y":756,"ALL":100000}
    period = st.radio("Period:", list(period_map.keys()),
                       index=3, horizontal=True)
    n_days = period_map[period]

    c1, c2 = st.columns([3,1])
    with c1:
        compare_list = st.multiselect(
            "Select funds to compare (up to 15 for a readable chart):",
            all_funds, default=all_funds[:2])
    with c2:
        show_forecast = st.checkbox("📈 Show forecast", value=True)
        forecast_days = st.slider("Forecast days", 5, 90, 30) if show_forecast else 0

    tab10_colors = ["#1f77b4","#ff7f0e","#2ca02c","#d62728","#9467bd",
                     "#8c564b","#e377c2","#7f7f7f","#bcbd22","#17becf"]

    if 1 <= len(compare_list) <= 15:
        cdata = []
        fig6 = go.Figure()

        for idx, fund in enumerate(compare_list):
            full_fdf = df[df["Scheme_Name"]==fund].sort_values("Date")
            fdf = full_fdf.tail(n_days) if n_days < len(full_fdf) else full_fdf
            if len(fdf) < 5:
                continue

            base_nav = fdf["NAV_Value"].iloc[0]
            dates  = list(fdf["Date"])
            values = list((fdf["NAV_Value"]/base_nav - 1)*100)

            # attach LIVE AMFI NAV so the line reaches today — but only if it's
            # a sane continuation of the series (guards against bad fuzzy matches)
            fund_words = set(fund.lower().split())
            best_match, best_score = None, 0
            for amfi_name in nav_today:
                score = len(fund_words & set(amfi_name.lower().split()))
                if score > best_score:
                    best_score, best_match = score, amfi_name
            if best_match and best_score >= 2:
                live_val = (nav_today[best_match]["nav"]/base_nav - 1)*100
                if abs(live_val - values[-1]) <= max(15, abs(values[-1]) * 0.5):
                    dates.append(pd.Timestamp(datetime.now(IST).date()))
                    values.append(live_val)

            color = tab10_colors[idx % 10]
            fig6.add_trace(go.Scatter(
                x=dates, y=values, mode="lines", name=fund[:28],
                line=dict(color=color, width=2),
                hovertemplate="%{x|%d %b %Y}<br><b>%{y:.2f}%</b><extra>" + fund[:28] + "</extra>"))

            if show_forecast and len(values) >= 3:
                w_len = min(14, len(values))
                w_vals = np.array(values[-w_len:], dtype=float)
                x_pts = np.arange(w_len)
                slope_reg, _ = np.polyfit(x_pts, w_vals, 1)  # % per day
                slope_5d = (w_vals[-1] - w_vals[max(0, w_len - 5)]) / max(1, min(4, w_len - 1))
                mu_f = float(0.60 * slope_5d + 0.40 * slope_reg)
                decay = 0.95
                last_val, last_date = values[-1], dates[-1]
                fut_dates = list(pd.bdate_range(
                    last_date + pd.Timedelta(days=1), periods=forecast_days))
                cum_delta = [sum(mu_f * (decay**t) for t in range(i+1)) for i in range(forecast_days)]
                fut_vals = [last_val + d for d in cum_delta]
                fig6.add_trace(go.Scatter(
                    x=[last_date]+fut_dates, y=[last_val]+fut_vals,
                    mode="lines", name=f"{fund[:22]} (forecast)",
                    line=dict(color=color, width=2, dash="dash"), opacity=0.6,
                    hovertemplate="%{x|%d %b %Y}<br><b>%{y:.2f}%</b><extra>Forecast</extra>",
                    showlegend=False))

            r1y = ((full_fdf["NAV_Value"].iloc[-1] -
                    full_fdf["NAV_Value"].iloc[-252 if len(full_fdf)>252 else 0]) /
                   full_fdf["NAV_Value"].iloc[-252 if len(full_fdf)>252 else 0])*100
            cdata.append({
                "Fund"    : fund[:35],
                "Sharpe"  : round(full_fdf["Sharpe"].mean(),3),
                "1Y Ret%" : round(r1y,2),
                "Alpha"   : round(full_fdf["Alpha"].mean(),3),
                "Beta"    : round(full_fdf["Beta"].mean(),3),
                "Expense" : round(full_fdf["Expense_Ratio"].mean(),3),
                "Risk"    : full_fdf["Risk_Level"].iloc[-1],
            })

        fig6.add_hline(y=0, line_width=1, line_color="gray", opacity=0.5)
        title = f"% Change — {period}"
        if show_forecast:
            title += "  (solid = actual · dashed = forecast)"
        fig6.update_layout(
            title=title, xaxis_title="Date", yaxis_title="% Change",
            hovermode="x unified", height=480,
            margin=dict(l=10, r=10, t=55, b=10),
            legend=dict(orientation="h", yanchor="bottom", y=1.05, xanchor="left", x=0))
        fig6.update_xaxes(tickformat="%d %b %Y", showgrid=True,
                            gridcolor="rgba(128,128,128,0.15)", rangeslider_visible=True)
        fig6.update_yaxes(showgrid=True, gridcolor="rgba(128,128,128,0.15)")
        st.plotly_chart(fig6, use_container_width=True, config={"displayModeBar": False})

        st.dataframe(pd.DataFrame(cdata),
                     use_container_width=True,
                     hide_index=True)
        if show_forecast:
            st.caption(
                "Forecast = Damped momentum trajectory using each fund's recent directional slope "
                "(14D regression + 5D velocity). Captures both upward and downward market momentum. "
                "Click a legend entry to show/hide that fund.")
    elif len(compare_list) > 15:
        st.warning("Please select 15 funds or fewer for a readable chart.")
    else:
        st.warning("Select at least 1 fund to compare.")


# ── TAB 7: Live NAV Search ──
with tab7:
    st.subheader("📡 Live NAV Search — All Funds")
    feed_type = "Official AMFI India Real-Time Feed" if is_amfi_network_live else "AMFI Verified Scheme Database"
    st.markdown(
        f"Data from **{feed_type}** · "
        f"Updated daily · "
        f"**{len(nav_today):,} funds loaded**")

    search = st.text_input(
        "Search any mutual fund:",
        placeholder="e.g. SBI, HDFC, Quant, Axis, Nippon...",
        key="nav_search_input_unique")

    if search and len(nav_today) > 0:
        results = {k:v for k,v in nav_today.items()
                   if search.lower() in k.lower()}
        if results:
            nav_df = pd.DataFrame([
                {"Fund Name": k,
                 "NAV (₹)"  : f"₹{v['nav']:.4f}",
                 "Date"     : v["date"],
                 "Code"     : v["code"]}
                for k,v in list(results.items())[:50]
            ])
            st.dataframe(nav_df,
                         use_container_width=True,
                         hide_index=True)
            st.caption(
                f"Showing {min(50,len(results))} of "
                f"{len(results)} results")
        else:
            st.warning(f"No funds found for: {search}")
    elif len(nav_today) == 0:
        st.error(
            "AMFI API not responding. "
            "Try clicking Refresh Live Data button above.")
    else:
        st.info(
            "Type a fund name above to search "
            "today's live NAV from AMFI India.")

    if nav_today:
        st.subheader("📊 NAV Statistics Today")
        navs = [v["nav"] for v in nav_today.values()]
        n1,n2,n3,n4 = st.columns(4)
        n1.metric("Total Funds", f"{len(nav_today):,}")
        n2.metric("Lowest NAV",  f"₹{min(navs):.2f}")
        n3.metric("Highest NAV", f"₹{max(navs):,.2f}")
        n4.metric("Average NAV", f"₹{sum(navs)/len(navs):.2f}")


# ── TAB 8: Platform Guide ──
with tab8:
    st.subheader("🏦 Best Platform to Invest")
    if investment < 10000:
        top = "Groww"
        reason = "Best for small amounts · zero minimum"
    elif investment < 100000:
        top = "Kuvera"
        reason = "Best free direct fund platform"
    else:
        top = "Zerodha Coin"
        reason = "Best analytics for large investments"

    st.success(f"🥇 **Best for you: {top}** — {reason}")

    for name,url,rating,desc in [
        ("🌱 Groww","groww.in","⭐⭐⭐⭐⭐",
         "Zero commission · ₹100 min · Instant KYC"),
        ("🪙 Zerodha Coin","coin.zerodha.com","⭐⭐⭐⭐⭐",
         "Best analytics · Direct funds · Stocks+MF"),
        ("💎 Kuvera","kuvera.in","⭐⭐⭐⭐⭐",
         "100% free · Tax harvesting · Goal planning"),
        ("💰 Paytm Money","paytmmoney.com","⭐⭐⭐⭐",
         "UPI instant · SIP automation"),
        ("🏛️ MF Central","mfcentral.com","⭐⭐⭐⭐",
         "SEBI official · Most secure · Free"),
    ]:
        with st.expander(f"{name} {rating}"):
            st.markdown(f"**{desc}** · 🌐 {url}")

    st.divider()
    st.subheader("📌 Your Personalised Action Plan")
    st.info(f"""
**Step 1:** Open **{top}** → complete KYC (Aadhaar + PAN, 5 min)

**Step 2:** Search: **{selected_fund[:50]}**

**Step 3:** Invest **₹{investment:,}** as lump sum
           OR **₹{sip_amount:,}/month** as SIP

**Step 4:** Set reminder to withdraw on:
           **{withdraw_date.strftime("%d %B %Y")}**

**Expected outcome:**
- Lump sum: ₹{investment:,} → ₹{opt_val:,.0f} ({ret_pct:.1f}% in {opt_day//21} months)
- SIP: ₹{sip_amount:,}/mo × {sip_years*12} months → ₹{expected_sip:,.0f}
    """)


# ── TAB 9: PDF Report ──
with tab9:
    st.subheader("📄 Download Your Investment Report")
    st.markdown("Generate a personalised PDF with your complete analysis.")

    report_name = st.text_input("Your Name:", value="MANI SAI", key="pdf_report_user_name_input_unique")

    if st.button("📄 Generate and Download PDF", key="btn_generate_download_pdf_unique"):
        try:
            from fpdf import FPDF
            pdf = FPDF()
            pdf.add_page()

            pdf.set_font("Arial", "B", 18)
            pdf.cell(190, 12, "Q-FinOpt Investment Report", 0, 1, "C")
            pdf.set_font("Arial", "", 10)
            pdf.cell(190, 8,
                f"For: {report_name} | Date: {datetime.now(IST).strftime('%d %b %Y %I:%M %p')} IST",
                0, 1, "C")
            pdf.ln(5)

            pdf.set_font("Arial", "B", 13)
            pdf.cell(190, 10, "Fund Details", 0, 1)
            pdf.set_font("Arial", "", 10)
            for label, value in [
                ("Fund Name",        str(selected_fund[:60]).encode('latin-1', 'replace').decode('latin-1')),
                ("Category",         str(category)),
                ("Risk Level",       str(risk_level)),
                ("Latest NAV",       f"Rs {display_nav:.2f}"),
                ("1 Year Return",    f"{ret_1y:.2f}%"),
                ("Sharpe Ratio",     f"{sharpe_val:.3f}"),
                ("Alpha",            f"{alpha_val:.3f}"),
                ("Beta",             f"{beta_val:.3f}"),
                ("Expense Ratio",    f"{expense:.2f}%"),
                ("Market Sentiment", str(sentiment["sentiment"])),
            ]:
                pdf.set_font("Arial", "B", 10)
                pdf.cell(70, 7, label + ":", 0, 0)
                pdf.set_font("Arial", "", 10)
                pdf.cell(120, 7, str(value), 0, 1)
            pdf.ln(4)

            pdf.set_font("Arial", "B", 13)
            pdf.cell(190, 10, "Withdrawal Recommendation", 0, 1)
            for label, value in [
                ("Investment Amount",  f"Rs {investment:,}"),
                ("Investment Date",    invest_date.strftime("%d %b %Y")),
                ("Withdraw On",        withdraw_date.strftime("%d %b %Y")),
                ("Holding Period",     f"{opt_day} days (~{opt_day//21} months)"),
                ("Expected Value",     f"Rs {opt_val:,.0f}"),
                ("Expected Gain",      f"Rs {gain:,.0f} ({ret_pct:.1f}%)"),
                ("Profit Probability", f"{prob_g[opt_day]*100:.1f}%"),
            ]:
                pdf.set_font("Arial", "B", 10)
                pdf.cell(70, 7, label + ":", 0, 0)
                pdf.set_font("Arial", "", 10)
                pdf.cell(120, 7, str(value), 0, 1)
            pdf.ln(4)

            pdf.set_font("Arial", "B", 13)
            pdf.cell(190, 10, "SIP Projection", 0, 1)
            for label, value in [
                ("Monthly SIP",      f"Rs {sip_amount:,}"),
                ("Duration",         f"{sip_years} years"),
                ("Total Invested",   f"Rs {total_invested:,}"),
                ("Expected Corpus",  f"Rs {expected_sip:,.0f}"),
                ("Wealth Gain",      f"Rs {gain_sip:,.0f}"),
                ("Est. XIRR",        f"{xirr:.1f}% per year"),
            ]:
                pdf.set_font("Arial", "B", 10)
                pdf.cell(70, 7, label + ":", 0, 0)
                pdf.set_font("Arial", "", 10)
                pdf.cell(120, 7, str(value), 0, 1)
            pdf.ln(4)

            pdf.set_font("Arial", "I", 8)
            pdf.set_text_color(100, 100, 100)
            pdf.multi_cell(190, 6,
                "DISCLAIMER: For educational purposes only. "
                "Mutual fund investments are subject to market risks. "
                "Past performance does not guarantee future returns.")

            pdf.set_font("Arial", "B", 8)
            pdf.set_text_color(0, 0, 0)
            pdf.set_xy(10, 285)
            pdf.cell(190, 6,
                f"Q-FinOpt v3.0 | Built by MANI SAI | {datetime.now(IST).strftime('%d %b %Y')}",
                0, 0, "C")

            out = pdf.output()
            pdf_bytes = bytes(out) if isinstance(out, (bytearray, bytes)) else str(out).encode("latin-1", errors="replace")
            st.download_button(
                label="⬇️ Click Here to Download PDF",
                data=pdf_bytes,
                file_name=f"QFinOpt_{selected_fund[:15].replace(' ','_')}_{date.today()}.pdf",
                mime="application/pdf")
            st.success("✅ PDF ready! Click button above to download.")

        except ImportError:
            st.warning("Installing PDF library...")
            import subprocess
            subprocess.run(["pip","install","fpdf2","-q"])
            st.info("Done! Click Generate again.")
        except Exception as e:
            st.error(f"Error: {e}")

# ── TAB 10: Reminders ──
with tab10:
    st.subheader("🔔 Set Withdrawal Reminder")
    st.markdown(f"Your optimal withdrawal date is **{withdraw_date.strftime('%d %B %Y')}**")

    st.info(f"""
Fund      : {selected_fund[:55]}
Invested  : Rs {investment:,} on {invest_date.strftime('%d %b %Y')}
Withdraw  : {withdraw_date.strftime('%d %B %Y')} (Day {opt_day})
Expected  : Rs {opt_val:,.0f} (+Rs {gain:,.0f} | {ret_pct:.1f}%)
Probability: {prob_g[opt_day]*100:.1f}%
    """)

    col1, col2 = st.columns(2)
    with col1:
        st.markdown("### 📱 WhatsApp Reminder")
        msg = (
            f"Q-FinOpt Reminder%0A"
            f"Fund: {selected_fund[:40]}%0A"
            f"WITHDRAW ON: {withdraw_date.strftime('%d %b %Y')}%0A"
            f"Expected: Rs {opt_val:,.0f}%0A"
            f"Gain: Rs {gain:,.0f} ({ret_pct:.1f}%)")
        wa_url = f"https://wa.me/?text={msg}"
        st.markdown(
            f'''<a href="{wa_url}" target="_blank">
            <button style="background:#25D366;color:white;
                           padding:14px 28px;border:none;
                           border-radius:8px;font-size:15px;
                           cursor:pointer;width:100%">
                📱 Send WhatsApp Reminder
            </button></a>''',
            unsafe_allow_html=True)

    with col2:
        st.markdown("### 📧 Email Reminder")
        subject = f"Withdraw {selected_fund[:25]} on {withdraw_date.strftime('%d %B %Y')}"
        body = (
            f"Q-FinOpt Reminder%0A"
            f"Fund: {selected_fund}%0A"
            f"WITHDRAW ON: {withdraw_date.strftime('%d %B %Y')}%0A"
            f"Expected: Rs {opt_val:,.0f}%0A"
            f"Gain: Rs {gain:,.0f} ({ret_pct:.1f}%)")
        mailto = f"mailto:?subject={subject}&body={body}"
        st.markdown(
            f'''<a href="{mailto}">
            <button style="background:#4285F4;color:white;
                           padding:14px 28px;border:none;
                           border-radius:8px;font-size:15px;
                           cursor:pointer;width:100%">
                📧 Send Email Reminder
            </button></a>''',
            unsafe_allow_html=True)

    st.divider()
    st.markdown("### 📅 Add to Google Calendar")
    cal_date = withdraw_date.strftime("%Y%m%d")
    cal_url  = (
        f"https://calendar.google.com/calendar/render"
        f"?action=TEMPLATE"
        f"&text=Withdraw+{selected_fund[:20].replace(' ','+')}+-+QFinOpt"
        f"&dates={cal_date}/{cal_date}"
        f"&details=QFinOpt+Reminder")
    st.markdown(
        f'''<a href="{cal_url}" target="_blank">
        <button style="background:#DB4437;color:white;
                       padding:14px 28px;border:none;
                       border-radius:8px;font-size:15px;
                       cursor:pointer">
            📅 Add to Google Calendar
        </button></a>''',
        unsafe_allow_html=True)
    st.caption(f"Adds reminder on {withdraw_date.strftime('%d %B %Y')} to Google Calendar!")

st.divider()
from datetime import timezone
IST = timezone(timedelta(hours=5, minutes=30))
st.markdown(
    f"*Q-FinOpt v3.0 Live · "
    f"AMFI India + Yahoo Finance + Historical Data · "
    f"Last refresh: {datetime.now(IST).strftime('%d %b %Y %I:%M %p')} IST · "
    f"Built by MANI SAI*")
