import io
from datetime import datetime
from typing import Any
from fpdf import FPDF
from app.config import IST
from app.models import PdfReportRequest, WithdrawalRequest, SipRequest
from app.fund_service import get_fund_stats
from app.simulation_service import run_withdrawal_simulation, run_sip_simulation

def clean_pdf_text(text: Any) -> str:
    """Sanitize text to safe Latin-1 / ASCII for standard FPDF fonts without crashing."""
    if text is None:
        return ""
    s = str(text)
    replacements = {
        "\u2013": "-",  # en-dash
        "\u2014": "--", # em-dash
        "\u2018": "'",  # left single quote
        "\u2019": "'",  # right single quote
        "\u201c": '"',  # left double quote
        "\u201d": '"',  # right double quote
        "\u2022": "*",  # bullet
        "\u20b9": "Rs. ", # Indian rupee symbol
        "₹": "Rs. ",
        "📡": "",
        "📁": "",
        "🟢": "",
        "🔴": "",
        "🟡": "",
        "⚡": "",
        "🎯": "",
        "📄": "",
        "🔔": "",
        "💼": "",
        "📊": "",
        "📈": "",
        "🛡️": "",
        "🏖️": "",
        "⚔️": "",
        "🔍": "",
        "🏦": "",
    }
    for old, new in replacements.items():
        s = s.replace(old, new)
    # Strip any remaining unencodable characters safely
    return s.encode("latin-1", errors="replace").decode("latin-1").strip()

