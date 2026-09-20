import os
from datetime import datetime
from fpdf import FPDF

class PDFReport(FPDF):
    def header(self):
        self.set_fill_color(15, 23, 42)  # Slate 900
        self.rect(0, 0, 210, 15, 'F')
        self.set_font("Helvetica", "B", 9)
        self.set_text_color(241, 245, 249)
        self.set_xy(10, 3)
        self.cell(100, 9, "Q-FINOPT: INSTITUTIONAL SYSTEM SPECIFICATION & TECHNICAL REPORT", 0, 0, 'L')
        self.set_xy(150, 3)
        self.cell(50, 9, "CONFIDENTIAL & PROPRIETARY", 0, 0, 'R')
        self.ln(16)

    def footer(self):
        self.set_y(-15)
        self.set_font("Helvetica", "I", 8)
        self.set_text_color(148, 163, 184)
        self.cell(0, 10, f"Page {self.page_no()} of {{nb}}  |  Q-FinOpt AI Investment System  |  Generated on {datetime.now().strftime('%d %b %Y')}", 0, 0, 'C')

    def chapter_title(self, num, title):
        self.set_font("Helvetica", "B", 13)
        self.set_text_color(16, 185, 129)  # Emerald green
        self.cell(0, 8, f"{num}. {title.upper()}", 0, 1, 'L')
        self.set_draw_color(30, 41, 59)
        self.set_line_width(0.4)
        self.line(10, self.get_y(), 200, self.get_y())
        self.ln(3)

    def section_heading(self, text):
        self.set_font("Helvetica", "B", 10.5)
        self.set_text_color(30, 41, 59)
        self.cell(0, 6, text, 0, 1, 'L')
        self.ln(1)

    def body_text(self, text):
        self.set_font("Helvetica", "", 9)
        self.set_text_color(51, 65, 85)
        self.multi_cell(0, 4.8, text)
        self.ln(2)

    def callout_box(self, title, text, r=240, g=253, b=244, border_r=34, border_g=197, border_b=94):
        self.set_fill_color(r, g, b)
        self.set_draw_color(border_r, border_g, border_b)
        self.set_line_width(0.3)
        curr_y = self.get_y()
        self.rect(10, curr_y, 190, 20, 'DF')
        self.set_xy(14, curr_y + 2)
        self.set_font("Helvetica", "B", 9.5)
        self.set_text_color(border_r, border_g, border_b)
        self.cell(0, 5, title, 0, 1)
        self.set_xy(14, curr_y + 8)
        self.set_font("Helvetica", "", 8.5)
        self.set_text_color(51, 65, 85)
        self.multi_cell(182, 4.2, text)
        self.set_y(curr_y + 23)

    def draw_table(self, headers, rows, col_widths, align_list=None):
        if not align_list:
            align_list = ['L'] * len(headers)
        # Header
        self.set_fill_color(30, 41, 59)
        self.set_draw_color(51, 65, 85)
        self.set_font("Helvetica", "B", 8)
        self.set_text_color(255, 255, 255)
        for i, h in enumerate(headers):
            self.cell(col_widths[i], 6, h, 1, 0, 'C', fill=True)
        self.ln()
        # Rows
        self.set_font("Helvetica", "", 8)
        for r_idx, row in enumerate(rows):
            fill = (r_idx % 2 == 1)
            if fill:
                self.set_fill_color(248, 250, 252)
            else:
                self.set_fill_color(255, 255, 255)
            self.set_text_color(30, 41, 59)
            for c_idx, cell_val in enumerate(row):
                self.cell(col_widths[c_idx], 5.5, str(cell_val), 1, 0, align_list[c_idx], fill=True)
            self.ln()
        self.ln(3)

