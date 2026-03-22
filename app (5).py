
import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import json
from datetime import datetime, timedelta, date

st.set_page_config(
    page_title="Q-FinOpt Live",
    page_icon="📈", layout="wide")

st.title("📈 Q-FinOpt: Live Mutual Fund Advisor")
st.markdown("*Real-time · SIP Calculator · Withdrawal Timing · Platform Guide*")
st.divider()

# ── Load data ──
@st.cache_data
def load_data():
    file_id = "1EfNv54tjvjcsJdZhxYwa4zPJxl9q9_9X"
    url     = f"https://drive.google.com/uc?id={file_id}"
    df      = pd.read_csv(url)
    df["Date"] = pd.to_datetime(df["Date"])
    return df

# ── Load live Nifty data ──
@st.cache_data(ttl=3600)
def load_market():
    try:
        import yfinance as yf
        nifty  = yf.download("^NSEI",  period="1y",
                              progress=False)
        sensex = yf.download("^BSESN", period="1y",
                              progress=False)
        return nifty, sensex
    except:
        return None, None

with st.spinner("Loading live data..."):
    df           = load_data()
    nifty, sensex = load_market()

# ── Live market ticker ──
# Market data with full error protection
n_pct = 0
s_pct = 0
n_now = 0
s_now = 0
n_chg = 0
s_chg = 0
market_data_ok = False

try:
    if nifty is not None and len(nifty) > 1:
        nifty_close  = nifty["Close"].squeeze()
        sensex_close = sensex["Close"].squeeze()
        n_now  = float(nifty_close.iloc[-1])
        n_prev = float(nifty_close.iloc[-2])
        n_chg  = n_now - n_prev
        n_pct  = (n_chg / n_prev) * 100
        s_now  = float(sensex_close.iloc[-1])
        s_prev = float(sensex_close.iloc[-2])
        s_chg  = s_now - s_prev
        s_pct  = (s_chg / s_prev) * 100
        market_data_ok = True
except Exception as e:
    market_data_ok = False

if market_data_ok:
    mc1,mc2,mc3,mc4 = st.columns(4)
    mc1.metric("Nifty 50",
               f"{n_now:,.0f}",
               f"{n_chg:+.0f} ({n_pct:+.2f}%)")
    mc2.metric("Sensex",
               f"{s_now:,.0f}",
               f"{s_chg:+.0f} ({s_pct:+.2f}%)")
    mc3.metric("Market Mood",
               "Bullish 📈" if n_pct > 0 else "Bearish 📉")
    mc4.metric("Data Updated",
               datetime.now().strftime("%d %b %Y"))
    st.divider()
else:
    st.info("📡 Market ticker loading... Predictions use fund historical data.")
    st.divider()

# ── Sidebar ──
st.sidebar.title("🎯 Your Investment")
categories  = sorted(df["Sheet_Category"].unique().tolist())
risk_levels = sorted(df["Risk_Level"].unique().tolist())

st.sidebar.subheader("Filter")
sel_cat  = st.sidebar.multiselect(
    "Category", categories, default=categories[:3])
sel_risk = st.sidebar.multiselect(
    "Risk Level", risk_levels, default=risk_levels)

filtered = df[
    (df["Sheet_Category"].isin(sel_cat)) &
    (df["Risk_Level"].isin(sel_risk))
]["Scheme_Name"].unique().tolist()

st.sidebar.subheader("Fund & Amount")
selected_fund = st.sidebar.selectbox(
    "Choose Fund:", sorted(filtered))
invest_date = st.sidebar.date_input(
    "Start Date:", value=date.today())
investment  = st.sidebar.number_input(
    "Lump Sum (₹)", min_value=500,
    max_value=10000000, value=100000, step=500)
sip_amount  = st.sidebar.number_input(
    "Monthly SIP (₹)", min_value=500,
    max_value=100000, value=5000, step=500)
