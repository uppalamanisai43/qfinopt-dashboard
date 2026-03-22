
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
st.markdown("*Real-time fund analysis · Withdrawal timing · Platform advice*")
st.divider()

# ── Load real data ──
@st.cache_data
def load_data():
    file_id = "1EfNv54tjvjcsJdZhxYwa4zPJxl9q9_9X"
    url     = f"https://drive.google.com/uc?id={file_id}"
    df      = pd.read_csv(url)
    df["Date"] = pd.to_datetime(df["Date"])
    return df

with st.spinner("Loading live fund data..."):
    df = load_data()

# ── SIDEBAR: User inputs ──
st.sidebar.title("🎯 Your Investment Details")

# Fund selection
all_funds    = sorted(df["Scheme_Name"].unique().tolist())
categories   = sorted(df["Sheet_Category"].unique().tolist())
risk_levels  = sorted(df["Risk_Level"].unique().tolist())

st.sidebar.subheader("Filter Funds")
sel_category = st.sidebar.multiselect(
    "Fund Category", categories,
    default=categories[:3])
sel_risk = st.sidebar.multiselect(
    "Risk Level", risk_levels,
    default=risk_levels)

filtered_funds = df[
    (df["Sheet_Category"].isin(sel_category)) &
    (df["Risk_Level"].isin(sel_risk))
]["Scheme_Name"].unique().tolist()
filtered_funds = sorted(filtered_funds)

st.sidebar.subheader("Select Fund & Amount")
selected_fund = st.sidebar.selectbox(
    "Choose a Fund:", filtered_funds)
investment = st.sidebar.number_input(
    "Investment Amount (₹)",
    min_value=500, max_value=10000000,
    value=100000, step=500)
invest_date = st.sidebar.date_input(
    "Investment Date:", value=date.today())
n_sim = st.sidebar.selectbox(
    "Simulations", [1000,5000,10000], index=1)
st.sidebar.divider()
st.sidebar.caption("Data updates from your fund history")

# ── Get REAL fund statistics ──
fund_df = df[df["Scheme_Name"] == selected_fund].sort_values("Date")

if len(fund_df) < 30:
    st.error("Not enough data for this fund. Please select another.")
    st.stop()

# Real statistics from actual fund data
mu_real    = fund_df["Daily_Return_%"].mean()
sigma_real = fund_df["Daily_Return_%"].std()
sharpe_val = fund_df["Sharpe"].mean()
alpha_val  = fund_df["Alpha"].mean()
beta_val   = fund_df["Beta"].mean()
sd_val     = fund_df["SD"].mean()
expense    = fund_df["Expense_Ratio"].mean()
risk_level = fund_df["Risk_Level"].iloc[-1]
category   = fund_df["Sheet_Category"].iloc[-1]
latest_nav = fund_df["NAV_Value"].iloc[-1]
nav_1y_ago = fund_df["NAV_Value"].iloc[-252] if len(fund_df)>252 else fund_df["NAV_Value"].iloc[0]
real_1y_return = ((latest_nav - nav_1y_ago)/nav_1y_ago)*100

# ── Fund Info Card ──
st.subheader(f"📊 {selected_fund}")
c1,c2,c3,c4,c5,c6 = st.columns(6)
c1.metric("Latest NAV",    f"₹{latest_nav:.2f}")
c2.metric("1Y Return",     f"{real_1y_return:.2f}%",
          f"{real_1y_return:.1f}%")
c3.metric("Sharpe Ratio",  f"{sharpe_val:.3f}")
c4.metric("Alpha",         f"{alpha_val:.3f}")
c5.metric("Beta",          f"{beta_val:.3f}")
c6.metric("Expense Ratio", f"{expense:.2f}%")
st.divider()

# ── TABS ──
tab1,tab2,tab3,tab4 = st.tabs([
    "📅 Withdrawal Timing",
    "📈 Fund Analysis",
    "🏦 Platform Guide",
    "⚖️ Compare Funds"])