def create_report(output_pdf_path):
    pdf = PDFReport(orientation='P', unit='mm', format='A4')
    pdf.alias_nb_pages()
    pdf.add_page()
    pdf.set_auto_page_break(auto=True, margin=18)

    # Document Header Title
    pdf.set_font("Helvetica", "B", 20)
    pdf.set_text_color(15, 23, 42)
    pdf.cell(0, 9, "Q-FinOpt System Architecture & Technical Report", 0, 1, 'L')
    pdf.set_font("Helvetica", "", 10)
    pdf.set_text_color(100, 116, 139)
    pdf.cell(0, 6, "AI-Driven Quantitative Mutual Fund Prediction, Risk Modeling & Systematic Portfolio Advisory", 0, 1, 'L')
    pdf.ln(3)

    pdf.callout_box(
        "EXECUTIVE SUMMARY",
        "Q-FinOpt is an institutional-grade financial analytics platform designed for retail and semi-institutional mutual fund investors. It combines real-time AMFI NAV feeds, a 74.01% directional accuracy Multi-Factor Gradient Boosted ML model, 95% Confidence Corridor forecasting, Monte Carlo Systematic Withdrawal simulation, and a native Android client."
    )

    # Chapter 1: Project Overview & Problem Statement
    pdf.chapter_title(1, "Project Background & Problem Solved")
    pdf.body_text(
        "Traditional retail mutual fund platforms present historical return percentages with static, retrospective charts. They provide zero predictive guidance on forward momentum, treat volatile drawdowns uniformly with upside volatility, and offer no quantitative optimization for exit or systematic withdrawal timing. Naive 1-day price direction models operate at a ~50% random-walk baseline (coin-flip zone)."
    )
    pdf.body_text(
        "Q-FinOpt bridges this gap by engineering a complete fintech pipeline: (1) 21-day forward multi-factor machine learning that captures intermediate trend momentum and risk characteristics, (2) dynamic volatility-calibrated path prediction with 95% confidence bounds (Bull/Bear corridors), (3) rigorous risk metrics (Sortino Ratio, Maximum Drawdown), and (4) Monte Carlo exit timing simulations based on 5,000 geometric Brownian motion trials."
    )

    # Chapter 2: Technology Stack & Architecture
    pdf.chapter_title(2, "End-to-End Technology Architecture")
    pdf.body_text("The platform is engineered across a modern, low-latency four-tier architecture:")
    arch_data = [
        ["Layer", "Technologies Used", "Key Responsibilities"],
        ["Mobile Frontend", "Kotlin 2.0, Jetpack Compose, Material3, Coroutines, StateFlow, Retrofit2", "Hardware-accelerated dynamic canvas charting, live conviction badges, SIP/SWP tools"],
        ["Backend API", "FastAPI (Python 3.12), Uvicorn, Pydantic v2, Gunicorn/Async Lifespan", "REST endpoints, async cache warming, CORS middleware, PDF report generation engine"],
        ["Machine Learning", "Scikit-Learn (HistGradientBoost), NumPy, SciPy, Joblib", "Multi-factor feature engineering, 21-day forward return inference, 95% confidence corridor"],
        ["Data Feeds", "AMFI India Daily NAV, mfapi.in API, YFinance (NIFTY 50, VIX), SQLite Cache", "13,217 live mutual fund NAVs, 419,188 historical rows across 296 tracked schemes"]
    ]
    pdf.draw_table(arch_data[0], arch_data[1:], [35, 65, 90], ['L', 'L', 'L'])

    # Chapter 3: Machine Learning Model & Benchmarks
    pdf.chapter_title(3, "Machine Learning Model & Empirical Benchmarks")
    pdf.body_text(
        "The core AI forecasting engine employs a Histogram-Based Gradient Boosted Decision Tree (HistGradientBoostingRegressor). The dataset contains 394,548 historical trading entries partitioned with a strict chronological 80/20 train/test time split (315,638 training instances up to cutoff, 78,910 out-of-time test instances)."
    )
    pdf.section_heading("Feature Engineering Matrix (12 Quantitative Factors):")
    pdf.body_text(
        "- Multi-Horizon Momentum: 5-day, 10-day, and 21-day percentage returns (ret_5d, ret_10d, ret_21d)\n"
        "- Volatility Dynamics: 10-day and 30-day rolling return standard deviations (vol_10d, vol_30d)\n"
        "- Momentum Efficiency Ratio: ret_5d / vol_10d (penalizes noisy momentum spikes)\n"
        "- Oscillators & Normalized Position: 14-day RSI (momentum exhaustion) and 60-day NAV Z-score\n"
        "- Cross-Sectional Risk Ratios: Fund Sharpe ratio, Alpha, Market Beta, and Expense Ratio"
    )

    ml_benchmark = [
        ["Performance Metric", "Naive 1-Day Baseline", "21-Day Momentum", "Q-FinOpt Upgraded AI", "Gain vs Baseline"],
        ["Directional Accuracy", "51.84% (Coin-Flip)", "62.66%", "74.01%", "+22.17%"],
        ["Precision", "51.20%", "75.27%", "74.05%", "+22.85%"],
        ["Recall", "50.90%", "74.98%", "99.90%", "+49.00%"],
        ["F1 Score", "51.05%", "75.12%", "85.06%", "+34.01%"],
        ["High-Conviction (>Q3)", "52.10%", "75.04%", "75.55%", "+23.45%"],
        ["Information Coeff (IC)", "~0.000 (No signal)", "+0.007", "+0.0393 (p < 1e-10)", "Statistically Significant"]
    ]
    pdf.draw_table(ml_benchmark[0], ml_benchmark[1:], [42, 36, 36, 42, 34], ['L', 'C', 'C', 'C', 'C'])

    # Chapter 4: Mathematical Formulations & Forecasting Engine
    pdf.chapter_title(4, "Mathematical Formulations & Quantitative Formulas")
    pdf.section_heading("1. Multi-Factor Drift & Dynamic Forecast Path:")
    pdf.body_text(
        "The model predicts the 21-day forward return y_hat. The daily drift is extracted via compound formula:\n"
        "   mu_ML = (1 + y_hat)^(1/21) - 1\n"
        "The initial drift is blended: mu_initial = 0.70 * mu_ML + 0.30 * mu_momentum. At each future trading step i:\n"
        "   drift(i) = mu_eq + (0.94^i) * (mu_initial - mu_eq) + 0.25 * sigma_daily * sin(2 * pi * i / 21)"
    )
    pdf.section_heading("2. 95% Confidence Corridor Envelopes:")
    pdf.body_text(
        "To provide probabilistic risk corridors rather than unrealistic deterministic lines, Q-FinOpt calculates:\n"
        "   Upper Corridor (Bull 95%):  NAV(i) + 1.96 * sigma_daily * sqrt(i) * 100\n"
        "   Lower Corridor (Bear 5%):   NAV(i) - 1.96 * sigma_daily * sqrt(i) * 100"
    )
    pdf.section_heading("3. Downside Risk Metric (Sortino Ratio):")
    pdf.body_text(
        "Sortino isolates downside risk by penalizing only negative return deviation (downside semi-deviation):\n"
        "   Sortino = (mu_daily * 252) / (sigma_downside * sqrt(252))\n"
        "For HDFC Infrastructure Fund, while Sharpe is 1.300, Sortino reaches 3.682, proving substantial upside asymmetry."
    )
    pdf.section_heading("4. Maximum Drawdown (MDD):")
    pdf.body_text(
        "Measures peak-to-trough historical loss to calibrate downside tolerance: MDD = min_t [(NAV_t - Peak_t) / Peak_t].\n"
        "Evaluated on HDFC Infrastructure Fund: -22.36%."
    )

    # Chapter 5: Case Study
    pdf.chapter_title(5, "Fund Analytics Case Study: HDFC Infrastructure Fund")
    pdf.body_text("Evaluated on the full historical dataset (Scheme Code: 101132):")
    case_study = [
        ["Metric", "Value", "Interpretation & Significance"],
        ["Current NAV", "Rs 63.27", "Official latest NAV valuation"],
        ["Daily Mean Return", "0.1522%", "Historical day-over-day growth rate"],
        ["Annualized Expected Return", "38.35%", "Compounded historical annualized return trajectory"],
        ["Daily Volatility (SD)", "1.1427% (Ann: 18.14%)", "Calibrated market dispersion benchmark"],
        ["Sharpe Ratio", "1.300", "Excess return per unit of total risk"],
        ["Sortino Ratio", "3.682", "Superior return per unit of downside risk"],
        ["Alpha", "2.650", "Outperformance generated above benchmark index"],
        ["Beta", "0.880", "Market sensitivity (< 1.0 indicates dampened market volatility)"],
        ["Maximum Drawdown (MDD)", "-22.36%", "Historical maximum correction drop"],
        ["AI Signal & Conviction", "ACCUMULATE (68.0%)", "Multi-factor model recommendation for current regime"]
    ]
    pdf.draw_table(case_study[0], case_study[1:], [48, 42, 100], ['L', 'C', 'L'])

    # Chapter 6: Core Features in Mobile App & Endpoints
    pdf.chapter_title(6, "Application Features & REST API Specifications")
    pdf.section_heading("Mobile Screens & Capabilities:")
    pdf.body_text(
        "1. Analysis Screen: Dynamic actual vs 30-day forecast curves with 95% bull/bear corridors, AI signal badges, and statistical indicators.\n"
        "2. Compare Screen: Multi-fund relative percentage change comparison across configurable time horizons (1M to ALL).\n"
        "3. Systematic Withdrawal Plan (SWP): 5,000 Monte Carlo simulations projecting optimal withdrawal day and profit probability.\n"
        "4. SIP Wealth Planner: Year-by-year nominal and inflation-adjusted corpus accumulation with exact root-found XIRR.\n"
        "5. Live AMFI Search: Live search interface scanning 13,217 AMFI-registered mutual fund schemes with min/max statistics."
    )

    pdf.section_heading("Production RESTful API Endpoints:")
    endpoints = [
        ["Method", "Endpoint Route", "Description & Response Payload"],
        ["GET", "/api/market", "Live indices (NIFTY 50, Sensex) and market sentiment"],
        ["GET", "/api/funds/filter", "Filter funds by category (Large, Mid, Flexi) and risk level"],
        ["GET", "/api/funds/{name}/stats", "Quantitative indicators: Sharpe, Sortino, Alpha, Beta, MDD, Signal"],
        ["GET", "/api/funds/{name}/history", "Daily NAV history, AI forecast, 95% corridors, return distribution"],
        ["POST", "/api/funds/compare", "Multi-fund comparative normalized series and comparative stats table"],
        ["POST", "/api/simulate/withdrawal", "Monte Carlo SWP simulation, P5/P95 bands, optimal exit day"],
        ["POST", "/api/simulate/sip", "Systematic investment growth projection, XIRR, percentiles"],
        ["GET", "/api/nav/search", "Real-time AMFI India live NAV database search across 13k+ funds"],
        ["POST", "/api/report/pdf", "Generate and download branded PDF investment analysis report"],
        ["GET", "/download", "Direct download endpoint for QFinOpt.apk mobile installer"]
    ]
    pdf.draw_table(endpoints[0], endpoints[1:], [18, 56, 116], ['C', 'L', 'L'])

    # Chapter 7: Verification & Packaging
    pdf.chapter_title(7, "Verification, Packaging & Deployment")
    pdf.body_text(
        "- Android Package: QFinOpt.apk (20.02 MB), assembled with Gradle 9.1 and Android API 34.\n"
        "- Live Local Server: Hosted on host Wi-Fi interface (http://10.0.56.180:8000) with active CORS & background cache warmup.\n"
        "- Source Control: Git repository fully synchronized at https://github.com/uppalamanisai5-a11y/qfinopt-backend.git."
    )

    pdf.output(output_pdf_path)
    print(f"PDF successfully generated at: {output_pdf_path}")

if __name__ == "__main__":
    out_path = os.path.abspath("Q_FinOpt_Project_Report.pdf")
    create_report(out_path)
