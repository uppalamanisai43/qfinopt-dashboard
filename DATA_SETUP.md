# Dataset & Model — Download Instructions

The following large files are **NOT included** in this GitHub repository due to size limits (GitHub max: 100MB per file):

| File | Size | Description |
|---|---|---|
| `backend/data/qfinopt_cleaned.csv` | ~87 MB | 296 curated mutual funds — 5 to 10 years of daily NAV history from AMFI |
| `backend/data/mf_gb_model.joblib` | ~554 KB | Pre-trained Gradient Boosting ML model |

---

## Option 1: Train the Model Yourself (Recommended)

If you have the dataset, train the model fresh:

```bash
cd QFinOpt
python train_save_model.py
```

This will generate `backend/data/mf_gb_model.joblib` and `backend/data/mf_gb_meta.json` automatically.

---

## Option 2: Download the Dataset from AMFI

You can download NAV data directly from AMFI's official portal:

1. Visit: https://www.amfiindia.com/net-asset-value/nav-history
2. Select fund schemes and download historical NAV data
3. Clean and format to match the CSV schema below

---

## CSV Schema

The `qfinopt_cleaned.csv` file follows this schema:

```
Scheme_Code    : int     — AMFI Scheme Code (unique fund identifier)
Scheme_Name    : str     — Full fund name (e.g., "Axis Bluechip Fund - Direct Plan")
Date           : date    — NAV date (YYYY-MM-DD format)
NAV_Value      : float   — Net Asset Value on that date
Daily_Return_% : float   — Percentage return vs previous day
Sharpe         : float   — Pre-computed annualized Sharpe Ratio
Alpha          : float   — CAPM Alpha vs Nifty 50
Beta           : float   — CAPM Beta vs Nifty 50
Expense_Ratio  : float   — Annual expense ratio (%)
Category       : str     — Fund category (e.g., "Large Cap", "ELSS", "Debt")
Risk_Level     : str     — Risk level ("Low", "Moderate", "High", "Very High")
```