# ══════════════════════════════════════════
# TAB 1: REAL Monte Carlo + Withdrawal Date
# ══════════════════════════════════════════
with tab1:
    st.subheader("When Should You Withdraw?")
    st.markdown(f"Running **{n_sim:,} simulations** using "
                f"**real data** from {selected_fund[:40]}")

    # Monte Carlo using REAL fund statistics
    hold_days = 504  # 2 years
    np.random.seed(42)
    daily_ret = np.random.normal(
        mu_real/100, sigma_real/100,
        (hold_days, n_sim))
    paths = np.zeros((hold_days+1, n_sim))
    paths[0] = investment
    for d in range(1, hold_days+1):
        paths[d] = paths[d-1]*(1+daily_ret[d-1])

    exp_v    = paths.mean(axis=1)
    prob_g   = (paths > investment).mean(axis=1)
    vol      = paths.std(axis=1)
    risk_adj = (exp_v - investment)/(vol+1)

    # Find optimal day
    opt_day  = int(np.argmax(risk_adj))
    opt_val  = exp_v[opt_day]
    gain     = opt_val - investment
    ret_pct  = (gain/investment)*100

    # Calculate REAL withdrawal date (skip weekends)
    current = datetime.combine(invest_date,
                               datetime.min.time())
    count = 0
    while count < opt_day:
        current += timedelta(days=1)
        if current.weekday() < 5:
            count += 1
    withdraw_date = current.date()

    # Also find 3m, 6m, 1y, 2y returns
    returns_by_period = {}
    for label, days in [("3 months",63),
                        ("6 months",126),
                        ("1 year",252),
                        ("2 years",504)]:
        if days <= hold_days:
            v = exp_v[days]
            returns_by_period[label] = {
                "val" : v,
                "pct" : ((v-investment)/investment)*100,
                "prob": prob_g[days]*100
            }

    # BIG recommendation box
    st.markdown(f"""
    <div style="background:linear-gradient(135deg,#0d1b2a,#1b263b);
                border:2px solid #4CAF50;
                padding:24px;border-radius:16px;margin:12px 0">
        <h2 style="color:#4CAF50;margin:0 0 6px 0">
            🎯 Your Personalised Recommendation
        </h2>
        <p style="color:#aaa;margin:0 0 20px 0;font-size:14px">
            Based on {len(fund_df):,} days of real {selected_fund[:35]} data
        </p>
        <div style="display:grid;
                    grid-template-columns:1fr 1fr 1fr 1fr;
                    gap:12px;margin-bottom:16px">
            <div style="background:rgba(76,175,80,0.15);
                        border:1px solid #4CAF50;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#81C784;font-size:12px;
                            margin-bottom:4px">📅 Invest On</div>
                <div style="color:white;font-size:16px;
                            font-weight:bold">
                    {invest_date.strftime("%d %b %Y")}
                </div>
            </div>
            <div style="background:rgba(255,215,0,0.15);
                        border:1px solid #FFD700;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#FFD700;font-size:12px;
                            margin-bottom:4px">💰 Withdraw On</div>
                <div style="color:#FFD700;font-size:16px;
                            font-weight:bold">
                    {withdraw_date.strftime("%d %b %Y")}
                </div>
                <div style="color:#aaa;font-size:11px">
                    Day {opt_day} (~{opt_day//21} months)
                </div>
            </div>
            <div style="background:rgba(33,150,243,0.15);
                        border:1px solid #2196F3;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#64B5F6;font-size:12px;
                            margin-bottom:4px">💼 Expected Value</div>
                <div style="color:white;font-size:16px;
                            font-weight:bold">
                    ₹{opt_val:,.0f}
                </div>
            </div>
            <div style="background:rgba(156,39,176,0.15);
                        border:1px solid #9C27B0;
                        padding:14px;border-radius:10px;
                        text-align:center">
                <div style="color:#CE93D8;font-size:12px;
                            margin-bottom:4px">📈 Expected Gain</div>
                <div style="color:#CE93D8;font-size:16px;
                            font-weight:bold">
                    +₹{gain:,.0f}
                </div>
                <div style="color:#aaa;font-size:11px">
                    {ret_pct:.1f}% return
                </div>
            </div>
        </div>
        <div style="background:rgba(255,255,255,0.05);
                    padding:10px 14px;border-radius:8px">
            <span style="color:#81C784">✅ Profit probability: 
            <strong>{prob_g[opt_day]*100:.1f}%</strong></span>
            &nbsp;&nbsp;
            <span style="color:#64B5F6">📊 Fund risk: 
            <strong>{risk_level}</strong></span>
            &nbsp;&nbsp;
            <span style="color:#CE93D8">📁 Category: 
            <strong>{category}</strong></span>
        </div>
    </div>
    """, unsafe_allow_html=True)

    # Period comparison
    st.subheader("📆 Returns by Holding Period")
    cols = st.columns(len(returns_by_period))
    for col, (period, data) in zip(cols,
                                    returns_by_period.items()):
        col.metric(
            period,
            f"₹{data['val']:,.0f}",
            f"+{data['pct']:.1f}% | {data['prob']:.0f}% chance")

    # Charts
    col1, col2 = st.columns(2)
    with col1:
        fig1, ax1 = plt.subplots(figsize=(8,4))
        show = min(500, n_sim)
        ax1.plot(paths[:,:show], alpha=0.02,
                 color="steelblue", linewidth=0.5)
        ax1.plot(exp_v, color="orange",
                 linewidth=2.5, label="Expected")
        ax1.plot(
            [np.percentile(paths[d],5)
             for d in range(hold_days+1)],
            color="red", linewidth=1.5,
            linestyle="--", label="Worst 5%")
        ax1.plot(
            [np.percentile(paths[d],95)
             for d in range(hold_days+1)],
            color="green", linewidth=1.5,
            linestyle="--", label="Best 95%")
        ax1.axhline(investment, color="white",
                    linestyle=":", linewidth=1,
                    label="Your investment")
        ax1.axvline(opt_day, color="yellow",
                    linestyle="--", linewidth=2,
                    label=f"Withdraw Day {opt_day}")
        ax1.set_title(f"Monte Carlo: {n_sim:,} Scenarios")
        ax1.set_xlabel("Trading Days")
        ax1.set_ylabel("Portfolio Value (₹)")
        ax1.legend(fontsize=7)
        ax1.grid(True, alpha=0.2)
        st.pyplot(fig1)
        plt.close()

    with col2:
        fig2, ax2 = plt.subplots(figsize=(8,4))
        days_range = range(hold_days+1)
        ax2.plot(days_range, prob_g*100,
                 color="green", linewidth=2.5)
        ax2.fill_between(days_range, prob_g*100, 50,
            where=[p>50 for p in prob_g],
            alpha=0.3, color="green",
            label="Profit zone")
        ax2.fill_between(days_range, prob_g*100, 50,
            where=[p<=50 for p in prob_g],
            alpha=0.3, color="red",
            label="Loss zone")
        ax2.axvline(opt_day, color="yellow",
                    linewidth=2.5, linestyle="--",
                    label=f"Optimal: Day {opt_day}")
        ax2.axhline(50, color="white",
                    linewidth=1, linestyle=":")
        ax2.set_title("Probability of Profit Over Time")
        ax2.set_xlabel("Trading Days")
        ax2.set_ylabel("Probability (%)")
        ax2.legend(fontsize=8)
        ax2.set_ylim(0,100)
        ax2.grid(True, alpha=0.2)
        st.pyplot(fig2)
        plt.close()

    # Scenario table
    st.subheader("🎲 Scenario Breakdown at Optimal Exit")
    sc1,sc2,sc3,sc4 = st.columns(4)
    p5  = np.percentile(paths[opt_day],5)
    p25 = np.percentile(paths[opt_day],25)
    p75 = np.percentile(paths[opt_day],75)
    p95 = np.percentile(paths[opt_day],95)
    sc1.metric("🐻 Worst (5%)",
               f"₹{p5:,.0f}",
               f"{((p5-investment)/investment)*100:.1f}%")
    sc2.metric("📉 Conservative (25%)",
               f"₹{p25:,.0f}",
               f"{((p25-investment)/investment)*100:.1f}%")
    sc3.metric("📈 Optimistic (75%)",
               f"₹{p75:,.0f}",
               f"{((p75-investment)/investment)*100:.1f}%")
    sc4.metric("🚀 Best (95%)",
               f"₹{p95:,.0f}",
               f"{((p95-investment)/investment)*100:.1f}%")

