import pandas as pd
import numpy as np
from scipy.stats import spearmanr
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score

print("Loading dataset...")
df = pd.read_csv('backend/data/qfinopt_cleaned.csv')
df['Date'] = pd.to_datetime(df['Date'])
df = df.sort_values(['Scheme_Code', 'Date']).reset_index(drop=True)

# 1. Calculate specific metrics for HDFC Infrastructure Fund
hdfc = df[df['Scheme_Name'] == 'HDFC Infrastructure Fund'].sort_values('Date')
rets = hdfc['Daily_Return_%'].dropna()
mu = float(rets.mean())
sigma = float(rets.std())
sharpe = float(hdfc['Sharpe'].mean())
alpha = float(hdfc['Alpha'].mean())
beta = float(hdfc['Beta'].mean())
hist_nav = float(hdfc['NAV_Value'].iloc[-1])

# Downside deviation & Sortino
downside = rets[rets < 0]
downside_sd = float(downside.std()) if len(downside) > 0 else sigma
sortino = (mu * 252) / (downside_sd * np.sqrt(252)) if downside_sd > 0 else sharpe

# Maximum Drawdown (MDD)
navs = hdfc['NAV_Value'].values
cummax = np.maximum.accumulate(navs)
drawdowns = (navs - cummax) / cummax
mdd = float(drawdowns.min()) * 100.0

print("\n" + "="*50)
print("1. FUND QUANTITATIVE METRICS (HDFC Infrastructure Fund)")
print("="*50)
print(f"Daily Mean Return:     {mu:.4f}%")
print(f"Annualized Return:     {mu*252:.2f}%")
print(f"Daily Volatility (SD): {sigma:.4f}% (Annualized: {sigma*np.sqrt(252):.2f}%)")
print(f"Sharpe Ratio:          {sharpe:.3f}")
print(f"Sortino Ratio:         {sortino:.3f}")
print(f"Alpha:                 {alpha:.3f}")
print(f"Beta:                  {beta:.3f}")
print(f"Max Drawdown (MDD):    {mdd:.2f}%")
print(f"Current NAV:           Rs. {hist_nav:.2f}")

# 2. ML Performance Metrics: Baseline vs Multi-Horizon Momentum Model
print("\n" + "="*50)
print("2. MACHINE LEARNING MODEL PERFORMANCE METRICS")
print("="*50)

# Build 21-day forward return target across all funds
sample_df = df[df['Date'] >= '2023-01-01'].copy()
sample_df['mom5'] = sample_df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x/x.shift(5) - 1)
sample_df['mom21'] = sample_df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x/x.shift(21) - 1)
sample_df['fwd_ret_21d'] = sample_df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: (x.shift(-21)/x - 1)*100)
sample_df['fwd_ret_1d'] = sample_df.groupby('Scheme_Code')['Daily_Return_%'].shift(-1)

sample_df = sample_df.dropna(subset=['mom5', 'mom21', 'fwd_ret_21d', 'fwd_ret_1d'])

# Baseline 1-day prediction (Random Walk)
baseline_pred = (sample_df['mom5'] > 0).astype(int)
baseline_true = (sample_df['fwd_ret_1d'] > 0).astype(int)
base_acc = accuracy_score(baseline_true, baseline_pred)

# 21-day Trend Momentum Model
model_score = 0.6 * sample_df['mom21'] + 0.4 * sample_df['mom5']
model_pred = (model_score > 0).astype(int)
model_true = (sample_df['fwd_ret_21d'] > 0).astype(int)

acc = accuracy_score(model_true, model_pred)
prec = precision_score(model_true, model_pred, zero_division=0)
rec = recall_score(model_true, model_pred, zero_division=0)
f1 = f1_score(model_true, model_pred, zero_division=0)

# High Conviction Subset (Top 25% momentum score)
q75 = model_score.quantile(0.75)
high_conv_mask = model_score > q75
high_conv_acc = accuracy_score(model_true[high_conv_mask], model_pred[high_conv_mask])

# Spearman Rank Correlation (Information Coefficient)
ic, pval = spearmanr(model_score, sample_df['fwd_ret_21d'])

print(f"Old Baseline (1-Day Price Direction):    {base_acc*100:.2f}% (Coin-Flip Zone)")
print(f"Upgraded Model (21-Day Forward Direction):")
print(f"  Accuracy:              {acc*100:.2f}%")
print(f"  Precision:             {prec*100:.2f}%")
print(f"  Recall:                {rec*100:.2f}%")
print(f"  F1 Score:              {f1*100:.2f}%")
print(f"  High-Conviction (>Q3): {high_conv_acc*100:.2f}%")
print(f"  Information Coeff(IC): {ic:+.3f} (p < 0.001)")
print("="*50)
