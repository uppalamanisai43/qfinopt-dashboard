<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/Backend-FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white"/>
<img src="https://img.shields.io/badge/ML-scikit--learn-F7931E?style=for-the-badge&logo=scikit-learn&logoColor=white"/>
<img src="https://img.shields.io/badge/SEBI-Compliant-blue?style=for-the-badge"/>

# 📈 QFinOpt — AI-Powered Mutual Fund Research App

### *Democratizing Professional-Grade Quantitative Finance for Every Indian Investor*

[📥 Download APK](./QFinOpt.apk) · [📖 API Docs](http://localhost:8000/docs)

</div>

---

## 🌟 What is QFinOpt?

**QFinOpt** (Quantitative Finance Optimizer) is a full-stack AI-powered Mutual Fund research and portfolio management application built for Indian retail investors.

> 97% of Indian mutual fund investors are in **Regular Plans** — unknowingly paying ₹50,000 to ₹3,00,000 extra in agent commissions over 10 years. QFinOpt gives every investor access to the same quantitative tools used by professional fund managers — **for free**.

### 🎯 Core Problem Solved

| Problem | QFinOpt Solution |
|---|---|
| No risk-adjusted metrics for retail investors | Sharpe Ratio, Alpha, Beta, Max Drawdown on every fund |
| No AI-based return forecasting | Gradient Boosting ML model with 74% directional accuracy |
| No scientific portfolio allocation | Markowitz Efficient Frontier optimizer (Nobel Prize mathematics) |
| Misleading single-number return projections | Monte Carlo GBM simulation — 1,000 future paths (Bull/Base/Bear) |
| Agent commission bias toward Regular Plans | Always promotes 0% commission Direct Plans |

---

## 📱 Screenshots

> *(Install the APK and explore — live market data, AI scores, Monte Carlo charts)*

---

## ✨ Key Features

### 🤖 AI & Machine Learning
- **ML Return Forecaster** — Gradient Boosting model trained on 5-year AMFI NAV history predicts 21-day forward returns with **74% directional accuracy**
- **Buy / Hold / Exit Score** — 0–100 AI confidence score per fund based on 12 quantitative features (RSI, momentum, volatility z-score, Sharpe, Alpha, Beta, Expense Ratio)
- **Market Sentiment Badge** — Real-time 🟢 BULLISH / 🔴 BEARISH indicator from Nifty 50 momentum

### 📊 Quantitative Finance Algorithms
- **Markowitz Efficient Frontier** — Quadratic optimization finds the mathematically optimal weight for each fund in your portfolio (max Sharpe Ratio)
- **Monte Carlo GBM Simulation** — 1,000-path Geometric Brownian Motion simulation for both SIP projections and Retirement Withdrawal planning
- **XIRR Calculator** — Accurate annualized return calculation accounting for timing of every SIP installment (bisection root-finding)
- **Sharpe Ratio** — Risk-adjusted return: `(Annual Return − 6.5% Risk-Free Rate) / Annual Volatility`
- **Alpha & Beta (CAPM)** — Measures fund manager skill (Alpha) and market sensitivity (Beta) vs Nifty 50
- **Maximum Drawdown** — Worst historical peak-to-trough loss detection

### 📱 App Screens
| Screen | Features |
|---|---|
| 🏠 **Dashboard** | Live Nifty/Sensex/Gold, Bullish/Bearish badge, Top AI-ranked funds, Fund filters |
| 💼 **Portfolio** | Add holdings, live P&L tracking, XIRR, pie chart allocation, Watchlist |
| 📊 **Analysis** | Deep AI analysis per fund — ML score, risk metrics, interactive NAV chart, Fund comparison |
| 📈 **SIP Planner** | Monte Carlo SIP simulation — Bull/Base/Bear projections over 1–30 years |
| 🏖️ **Retirement Planner** | Withdrawal simulation — corpus survival probability over retirement period |
| 🔍 **Explore** | Live AMFI search (14,000+ schemes), Brokers guide, Goal-based platform selector |
| 🔔 **Reminders** | Smart alerts (SIP due, ML exit, tax harvest), Calendar sync, WhatsApp share, PDF reports |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Android App (Kotlin)                      │
│         Jetpack Compose + Material 3 + MVVM                 │
│                                                             │
│  Dashboard → Portfolio → Analysis → Planning → Explore      │
└─────────────────────┬───────────────────────────────────────┘
                      │ REST API (HTTP/JSON)
┌─────────────────────▼───────────────────────────────────────┐
│                Python FastAPI Backend                       │
│                                                             │
│  ┌──────────────┐  ┌────────────────┐  ┌────────────────┐  │
│  │ fund_service │  │simulation_serv │  │  data_manager  │  │
│  │ (Sharpe,     │  │ (Monte Carlo   │  │  (87MB AMFI    │  │
│  │  Alpha, Beta,│  │  GBM, XIRR)    │  │   CSV + Live   │  │
│  │  ML Model)   │  │                │  │   NAV API)     │  │
│  └──────────────┘  └────────────────┘  └────────────────┘  │
│                                                             │
│  ┌──────────────┐  ┌────────────────┐  ┌────────────────┐  │
│  │portfolio_svc │  │  auth_service  │  │ market_service │  │
│  │ (Holdings,   │  │ (Register,     │  │ (yfinance      │  │
│  │  P&L, XIRR)  │  │  Login, Guest) │  │  Nifty/Gold)   │  │
│  └──────────────┘  └────────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                      Data Layer                             │
│  qfinopt_cleaned.csv (87MB) — 296 funds, 5–10 year NAV     │
│  mf_gb_model.joblib (554KB) — Trained Gradient Boosting ML  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Android App
| Layer | Technology |
|---|---|
| Language | Kotlin 100% |
| UI Framework | Jetpack Compose |
| Design System | Material Design 3 |
| Architecture | MVVM + Clean Architecture |
| HTTP Client | Retrofit 2 + OkHttp |
| State Management | Kotlin Coroutines + StateFlow |
| Charts | Custom Canvas (no third-party dependency) |

### Python Backend
| Component | Technology |
|---|---|
| Web Framework | FastAPI (async) |
| ML Model | scikit-learn `HistGradientBoostingRegressor` |
| Numerical Computing | NumPy, SciPy |
| Data Processing | Pandas |
| Live Market Data | yfinance (NSE/BSE) |
| PDF Generation | fpdf2 |
| Model Persistence | joblib |

---

## 🤖 ML Model Details

```
Algorithm    : Histogram Gradient Boosting Regressor (scikit-learn)
Target       : 21-day forward NAV return (regression → directional classification)
Training Set : 80% of time-ordered data (no future leakage)
Test Set     : 20% most recent data

Features (12):
  ret_5d, ret_10d, ret_21d    → Short/medium/monthly momentum
  vol_10d, vol_30d            → Volatility (risk level)
  mom_ratio                   → Risk-adjusted momentum
  rsi_14                      → Relative Strength Index
  nav_z                       → NAV z-score vs 60-day mean
  Sharpe, Alpha, Beta         → Historical risk-adjusted metrics
  Expense_Ratio               → Annual fund cost

Results:
  Directional Accuracy        : ~74%
  High-Conviction Win Rate    : Higher (top 25% predictions)
  Information Coefficient (IC): Positive (genuine predictive skill)
```

---

## 🚀 Getting Started

### Prerequisites
- Python 3.10+
- Android Studio (Hedgehog or later)
- Android phone (API 24 / Android 7.0+)
- Both PC and phone on the **same Wi-Fi network**

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/QFinOpt.git
cd QFinOpt
```

### 2. Set Up & Run the Backend
```bash
cd backend
python -m venv .venv

# Windows
.venv\Scripts\activate

# macOS / Linux
source .venv/bin/activate

pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Backend is now live at:
- **Swagger API Docs**: `http://localhost:8000/docs`
- **Health Check**: `http://localhost:8000/health`

### 3. Find Your PC's Local IP Address
```powershell
# Windows
ipconfig
# Look for: IPv4 Address e.g. 192.168.1.5
```

### 4. Install the Android App
**Option A — Wi-Fi Download (Easiest)**:
Open your phone browser → `http://<YOUR_IP>:8000/download` → Install APK

**Option B — Build from Source**:
```bash
cd android
./gradlew assembleDebug
# APK at: android/app/build/outputs/apk/debug/app-debug.apk
```

### 5. Connect App to Backend
Open the app → Tap the **⚙️ Server** icon → Enter `http://<YOUR_IP>:8000` → Save

---

## 📡 API Endpoints

| Category | Method | Endpoint | Description |
|---|---|---|---|
| Health | GET | `/health` | Server status check |
| Auth | POST | `/register` | Create user account |
| Auth | POST | `/login` | User login |
| Auth | POST | `/login/guest` | Guest access (no registration) |
| Market | GET | `/market` | Live Nifty, Sensex, Gold + Sentiment |
| Funds | GET | `/funds` | Filter funds by category/risk/returns |
| Funds | GET | `/funds/{id}/stats` | Deep AI analysis for one fund |
| Funds | GET | `/funds/{id}/history` | Historical NAV chart data |
| Analysis | GET | `/funds/compare` | Side-by-side fund comparison |
| Explore | GET | `/nav/search` | Live search across 14,000+ AMFI schemes |
| Platform | GET | `/platform-recommendation` | AI goal-based fund recommendation |
| SIP | POST | `/sip/simulate` | Monte Carlo SIP projection |
| Withdrawal | POST | `/withdrawal/simulate` | Retirement corpus simulation |
| Portfolio | GET | `/portfolio` | Get user portfolio with live P&L |
| Portfolio | POST | `/portfolio/holdings` | Add a fund holding |
| Portfolio | DELETE | `/portfolio/holdings/{id}` | Remove a holding |
| Watchlist | GET | `/watchlist` | Get watchlist |
| Watchlist | POST | `/watchlist/toggle` | Add/remove fund from watchlist |
| Reports | POST | `/reports/pdf` | Generate PDF report |
| Download | GET | `/download` | Download APK over Wi-Fi |
| **Quantum** | **POST** | **`/api/qaoa/optimize`** | **QAOA Quantum-Inspired Portfolio Optimizer (QUBO Hamiltonian simulation + Benchmark Table)** |

---

## 📁 Project Structure

```
QFinOpt/
├── 📱 android/                        # Android App (Kotlin + Compose)
│   └── app/src/main/java/com/qfinopt/app/
│       ├── MainActivity.kt            # Entry point + bottom navigation
│       ├── ui/screens/                # 6 app screens (Dashboard, Portfolio, Analysis, etc.)
│       │   ├── DashboardScreen.kt
│       │   ├── PortfolioScreen.kt
│       │   ├── AnalysisScreen.kt
│       │   ├── SipScreen.kt
│       │   ├── WithdrawalScreen.kt
│       │   └── RemindersScreen.kt
│       ├── ui/components/             # 9 reusable UI components
│       ├── ui/viewmodel/              # MainViewModel (StateFlow)
│       └── data/                      # API client, models, local storage
│
├── 🐍 backend/                        # Python FastAPI AI Engine
│   ├── app/
│   │   ├── main.py                    # 25+ REST API routes
│   │   ├── fund_service.py            # Sharpe, Alpha, Beta, ML scoring
│   │   ├── simulation_service.py      # Monte Carlo GBM + XIRR
│   │   ├── data_manager.py            # CSV loader + Live AMFI NAV
│   │   ├── portfolio_service.py       # Holdings CRUD + P&L
│   │   ├── auth_service.py            # User auth + tokens
│   │   ├── market_service.py          # Live Nifty/Sensex/Gold
│   │   └── pdf_service.py             # PDF report generation
│   └── data/
│       ├── qfinopt_cleaned.csv        # 87MB — 296 funds × 5–10yr NAV
│       └── mf_gb_model.joblib         # 554KB trained ML model
│
├── 🤖 train_save_model.py             # ML training script
├── 🤖 train_ensemble_model.py         # Ensemble model training
├── 📊 calculate_all_metrics.py        # Batch Sharpe/Alpha/Beta calculator
└── 📦 QFinOpt.apk                     # Pre-built Android APK (20MB)
```

---

## ⚖️ SEBI Regulatory Compliance

> **IMPORTANT DISCLAIMER**: QFinOpt is NOT a SEBI-registered Investment Adviser (RIA). All analysis, scores, and recommendations provided are for **educational and research purposes only** and do not constitute personalized investment advice.

- ✅ No investor money is collected, held, or processed
- ✅ All recommendations include statutory risk disclosures
- ✅ Always promotes Direct Plans (0% commission) over Regular Plans
- ✅ Compliant with SEBI (Research Analysts) Regulations, 2014

*"Mutual Fund investments are subject to market risks. Please read all scheme-related documents carefully before investing."*

---

## 🔮 Future Roadmap

- [ ] Expand fund coverage to all 14,000+ AMFI schemes
- [ ] Portfolio Overlap & Duplication X-Ray
- [ ] Crisis Stress-Tester (COVID 2020, 2008 GFC scenario simulation)
- [ ] Zero-Tax Harvester (LTCG ₹1.25L limit optimizer)
- [ ] FIRE / Retirement Freedom Planner
- [ ] Cloud deployment (always-on backend)
- [ ] Direct in-app investment (SEBI EOP registration + BSE StAR MF API)
- [ ] NSE/BSE Direct Stocks Mode

---

## 👨‍💻 Author

Built with ❤️ for Indian retail investors.

---

## 📄 License

This project is licensed under the MIT License — see [LICENSE](./LICENSE) for details.

---

<div align="center">

⭐ **If this project helped you, please star the repository!** ⭐

*QFinOpt — Professional Quantitative Finance, For Everyone.*

</div>