# ══════════════════════════════════════
# TAB 2: Real Fund Analysis
# ══════════════════════════════════════
with tab2:
    st.subheader(f"📈 Fund Deep Analysis: {selected_fund[:50]}")

    col1, col2 = st.columns(2)
    with col1:
        # NAV history
        fig3, ax3 = plt.subplots(figsize=(8,3))
        ax3.plot(fund_df["Date"], fund_df["NAV_Value"],
                 color="steelblue", linewidth=1.5)
        ax3.set_title("NAV Price History")
        ax3.set_xlabel("Date")
        ax3.set_ylabel("NAV (₹)")
        ax3.grid(True, alpha=0.3)
        plt.tight_layout()
        st.pyplot(fig3)
        plt.close()

    with col2:
        # Daily return distribution
        fig4, ax4 = plt.subplots(figsize=(8,3))
        returns = fund_df["Daily_Return_%"].dropna()
        ax4.hist(returns, bins=60,
                 color="coral", edgecolor="white", alpha=0.8)
        ax4.axvline(mu_real, color="green",
                    linewidth=2,
                    label=f"Mean: {mu_real:.4f}%")
        ax4.axvline(0, color="red",
                    linewidth=1, linestyle="--")
        ax4.set_title("Daily Return Distribution")
        ax4.set_xlabel("Daily Return %")
        ax4.legend(fontsize=9)
        ax4.grid(True, alpha=0.3)
        plt.tight_layout()
        st.pyplot(fig4)
        plt.close()

    # Rolling metrics
    fig5, axes = plt.subplots(1,3, figsize=(14,3))
    if "Rolling_Sharpe" in fund_df.columns:
        axes[0].plot(fund_df["Date"],
                     fund_df["Rolling_Sharpe"],
                     color="purple", linewidth=1)
        axes[0].set_title("Rolling Sharpe")
        axes[0].grid(True, alpha=0.3)
    if "Alpha" in fund_df.columns:
        axes[1].plot(fund_df["Date"],
                     fund_df["Alpha"],
                     color="green", linewidth=1)
        axes[1].axhline(0, color="red",
                        linestyle="--", linewidth=1)
        axes[1].set_title("Alpha Over Time")
        axes[1].grid(True, alpha=0.3)
    if "Rolling_Return_1Y_%" in fund_df.columns:
        axes[2].plot(fund_df["Date"],
                     fund_df["Rolling_Return_1Y_%"],
                     color="orange", linewidth=1)
        axes[2].axhline(0, color="red",
                        linestyle="--", linewidth=1)
        axes[2].set_title("1Y Rolling Return %")
        axes[2].grid(True, alpha=0.3)
    plt.tight_layout()
    st.pyplot(fig5)
    plt.close()

    # Fund stats table
    st.subheader("📋 Full Statistics")
    stats = pd.DataFrame({
        "Metric" : ["Daily Return (avg)","Annual Return (est)",
                    "Volatility (SD)","Sharpe Ratio",
                    "Alpha","Beta","Expense Ratio",
                    "Risk Level","Category",
                    "Latest NAV","1Y Return"],
        "Value"  : [f"{mu_real:.4f}%",
                    f"{mu_real*252:.2f}%",
                    f"{sigma_real:.4f}%",
                    f"{sharpe_val:.3f}",
                    f"{alpha_val:.3f}",
                    f"{beta_val:.3f}",
                    f"{expense:.2f}%",
                    risk_level, category,
                    f"₹{latest_nav:.2f}",
                    f"{real_1y_return:.2f}%"]
    })
    st.dataframe(stats, use_container_width=True,
                 hide_index=True)

