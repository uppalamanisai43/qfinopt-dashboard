import pandas as pd
import numpy as np
from sklearn.ensemble import HistGradientBoostingRegressor
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score
from scipy.stats import spearmanr
import joblib
import json
import os

print("Loading dataset for model training...")
df = pd.read_csv('backend/data/qfinopt_cleaned.csv')
df['Date'] = pd.to_datetime(df['Date'])
df = df.sort_values(['Scheme_Code', 'Date']).reset_index(drop=True)

# Feature engineering
print("Calculating quantitative & technical features...")
df['ret_1d'] = df['Daily_Return_%'] / 100.0
df['ret_5d'] = df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x / x.shift(5) - 1.0)
df['ret_10d'] = df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x / x.shift(10) - 1.0)
df['ret_21d'] = df.groupby('Scheme_Code')['NAV_Value'].transform(lambda x: x / x.shift(21) - 1.0)
df['vol_10d'] = df.groupby('Scheme_Code')['ret_1d'].transform(lambda x: x.rolling(10).std())
df['vol_30d'] = df.groupby('Scheme_Code')['ret_1d'].transform(lambda x: x.rolling(30).std())

# Rolling momentum ratios
df['mom_ratio'] = df['ret_5d'] / (df['vol_10d'] + 1e-6)

# RSI-14
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

split_idx = int(len(clean_data) * 0.8)
train_df = clean_data.iloc[:split_idx]
test_df = clean_data.iloc[split_idx:]

X_train, y_train = train_df[feature_cols], train_df['fwd_ret_21d']
X_test, y_test = test_df[feature_cols], test_df['fwd_ret_21d']

print(f"Training on {len(train_df)} rows, testing on {len(test_df)} rows...")
model = HistGradientBoostingRegressor(max_iter=150, max_depth=6, learning_rate=0.05, random_state=42)
model.fit(X_train, y_train)

# Evaluation
preds = model.predict(X_test)
y_true_dir = (y_test > 0).astype(int)
y_pred_dir = (preds > 0).astype(int)

acc = float(accuracy_score(y_true_dir, y_pred_dir))
prec = float(precision_score(y_true_dir, y_pred_dir, zero_division=0))
rec = float(recall_score(y_true_dir, y_pred_dir, zero_division=0))
f1 = float(f1_score(y_true_dir, y_pred_dir, zero_division=0))

q75 = float(np.quantile(preds, 0.75))
high_conv_mask = preds >= q75
high_conv_acc = float(accuracy_score(y_true_dir[high_conv_mask], y_pred_dir[high_conv_mask]))
ic, _ = spearmanr(preds, y_test)

print(f"Accuracy: {acc*100:.2f}% | F1: {f1*100:.2f}% | High-Conviction Win Rate: {high_conv_acc*100:.2f}%")

# Save model artifact
os.makedirs('backend/data', exist_ok=True)
model_path = 'backend/data/mf_gb_model.joblib'
joblib.dump(model, model_path)
print(f"Saved model to {model_path}")

meta = {
    "feature_cols": feature_cols,
    "accuracy": acc,
    "precision": prec,
    "recall": rec,
    "f1": f1,
    "high_conviction_acc": high_conv_acc,
    "high_conviction_threshold": q75,
    "ic": float(ic)
}
meta_path = 'backend/data/mf_gb_meta.json'
with open(meta_path, 'w') as f:
    json.dump(meta, f, indent=2)
print(f"Saved metadata to {meta_path}")