sip_years   = st.sidebar.slider(
    "SIP Duration (years)", 1, 30, 10)
n_sim = st.sidebar.selectbox(
    "Simulations", [1000,5000,10000], index=1)

# ── Real fund stats ──
fund_df    = df[df["Scheme_Name"]==selected_fund].sort_values("Date")
mu_real    = fund_df["Daily_Return_%"].mean()
sigma_real = fund_df["Daily_Return_%"].std()
sharpe_val = fund_df["Sharpe"].mean()
alpha_val  = fund_df["Alpha"].mean()
beta_val   = fund_df["Beta"].mean()
expense    = fund_df["Expense_Ratio"].mean()
risk_level = fund_df["Risk_Level"].iloc[-1]
category   = fund_df["Sheet_Category"].iloc[-1]
latest_nav = fund_df["NAV_Value"].iloc[-1]
nav_1y     = fund_df["NAV_Value"].iloc[-252] if len(fund_df)>252 else fund_df["NAV_Value"].iloc[0]
ret_1y     = ((latest_nav-nav_1y)/nav_1y)*100

# Market adjustment to mu
try:
    if market_data_ok and nifty is not None and len(nifty) > 21:
        nifty_close = nifty["Close"].squeeze()
        nifty_1m    = float(nifty_close.iloc[-1]) / float(nifty_close.iloc[-21]) - 1
        market_boost = nifty_1m * beta_val * 0.1
        mu_adjusted  = mu_real + market_boost
    else:
        mu_adjusted = mu_real
except:
    mu_adjusted = mu_real

# ── Fund header ──
st.subheader(f"📊 {selected_fund}")
mkt_signal = "🟢 Bullish" if n_pct > 0.5 else "🔴 Bearish" if n_pct < -0.5 else "🟡 Neutral"
st.caption(f"Market signal: {mkt_signal} | "
           f"Beta-adjusted return: {mu_adjusted*100:.4f}%/day")

c1,c2,c3,c4,c5,c6 = st.columns(6)
c1.metric("Latest NAV",    f"₹{latest_nav:.2f}")
c2.metric("1Y Return",     f"{ret_1y:.2f}%", f"{ret_1y:.1f}%")
c3.metric("Sharpe",        f"{sharpe_val:.3f}")
c4.metric("Alpha",         f"{alpha_val:.3f}")
c5.metric("Beta",          f"{beta_val:.3f}")
c6.metric("Expense",       f"{expense:.2f}%")
st.divider()

# ── TABS ──
tab1,tab2,tab3,tab4,tab5 = st.tabs([
    "📅 Withdrawal Timing",
    "📅 SIP Calculator",
    "📈 Fund Analysis",
    "🏦 Platform Guide",
    "⚖️ Compare Funds"])