# ══════════════════════════════════════
# TAB 3: Platform Guide
# ══════════════════════════════════════
with tab3:
    st.subheader("🏦 Best Platforms to Invest")

    if investment < 10000:
        top = "Groww"
        reason = "Best for small amounts with zero minimum SIP"
    elif investment < 100000:
        top = "Kuvera"
        reason = "Best free direct fund platform for mid-range"
    else:
        top = "Zerodha Coin"
        reason = "Best analytics and portfolio tools for large amounts"

    st.success(f"🥇 **Recommended for you: {top}** — {reason}")

    platforms = [
        ("🌱 Groww","groww.in","⭐⭐⭐⭐⭐",
         "Best for beginners","₹100 min SIP",
         "Zero commission · Simple app · Instant KYC · Good for SIP"),
        ("🪙 Zerodha Coin","coin.zerodha.com","⭐⭐⭐⭐⭐",
         "Best for active investors","₹1,000 min",
         "Direct funds · Best analytics · Stocks+MF together"),
        ("💎 Kuvera","kuvera.in","⭐⭐⭐⭐⭐",
         "Best free platform","₹500 min",
         "100% free · Tax harvesting · Goal planning · Family accounts"),
        ("💰 Paytm Money","paytmmoney.com","⭐⭐⭐⭐",
         "Best UPI integration","₹100 min",
         "Instant UPI invest · Direct funds · SIP automation"),
        ("🏛️ MF Central","mfcentral.com","⭐⭐⭐⭐",
         "Official SEBI platform","₹500 min",
         "Government backed · Most secure · All funds · Completely free"),
    ]

    for name, url, rating, type_, min_inv, desc in platforms:
        with st.expander(f"{name} {rating} — {type_}"):
            c1,c2 = st.columns([2,1])
            with c1:
                st.markdown(f"**{desc}**")
                st.markdown(f"Minimum: {min_inv}")
            with c2:
                st.markdown(f"🌐 **{url}**")

    st.divider()
    st.subheader("📌 Step-by-Step: How to Invest in " +
                 selected_fund[:30])
    st.info(f"""
**Step 1:** Open **{top}** app or website ({top.lower().replace(" ","")}.in)

**Step 2:** Complete KYC (takes 5 minutes — need Aadhaar + PAN)

**Step 3:** Search for: **{selected_fund[:50]}**

**Step 4:** Click **"Invest"** → Enter amount: **₹{investment:,}**

**Step 5:** Choose **"One time"** (lump sum) or **"SIP"** (monthly)

**Step 6:** Complete payment via UPI / Net banking

**Step 7:** Set a reminder to withdraw on:
           **{withdraw_date.strftime("%d %B %Y")}**

**Expected outcome:** ₹{investment:,} → ₹{opt_val:,.0f}
({ret_pct:.1f}% return in {opt_day//21} months)
    """)