def generate_pdf_report(req: PdfReportRequest) -> bytes:
    """Generate a clean, professional PDF investment report using fpdf2."""
    fund_name = clean_pdf_text(req.fund_name).strip() or "AXIS Bluechip Fund"
    user_name = clean_pdf_text(req.user_name).strip() or "Investor"
    invest_amt = float(req.investment) if req.investment and req.investment > 0 else 100000.0
    sip_amt = float(req.sip_amount) if req.sip_amount and req.sip_amount > 0 else 5000.0
    sip_yrs = int(req.sip_years) if req.sip_years and req.sip_years > 0 else 5
    invest_date = str(req.invest_date).strip() if req.invest_date else "2024-01-01"

    stats = get_fund_stats(fund_name)

    withdraw_req = WithdrawalRequest(
        fund_name=fund_name,
        invest_date=invest_date,
        investment=invest_amt,
        n_sim=1000
    )
    w_res = run_withdrawal_simulation(withdraw_req)

    sip_req = SipRequest(
        fund_name=fund_name,
        invest_date=invest_date,
        sip_amount=sip_amt,
        sip_years=sip_yrs,
        n_sim=1000
    )
    s_res = run_sip_simulation(sip_req)

    pdf = FPDF()
    pdf.add_page()

    # Title
    pdf.set_font("Helvetica", "B", 18)
    pdf.cell(190, 12, clean_pdf_text("Q-FinOpt Investment Report"), 0, 1, "C")
    pdf.set_font("Helvetica", "", 10)
    pdf.cell(190, 8, clean_pdf_text(f"For: {user_name} | Date: {datetime.now(IST).strftime('%d %b %Y %I:%M %p')} IST"), 0, 1, "C")
    pdf.ln(5)

    # Fund Details Section
    pdf.set_font("Helvetica", "B", 13)
    pdf.cell(190, 10, clean_pdf_text("Fund Details"), 0, 1)
    pdf.set_font("Helvetica", "", 10)
    nav_src_clean = clean_pdf_text(stats.nav_source).replace("LIVE", "LIVE").replace("Historical", "Historical")
    for label, value in [
        ("Fund Name", clean_pdf_text(fund_name[:60])),
        ("Category", clean_pdf_text(stats.category)),
        ("Risk Level", clean_pdf_text(stats.risk_level)),
        ("Latest NAV", clean_pdf_text(f"Rs {stats.display_nav:.2f} ({nav_src_clean})")),
        ("1 Year Return", clean_pdf_text(f"{stats.ret_1y:.2f}%")),
        ("Sharpe Ratio", clean_pdf_text(f"{stats.sharpe_val:.3f}")),
        ("Sortino Ratio", clean_pdf_text(f"{stats.sortino_val:.3f}" if stats.sortino_val is not None else "N/A")),
        ("Alpha", clean_pdf_text(f"{stats.alpha_val:.3f}")),
        ("Beta", clean_pdf_text(f"{stats.beta_val:.3f}")),
        ("Max Drawdown (MDD)", clean_pdf_text(f"{stats.mdd_val:.2f}%" if stats.mdd_val is not None else "N/A")),
        ("Expense Ratio", clean_pdf_text(f"{stats.expense:.2f}%")),
        ("AI Signal & Conviction", clean_pdf_text(f"{stats.signal} ({stats.conviction_score:.1f}%)" if stats.signal else "N/A")),
        ("Market Sentiment", clean_pdf_text(stats.sentiment)),
    ]:
        pdf.set_font("Helvetica", "B", 10)
        pdf.cell(70, 7, clean_pdf_text(label) + ":", 0, 0)
        pdf.set_font("Helvetica", "", 10)
        pdf.cell(120, 7, clean_pdf_text(str(value)), 0, 1)
    pdf.ln(4)

    # Withdrawal Recommendation
    pdf.set_font("Helvetica", "B", 13)
    pdf.cell(190, 10, clean_pdf_text("Withdrawal Recommendation"), 0, 1)
    for label, value in [
        ("Investment Amount", f"Rs {invest_amt:,.0f}"),
        ("Investment Date", clean_pdf_text(w_res.invest_date)),
        ("Withdraw On", clean_pdf_text(w_res.withdraw_date)),
        ("Holding Period", clean_pdf_text(f"{w_res.opt_day} days (~{w_res.opt_months} months)")),
        ("Expected Value", f"Rs {w_res.opt_val:,.0f}"),
        ("Expected Gain", f"Rs {w_res.gain:,.0f} ({w_res.ret_pct:.1f}%)"),
        ("Profit Probability", f"{w_res.profit_prob:.1f}%"),
    ]:
        pdf.set_font("Helvetica", "B", 10)
        pdf.cell(70, 7, clean_pdf_text(label) + ":", 0, 0)
        pdf.set_font("Helvetica", "", 10)
        pdf.cell(120, 7, clean_pdf_text(str(value)), 0, 1)
    pdf.ln(4)

    # SIP Projection
    pdf.set_font("Helvetica", "B", 13)
    pdf.cell(190, 10, clean_pdf_text("SIP Projection"), 0, 1)
    for label, value in [
        ("Monthly SIP", f"Rs {sip_amt:,.0f}"),
        ("Duration", f"{sip_yrs} years"),
        ("Total Invested", f"Rs {s_res.total_invested:,.0f}"),
        ("Expected Corpus", f"Rs {s_res.expected_sip:,.0f}"),
        ("Wealth Gain", f"Rs {s_res.gain_sip:,.0f}"),
        ("Est. XIRR", f"{s_res.expected_xirr:.1f}% per year"),
    ]:
        pdf.set_font("Helvetica", "B", 10)
        pdf.cell(70, 7, clean_pdf_text(label) + ":", 0, 0)
        pdf.set_font("Helvetica", "", 10)
        pdf.cell(120, 7, clean_pdf_text(str(value)), 0, 1)
    pdf.ln(4)

    # Disclaimer
    pdf.set_font("Helvetica", "I", 8)
    pdf.set_text_color(100, 100, 100)
    pdf.multi_cell(
        190, 5,
        clean_pdf_text(
            "DISCLAIMER: For educational purposes only. "
            "Mutual fund investments are subject to market risks. "
            "Past performance does not guarantee future returns."
        )
    )

    pdf.set_font("Helvetica", "B", 8)
    pdf.set_text_color(0, 0, 0)
    pdf.set_xy(10, 280)
    pdf.cell(
        190, 6,
        clean_pdf_text(f"Q-FinOpt Mobile Edition | Built by MANI SAI | {datetime.now(IST).strftime('%d %b %Y')}"),
        0, 0, "C"
    )

    return bytes(pdf.output())