# ══════════════════════════════
# TAB 1: Withdrawal Timing
# ══════════════════════════════
with tab1:
    st.subheader("When Should You Withdraw?")

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

    # Exact withdrawal date
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
        <h2 style="color:#4CAF50;margin:0 0 16px 0">
            🎯 Optimal Withdrawal Recommendation
        </h2>
        <div style="display:grid;
                    grid-template-columns:1fr 1fr 1fr 1fr;
                    gap:12px">
            <div style="background:rgba(76,175,80,0.15);
                        border:1px solid #4CAF50;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#81C784;font-size:12px">
                    Invest On</div>
                <div style="color:white;font-size:16px;
                            font-weight:bold">
                    {invest_date.strftime("%d %b %Y")}</div>
            </div>
            <div style="background:rgba(255,215,0,0.15);
                        border:1px solid #FFD700;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#FFD700;font-size:12px">
                    Withdraw On</div>
                <div style="color:#FFD700;font-size:16px;
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
                    Expected Value</div>
                <div style="color:white;font-size:16px;
                            font-weight:bold">
                    ₹{opt_val:,.0f}</div>
            </div>
            <div style="background:rgba(156,39,176,0.15);
                        border:1px solid #9C27B0;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#CE93D8;font-size:12px">
                    Expected Gain</div>
                <div style="color:#CE93D8;font-size:16px;
                            font-weight:bold">
                    +₹{gain:,.0f} ({ret_pct:.1f}%)</div>
            </div>
        </div>
        <div style="margin-top:14px;
                    background:rgba(255,255,255,0.05);
                    padding:10px 14px;border-radius:8px">
            <span style="color:#81C784">
                ✅ Profit probability:
                <strong>{prob_g[opt_day]*100:.1f}%</strong>
            </span> &nbsp;&nbsp;
            <span style="color:#64B5F6">
                📊 Risk: <strong>{risk_level}</strong>
            </span> &nbsp;&nbsp;
            <span style="color:#FFD54F">
                📡 Market-adjusted: <strong>Yes</strong>
            </span>
        </div>
    </div>
    """, unsafe_allow_html=True)

    # Period returns
    st.subheader("Returns by Period")
    p_cols = st.columns(4)
    for col,(label,days) in zip(p_cols,[
        ("3M",63),("6M",126),("1Y",252),("2Y",504)]):
        if days <= hold_days:
            v = exp_v[days]
            col.metric(label,
                f"₹{v:,.0f}",
                f"+{((v-investment)/investment)*100:.1f}% | "
                f"{prob_g[days]*100:.0f}%")

    col1,col2 = st.columns(2)
    with col1:
        fig1,ax1 = plt.subplots(figsize=(8,4))
        ax1.plot(paths[:,:min(300,n_sim)],
                 alpha=0.02, color="steelblue",
                 linewidth=0.5)
        ax1.plot(exp_v, color="orange",
                 linewidth=2.5, label="Expected")
        ax1.plot([np.percentile(paths[d],5)
                  for d in range(hold_days+1)],
                 color="red", linewidth=1.5,
                 linestyle="--", label="Worst 5%")
        ax1.plot([np.percentile(paths[d],95)
                  for d in range(hold_days+1)],
                 color="green", linewidth=1.5,
                 linestyle="--", label="Best 95%")
        ax1.axhline(investment, color="white",
                    linewidth=1, linestyle=":")
        ax1.axvline(opt_day, color="yellow",
                    linewidth=2, linestyle="--",
                    label=f"Day {opt_day}")
        ax1.set_title(f"Monte Carlo {n_sim:,} Scenarios")
        ax1.set_xlabel("Trading Days")
        ax1.set_ylabel("Value (₹)")
        ax1.legend(fontsize=7)
        ax1.grid(True, alpha=0.2)
        st.pyplot(fig1)
        plt.close()

    with col2:
        fig2,ax2 = plt.subplots(figsize=(8,4))
        ax2.plot(range(hold_days+1),
                 prob_g*100, color="green",
                 linewidth=2.5)
        ax2.fill_between(range(hold_days+1),
            prob_g*100, 50,
            where=[p>50 for p in prob_g],
            alpha=0.3, color="green")
        ax2.axvline(opt_day, color="yellow",
                    linewidth=2.5, linestyle="--",
                    label=f"Optimal: Day {opt_day}")
        ax2.axhline(50, color="white",
                    linewidth=1, linestyle=":")
        ax2.set_title("Probability of Profit")
        ax2.set_xlabel("Trading Days")
        ax2.set_ylabel("Probability (%)")
        ax2.legend(fontsize=8)
        ax2.set_ylim(0,100)
        ax2.grid(True, alpha=0.2)
        st.pyplot(fig2)
        plt.close()

    # Scenarios
    st.subheader("Scenario Breakdown")
    s1,s2,s3,s4 = st.columns(4)
    for col,(label,pct) in zip([s1,s2,s3,s4],[
        ("Worst 5%",5),("Conservative 25%",25),
        ("Optimistic 75%",75),("Best 95%",95)]):
        v = np.percentile(paths[opt_day], pct)
        col.metric(label, f"₹{v:,.0f}",
                   f"{((v-investment)/investment)*100:.1f}%")

# ══════════════════════════════
# TAB 2: SIP Calculator
# ══════════════════════════════
with tab2:
    st.subheader("📅 SIP Calculator")
    st.markdown(f"Investing **₹{sip_amount:,}/month** for "
                f"**{sip_years} years** in {selected_fund[:40]}")

    sip_months     = sip_years * 12
    sip_days_total = sip_years * 252
    total_invested = sip_amount * sip_months

    np.random.seed(42)
    n_sip = min(n_sim, 3000)
    sip_paths = np.zeros((sip_days_total+1, n_sip))
    for sim in range(n_sip):
        portfolio = 0
        dr = np.random.normal(
            mu_adjusted/100, sigma_real/100,
            sip_days_total)
        for day in range(sip_days_total):
            if day % 21 == 0:
                portfolio += sip_amount
            portfolio = portfolio*(1+dr[day])
            sip_paths[day+1, sim] = portfolio

    final_sip    = sip_paths[-1]
    expected_sip = final_sip.mean()
    gain_sip     = expected_sip - total_invested
    xirr         = ((expected_sip/total_invested)**(1/sip_years)-1)*100
    prob_sip     = (final_sip > total_invested).mean()*100

    st.markdown(f"""
    <div style="background:linear-gradient(135deg,#0d1b2a,#1b263b);
                border:2px solid #2196F3;
                padding:24px;border-radius:16px;margin:12px 0">
        <h2 style="color:#2196F3;margin:0 0 16px 0">
            📊 SIP Projection for {sip_years} Years
        </h2>
        <div style="display:grid;
                    grid-template-columns:1fr 1fr 1fr 1fr 1fr;
                    gap:10px">
            <div style="background:rgba(33,150,243,0.15);
                        border:1px solid #2196F3;
                        padding:12px;border-radius:10px;
                        text-align:center">
                <div style="color:#64B5F6;font-size:11px">
                    Monthly SIP</div>
                <div style="color:white;font-size:16px;
                            font-weight:bold">
                    ₹{sip_amount:,}</div>
            </div>
            <div style="background:rgba(244,67,54,0.15);
                        border:1px solid #F44336;
                        padding:12px;border-radius:10px;
                        text-align:center">
                <div style="color:#EF9A9A;font-size:11px">
                    Total Invested</div>
                <div style="color:white;font-size:16px;
                            font-weight:bold">
                    ₹{total_invested:,}</div>
            </div>
            <div style="background:rgba(76,175,80,0.15);
                        border:1px solid #4CAF50;
                        padding:12px;border-radius:10px;
                        text-align:center">
                <div style="color:#81C784;font-size:11px">
                    Expected Corpus</div>
                <div style="color:#4CAF50;font-size:16px;
                            font-weight:bold">
                    ₹{expected_sip:,.0f}</div>
            </div>
            <div style="background:rgba(255,193,7,0.15);
                        border:1px solid #FFC107;
                        padding:12px;border-radius:10px;
                        text-align:center">
                <div style="color:#FFD54F;font-size:11px">
                    Wealth Gain</div>
                <div style="color:#FFD700;font-size:16px;
                            font-weight:bold">
                    +₹{gain_sip:,.0f}</div>
            </div>
            <div style="background:rgba(156,39,176,0.15);
                        border:1px solid #9C27B0;
                        padding:12px;border-radius:10px;
                        text-align:center">
                <div style="color:#CE93D8;font-size:11px">
                    Est. XIRR</div>
                <div style="color:#CE93D8;font-size:16px;
                            font-weight:bold">
                    {xirr:.1f}%</div>
            </div>
        </div>
        <div style="margin-top:14px;
                    background:rgba(255,255,255,0.05);
                    padding:10px;border-radius:8px">
            <span style="color:#81C784">
                ✅ Profit probability: <strong>{prob_sip:.1f}%</strong>
            </span> &nbsp;&nbsp;
            <span style="color:#FFD54F">
                💰 Wealth multiplier:
                <strong>{expected_sip/total_invested:.1f}x</strong>
            </span>
        </div>
    </div>
    """, unsafe_allow_html=True)

    col1,col2 = st.columns(2)
    with col1:
        fig_s1,ax_s1 = plt.subplots(figsize=(8,4))
        ax_s1.plot(sip_paths[:,:200], alpha=0.02,
                   color="steelblue", linewidth=0.5)
        ax_s1.plot(sip_paths.mean(axis=1),
                   color="orange", linewidth=2.5,
                   label="Expected corpus")
        inv_line = [min((d//21)*sip_amount,
                        total_invested)
                    for d in range(sip_days_total+1)]
        ax_s1.plot(inv_line, color="red",
                   linewidth=2, linestyle="--",
                   label="Amount invested")
        ax_s1.set_title(f"SIP Growth — {sip_years} Years")
        ax_s1.set_xlabel("Trading Days")
        ax_s1.set_ylabel("Portfolio Value (₹)")
        ax_s1.legend(fontsize=8)
        ax_s1.grid(True, alpha=0.3)
        st.pyplot(fig_s1)
        plt.close()

    with col2:
        fig_s2,ax_s2 = plt.subplots(figsize=(8,4))
        ax_s2.hist(final_sip, bins=60,
                   color="steelblue",
                   edgecolor="white", alpha=0.8)
        ax_s2.axvline(total_invested, color="red",
                      linewidth=2, linestyle="--",
                      label=f"Invested: ₹{total_invested:,}")
        ax_s2.axvline(expected_sip, color="green",
                      linewidth=2.5,
                      label=f"Expected: ₹{expected_sip:,.0f}")
        ax_s2.set_title("Final Corpus Distribution")
        ax_s2.set_xlabel("Final Value (₹)")
        ax_s2.legend(fontsize=8)
        ax_s2.grid(True, alpha=0.3)
        st.pyplot(fig_s2)
        plt.close()

    # Year by year
    st.subheader("📆 Year by Year Breakdown")
    yr_data = []
    for yr in range(1, sip_years+1):
        idx = min(yr*252, sip_days_total)
        inv = min(yr*12*sip_amount, total_invested)
        ev  = sip_paths[idx].mean()
        prob= (sip_paths[idx]>inv).mean()*100
        yr_date = invest_date + timedelta(days=yr*365)
        yr_data.append({
            "Year"      : f"Year {yr}",
            "By Date"   : yr_date.strftime("%b %Y"),
            "Invested"  : f"₹{inv:,.0f}",
            "Expected"  : f"₹{ev:,.0f}",
            "Gain"      : f"+₹{ev-inv:,.0f}",
            "Probability": f"{prob:.0f}%",
            "Multiplier": f"{ev/inv:.2f}x"
        })
    st.dataframe(pd.DataFrame(yr_data),
                 use_container_width=True, hide_index=True)

    # SIP vs Lump Sum
    st.subheader("⚖️ SIP vs Lump Sum")
    lump = total_invested
    np.random.seed(42)
    lr   = np.random.normal(mu_adjusted/100,
                            sigma_real/100,
                            (sip_days_total, 3000))
    lp   = np.zeros((sip_days_total+1, 3000))
    lp[0]= lump
    for d in range(1, sip_days_total+1):
        lp[d] = lp[d-1]*(1+lr[d-1])
    exp_lump = lp[-1].mean()

    lc1,lc2 = st.columns(2)
    lc1.metric(f"SIP ₹{sip_amount:,}/mo × {sip_months}mo",
               f"₹{expected_sip:,.0f}",
               f"+₹{expected_sip-total_invested:,.0f}")
    lc2.metric(f"Lump Sum ₹{lump:,} upfront",
               f"₹{exp_lump:,.0f}",
               f"+₹{exp_lump-lump:,.0f}")
    if expected_sip > exp_lump:
        st.success(f"✅ SIP wins by ₹{expected_sip-exp_lump:,.0f}!")
    else:
        st.info(f"💡 Lump sum wins by ₹{exp_lump-expected_sip:,.0f}")

# ══════════════════════════════
# TAB 3: Fund Analysis
# ══════════════════════════════
with tab3:
    st.subheader(f"📈 {selected_fund[:50]}")
    col1,col2 = st.columns(2)
    with col1:
        fig3,ax3 = plt.subplots(figsize=(8,3))
        ax3.plot(fund_df["Date"],
                 fund_df["NAV_Value"],
                 color="steelblue", linewidth=1.5)
        ax3.set_title("NAV Price History")
        ax3.set_xlabel("Date")
        ax3.set_ylabel("NAV (₹)")
        ax3.grid(True, alpha=0.3)
        plt.tight_layout()
        st.pyplot(fig3)
        plt.close()
    with col2:
        fig4,ax4 = plt.subplots(figsize=(8,3))
        returns = fund_df["Daily_Return_%"].dropna()
        ax4.hist(returns, bins=60,
                 color="coral",
                 edgecolor="white", alpha=0.8)
        ax4.axvline(mu_real, color="green",
                    linewidth=2,
                    label=f"Mean: {mu_real:.4f}%")
        ax4.set_title("Daily Return Distribution")
        ax4.set_xlabel("Daily Return %")
        ax4.legend(fontsize=9)
        ax4.grid(True, alpha=0.3)
        plt.tight_layout()
        st.pyplot(fig4)
        plt.close()

    # Live market chart
    if nifty is not None:
        st.subheader("📡 Live Nifty 50 Chart")
        fig_n,ax_n = plt.subplots(figsize=(12,3))
        nifty_plot = nifty.tail(252)
        close_vals = nifty_plot["Close"].values.flatten()
        ax_n.plot(nifty_plot.index,
                  close_vals,
                  color="orange", linewidth=1.5)
        close_vals = nifty_plot["Close"].values.flatten()
        ax_n.fill_between(nifty_plot.index,
                          close_vals,
                          close_vals.min(),
                          alpha=0.1, color="orange")
        ax_n.set_title("Nifty 50 — Last 1 Year (Live)")
        ax_n.set_xlabel("Date")
        ax_n.set_ylabel("Index Value")
        ax_n.grid(True, alpha=0.3)
        plt.tight_layout()
        st.pyplot(fig_n)
        plt.close()

    stats = pd.DataFrame({
        "Metric" : ["Daily Return","Annual Return",
                    "Volatility","Sharpe","Alpha",
                    "Beta","Expense","Risk","NAV",
                    "1Y Return"],
        "Value"  : [f"{mu_real:.4f}%",
                    f"{mu_real*252:.2f}%",
                    f"{sigma_real:.4f}%",
                    f"{sharpe_val:.3f}",
                    f"{alpha_val:.3f}",
                    f"{beta_val:.3f}",
                    f"{expense:.2f}%",
                    risk_level,
                    f"₹{latest_nav:.2f}",
                    f"{ret_1y:.2f}%"]
    })
    st.dataframe(stats, use_container_width=True,
                 hide_index=True)

# ══════════════════════════════
# TAB 4: Platform Guide
# ══════════════════════════════
with tab4:
    st.subheader("🏦 Best Platform for You")

    if investment < 10000 or sip_amount < 1000:
        top = "Groww"; reason = "Best for small amounts"
    elif investment < 100000:
        top = "Kuvera"; reason = "Best free direct fund platform"
    else:
        top = "Zerodha Coin"; reason = "Best for large investments"

    st.success(f"🥇 **Recommended: {top}** — {reason}")

    platforms = [
        ("🌱 Groww","groww.in","⭐⭐⭐⭐⭐",
         "Best for beginners · Zero commission · ₹100 min SIP"),
        ("🪙 Zerodha Coin","coin.zerodha.com","⭐⭐⭐⭐⭐",
         "Best analytics · Direct funds · Stocks+MF together"),
        ("💎 Kuvera","kuvera.in","⭐⭐⭐⭐⭐",
         "100% free · Tax harvesting · Goal planning"),
        ("💰 Paytm Money","paytmmoney.com","⭐⭐⭐⭐",
         "Instant UPI · SIP automation · Easy interface"),
        ("🏛️ MF Central","mfcentral.com","⭐⭐⭐⭐",
         "SEBI official · Most secure · All funds"),
    ]
    for name,url,rating,desc in platforms:
        with st.expander(f"{name} {rating}"):
            st.markdown(f"**{desc}**")
            st.markdown(f"🌐 {url}")

    st.divider()
    st.subheader("📌 How to Invest — Step by Step")
    st.info(f"""