# ══════════════════════════════════════
# TAB 4: Compare Funds
# ══════════════════════════════════════
with tab4:
    st.subheader("⚖️ Compare Multiple Funds")
    compare_funds = st.multiselect(
        "Select 2-5 funds to compare:",
        all_funds,
        default=all_funds[:3])

    if len(compare_funds) >= 2:
        compare_data = []
        for fund in compare_funds:
            fdf = df[df["Scheme_Name"]==fund]
            if len(fdf) > 252:
                nav_now  = fdf["NAV_Value"].iloc[-1]
                nav_1y   = fdf["NAV_Value"].iloc[-252]
                ret_1y   = ((nav_now-nav_1y)/nav_1y)*100
            else:
                ret_1y = fdf["Daily_Return_%"].mean()*252
            compare_data.append({
                "Fund"        : fund[:35],
                "Sharpe"      : round(fdf["Sharpe"].mean(),3),
                "1Y Return %"  : round(ret_1y,2),
                "Alpha"       : round(fdf["Alpha"].mean(),3),
                "Beta"        : round(fdf["Beta"].mean(),3),
                "Expense %"   : round(fdf["Expense_Ratio"].mean(),3),
                "Risk"        : fdf["Risk_Level"].iloc[-1],
            })

        comp_df = pd.DataFrame(compare_data)
        st.dataframe(comp_df, use_container_width=True,
                     hide_index=True)

        # NAV comparison chart
        fig6, ax6 = plt.subplots(figsize=(12,4))
        for fund in compare_funds:
            fdf = df[df["Scheme_Name"]==fund].sort_values("Date")
            # Normalize to 100 for fair comparison
            norm = fdf["NAV_Value"] / fdf["NAV_Value"].iloc[0] * 100
            ax6.plot(fdf["Date"], norm,
                     label=fund[:25], linewidth=1.5)
        ax6.set_title("Normalized NAV Comparison (Base=100)")
        ax6.set_xlabel("Date")
        ax6.set_ylabel("Normalized NAV")
        ax6.legend(fontsize=8)
        ax6.grid(True, alpha=0.3)
        plt.tight_layout()
        st.pyplot(fig6)
        plt.close()
    else:
        st.warning("Please select at least 2 funds to compare.")

st.divider()
st.markdown(
    "*Q-FinOpt v2.0 Live · Real-time analysis · "
    "XGBoost + LSTM + Monte Carlo + QAOA · "
    "Built by MANI SAI · 308 Indian Mutual Funds*")
