import pandas as pd
import numpy as np
from sklearn.ensemble import HistGradientBoostingRegressor
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score
from scipy.stats import spearmanr
import os

print("Loading dataset...")
df = pd.read_csv('backend/data/qfinopt_cleaned.csv')
df['Date'] = pd.to_datetime(df['Date'])
df = df.sort_values(['Scheme_Code', 'Date']).reset_index(drop=True)

# Feature engineering
print("Engineering quantitative & technical features...")
df['ret_1d'] = df['Daily_Return_%'] / 100.0
df['ret_5d'] = df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x / x.shift(5) - 1.0)
df['ret_10d'] = df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x / x.shift(10) - 1.0)
df['ret_21d'] = df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x / x.shift(21) - 1.0)
df['vol_10d'] = df.groupby('Scheme_Code')['ret_1d'].transform(lambda x: x.rolling(10).std())
df['vol_30d'] = df.groupby('Scheme_Code')['ret_1d'].transform(lambda x: x.rolling(30).std())

# Rolling momentum ratios
df['mom_ratio'] = df['ret_5d'] / (df['vol_10d'] + 1e-6)

# RSI-14 approximation
def calc_rsi(series, period=14):
    delta = series.diff()
    gain = (delta.where(delta > 0, 0)).rolling(window=period).mean()
    loss = (-delta.where(delta < 0, 0)).rolling(window=period).mean()
    rs = gain / (loss + 1e-6)
    return 100 - (100 / (1 + rs))

df['rsi_14'] = df.groupby('Scheme_Code')['NAV_Value'].transform(calc_rsi)

# Z-score of NAV relative to 60-day mean
df['nav_mean60'] = df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x.rolling(60).mean())
df['nav_std60'] = df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x.rolling(60).std())
df['nav_z'] = (df['NAV_Value'] - df['nav_mean60']) / (df['nav_std60'] + 1e-6)

# Target: 21-day forward return
df['fwd_ret_21d'] = df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x.shift(-21) / x - 1.0)

feature_cols = [
    'ret_5d', 'ret_10d', 'ret_21d', 'vol_10d', 'vol_30d',
    'mom_ratio', 'rsi_14', 'nav_z', 'Sharpe', 'Alpha', 'Beta', 'Expense_Ratio'
]

clean_data = df.dropna(subset=feature_cols + ['fwd_ret_21d']).copy()
clean_data = clean_data.sort_values('Date').reset_index(drop=True)

# Time-based train / test split (80% train, 20% test)
split_idx = int(len(clean_data) * 0.8)
train_df = clean_data.iloc[:split_idx]
test_df = clean_data.iloc[split_idx:]

X_train, y_train = train_df[feature_cols], train_df['fwd_ret_21d']
X_test, y_test = test_df[feature_cols], test_df['fwd_ret_21d']

print(f"Training dataset size: {len(train_df)} rows")
print(f"Test dataset size:     {len(test_df)} rows")

print("Training HistGradientBoosting model...")
model = HistGradientBoostingRegressor(max_iter=150, max_depth=6, learning_rate=0.05, random_state=42)
model.fit(X_train, y_train)

# Predictions
preds = model.predict(X_test)
y_true_dir = (y_test > 0).astype(int)
y_pred_dir = (preds > 0).astype(int)

acc = accuracy_score(y_true_dir, y_pred_dir)
prec = precision_score(y_true_dir, y_pred_dir, zero_division=0)
rec = recall_score(y_true_dir, y_pred_dir, zero_division=0)
f1 = f1_score(y_true_dir, y_pred_dir, zero_division=0)

# High Conviction signals (top 20% positive forecast return)
top_threshold = np.quantile(preds, 0.80)
high_conv_mask = preds >= top_threshold
high_conv_acc = accuracy_score(y_true_dir[high_conv_mask], y_pred_dir[high_conv_mask])

ic, _ = spearmanr(preds, y_test)

print("\n" + "="*50)
print("UPGRADED ML MODEL EVALUATION RESULTS (Out-of-Time Test Set)")
print("="*50)
print(f"Directional Accuracy:     {acc*100:.2f}%")
print(f"Precision:                {prec*100:.2f}%")
print(f"Recall:                   {rec*100:.2f}%")
print(f"F1 Score:                 {f1*100:.2f}%")
print(f"High-Conviction Win Rate: {high_conv_acc*100:.2f}% (Top 20% strongest calls)")
print(f"Information Coeff (IC):   {ic:+.4f}")
print("="*50)