**Step 1:** Download **{top}** app or go to website

**Step 2:** Complete KYC — need Aadhaar + PAN (5 minutes)

**Step 3:** Search: **{selected_fund[:50]}**

**Step 4:** Click **Invest** → Amount: **₹{investment:,}**

**Step 5:** Choose One-time or SIP ₹{sip_amount:,}/month

**Step 6:** Pay via UPI / Net banking

**Step 7:** Set reminder to withdraw:
           **{withdraw_date.strftime("%d %B %Y")}**

Expected: ₹{investment:,} → ₹{opt_val:,.0f} ({ret_pct:.1f}% return)
    """)

# ══════════════════════════════
# TAB 5: Compare Funds
# ══════════════════════════════
with tab5:
    st.subheader("⚖️ Compare Multiple Funds")
    all_funds    = sorted(df["Scheme_Name"].unique())
    compare_list = st.multiselect(
        "Select 2-5 funds:",
        all_funds, default=all_funds[:3])

    if len(compare_list) >= 2:
        cdata = []
        for fund in compare_list:
            fdf = df[df["Scheme_Name"]==fund]
            if len(fdf) > 252:
                r1y = ((fdf["NAV_Value"].iloc[-1] -
                        fdf["NAV_Value"].iloc[-252]) /
                       fdf["NAV_Value"].iloc[-252])*100
            else:
                r1y = fdf["Daily_Return_%"].mean()*252
            cdata.append({
                "Fund"    : fund[:35],
                "Sharpe"  : round(fdf["Sharpe"].mean(),3),
                "1Y Ret%" : round(r1y,2),
                "Alpha"   : round(fdf["Alpha"].mean(),3),
                "Beta"    : round(fdf["Beta"].mean(),3),
                "Expense" : round(fdf["Expense_Ratio"].mean(),3),
                "Risk"    : fdf["Risk_Level"].iloc[-1],
            })
        st.dataframe(pd.DataFrame(cdata),
                     use_container_width=True, hide_index=True)

        fig6,ax6 = plt.subplots(figsize=(12,4))
        for fund in compare_list:
            fdf = df[df["Scheme_Name"]==fund].sort_values("Date")
            norm = fdf["NAV_Value"]/fdf["NAV_Value"].iloc[0]*100
            ax6.plot(fdf["Date"], norm,
                     label=fund[:25], linewidth=1.5)
        ax6.set_title("Normalized NAV (Base=100)")
        ax6.set_xlabel("Date")
        ax6.set_ylabel("Normalized NAV")
        ax6.legend(fontsize=8)
        ax6.grid(True, alpha=0.3)
        plt.tight_layout()
        st.pyplot(fig6)
        plt.close()
    else:
        st.warning("Select at least 2 funds.")

st.divider()
st.markdown(
    "*Q-FinOpt v3.0 · Live Market Data · SIP Calculator · "
    "XGBoost + LSTM + Monte Carlo + QAOA · MANI SAI*")
