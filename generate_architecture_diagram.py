import matplotlib.pyplot as plt
import matplotlib.patches as patches
import os

def create_architecture_diagram(output_path):
    fig = plt.figure(figsize=(16, 11), dpi=300)
    ax = fig.add_subplot(111)
    ax.set_xlim(0, 16)
    ax.set_ylim(0, 11)
    ax.axis('off')

    # Color Palette - Professional Tech & Quant Theme
    bg_color = "#F8FAFC"
    fig.patch.set_facecolor(bg_color)
    ax.set_facecolor(bg_color)

    c_client = "#EFF6FF"
    c_client_border = "#3B82F6"
    c_client_header = "#1E40AF"

    c_gateway = "#F5F3FF"
    c_gateway_border = "#8B5CF6"
    c_gateway_header = "#5B21B6"

    c_engine = "#ECFDF5"
    c_engine_border = "#10B981"
    c_engine_header = "#065F46"

    c_data = "#FFFBEB"
    c_data_border = "#F59E0B"
    c_data_header = "#92400E"

    card_bg = "#FFFFFF"
    text_dark = "#0F172A"
    text_muted = "#475569"
    badge_bg = "#E2E8F0"

    # Title Banner
    ax.text(8.0, 10.45, "QFinOpt — System Architecture", ha='center', va='center',
            fontsize=20, fontweight='bold', color="#0F172A", family='sans-serif')
    ax.text(8.0, 10.15, "AI-Driven Quantitative Mutual Fund Research, Stochastic Simulation & Quantum-Inspired Optimization",
            ha='center', va='center', fontsize=11, color="#64748B", family='sans-serif')

    # Function to draw rounded card
    def draw_box(x, y, w, h, bg, border, lw=1.5, radius=0.2):
        box = patches.FancyBboxPatch((x, y), w, h,
                                     boxstyle=f"round,pad=0.08,rounding_size={radius}",
                                     facecolor=bg, edgecolor=border, linewidth=lw, zorder=2)
        ax.add_patch(box)
        return box

    def draw_subcard(x, y, w, h, title, items, badge=""):
        draw_box(x, y, w, h, card_bg, "#CBD5E1", lw=1.2, radius=0.15)
        # Header banner inside card
        if badge:
            ax.text(x + w - 0.2, y + h - 0.28, badge, ha='right', va='center',
                    fontsize=7.5, fontweight='bold', color="#2563EB",
                    bbox=dict(boxstyle='round,pad=0.25', facecolor='#DBEAFE', edgecolor='none'))
        ax.text(x + 0.25, y + h - 0.28, title, ha='left', va='center',
                fontsize=10.5, fontweight='bold', color=text_dark)
        
        # Divider line
        ax.plot([x + 0.25, x + w - 0.25], [y + h - 0.45, y + h - 0.45], color="#E2E8F0", lw=1, zorder=3)
        
        # Bullet items
        cur_y = y + h - 0.68
        for item in items:
            ax.text(x + 0.25, cur_y, f"• {item}", ha='left', va='center',
                    fontsize=8.2, color=text_muted, zorder=3)
            cur_y -= 0.24

    # 1. CLIENT TIER (Top)
    draw_box(0.8, 7.8, 14.4, 2.05, c_client, c_client_border, lw=2.0)
    ax.text(1.1, 9.55, "1. CLIENT PRESENTATION LAYER", fontsize=11, fontweight='bold', color=c_client_header)

    # Client 1: Android App
    draw_subcard(1.1, 8.0, 6.8, 1.4, "Android Native Application", [
        "100% Kotlin + Jetpack Compose (Material Design 3)",
        "Clean MVVM Architecture with Reactive StateFlow & Coroutines",
        "High-Performance Custom Canvas Charts (No heavy 3rd-party libs)",
        "Retrofit 2 + OkHttp Client consuming JSON REST APIs"
    ], badge="Mobile App")

    # Client 2: Web Dashboard
    draw_subcard(8.3, 8.0, 6.6, 1.4, "Interactive Web Dashboard", [
        "Streamlit Full-Featured Quantitative Portal (app.py)",
        "Interactive Dynamic Plotly Analytics, Heatmaps & Corridors",
        "Live Market Sentiment Badges & Direct Comparison Matrix",
        "Zero-Config Rapid Research & Prototyping Frontend"
    ], badge="Web UI")

    # Arrow from Client to Gateway
    ax.annotate('', xy=(8.0, 7.35), xytext=(8.0, 7.8),
                arrowprops=dict(arrowstyle='->', color='#3B82F6', lw=2.5, mutation_scale=15))
    ax.text(8.0, 7.58, "RESTful HTTP / JSON (Port 8000) & Cloudflare Secure Tunnel",
            ha='center', va='center', fontsize=8.5, fontweight='bold', color='#1E40AF',
            bbox=dict(boxstyle='round,pad=0.2', facecolor='#FFFFFF', edgecolor='#BFDBFE'))

    # 2. API GATEWAY TIER
    draw_box(0.8, 6.1, 14.4, 1.25, c_gateway, c_gateway_border, lw=2.0)
    ax.text(1.1, 7.1, "2. API GATEWAY & SECURITY LAYER (FastAPI Async ASGI)", fontsize=11, fontweight='bold', color=c_gateway_header)

    draw_subcard(1.1, 6.25, 4.3, 0.8, "FastAPI Router", [
        "25+ REST Endpoints, Async Event Loop",
        "OpenAPI / Swagger Auto-Documentation"
    ])
    draw_subcard(5.85, 6.25, 4.3, 0.8, "Auth & Session Security", [
        "Token-Based Authentication & Guest Mode",
        "CORS Middleware & Request Rate Limits"
    ])
    draw_subcard(10.6, 6.25, 4.3, 0.8, "Ingress & Cloudflare Tunnel", [
        "Zero-Trust Remote WAN/Cellular Access",
        "Direct APK Distribution & Stream Service"
    ])

    # Arrow from Gateway to Microservices
    ax.annotate('', xy=(8.0, 5.65), xytext=(8.0, 6.1),
                arrowprops=dict(arrowstyle='->', color='#8B5CF6', lw=2.5, mutation_scale=15))

    # 3. COMPUTATIONAL ENGINES (Center)
    draw_box(0.8, 2.7, 14.4, 2.95, c_engine, c_engine_border, lw=2.0)
    ax.text(1.1, 5.35, "3. QUANTITATIVE, MACHINE LEARNING & QUANTUM MICROSERVICES LAYER", fontsize=11, fontweight='bold', color=c_engine_header)

    # Engine 1: ML Engine
    draw_subcard(1.1, 2.9, 4.4, 2.25, "AI & ML Forecasting Engine", [
        "HistGradientBoostingRegressor (scikit-learn)",
        "74.01% Directional Accuracy (21-Day Horizon)",
        "75.01% Conviction Accuracy (Top Quartile)",
        "12 Quantitative Features (RSI, Vol, Momentum)",
        "Out-of-Sample Monthly Alpha: +0.89% (p < 0.01)"
    ], badge="ML Core")

    # Engine 2: Simulation & Optimization
    draw_subcard(5.8, 2.9, 4.4, 2.25, "Simulation & Risk Engine", [
        "1,000+ Path Monte Carlo GBM Simulation",
        "Bull / Base / Bear Stochastic Corridors",
        "Safe Withdrawal Rate (SWR) Corpus Longevity",
        "Root-Finding Newton-Raphson XIRR Engine",
        "Capital Preservation Horizon Detection"
    ], badge="Stochastic")

    # Engine 3: Quantum QAOA & Portfolio
    draw_subcard(10.5, 2.9, 4.4, 2.25, "Quantum QAOA & Analytics", [
        "Mean-Variance QUBO Combinatorial Optimizer",
        "Ising Hamiltonian State Vector QAOA Simulation",
        "Cardinality Penalty & Covariance Risk Matrix",
        "Markowitz QP & Classical Solver Benchmarking",
        "Sharpe, Sortino, Treynor, CAPM Alpha & Beta"
    ], badge="Quantum")

    # Arrow from Engines to Data Layer
    ax.annotate('', xy=(8.0, 2.25), xytext=(8.0, 2.7),
                arrowprops=dict(arrowstyle='->', color='#10B981', lw=2.5, mutation_scale=15))

    # 4. DATA & PERSISTENCE TIER (Bottom)
    draw_box(0.8, 0.45, 14.4, 1.8, c_data, c_data_border, lw=2.0)
    ax.text(1.1, 1.95, "4. DATA MANAGEMENT & STORAGE LAYER", fontsize=11, fontweight='bold', color=c_data_header)

    draw_subcard(1.1, 0.6, 3.2, 1.15, "AMFI NAV Dataset", [
        "qfinopt_cleaned.csv (91 MB)",
        "419,188 daily trading records",
        "296 Curated Direct Schemes"
    ])

    draw_subcard(4.7, 0.6, 3.2, 1.15, "Model Artifacts", [
        "mf_gb_model.joblib (554 KB)",
        "mf_gb_meta.json feature spec",
        "StandardScaler normalizers"
    ])

    draw_subcard(8.3, 0.6, 3.2, 1.15, "Live Feeds & Market", [
        "Official AMFI NAV Stream (14k+)",
        "Yahoo Finance (NSE/BSE / Gold)",
        "Real-Time Sentiment Pipeline"
    ])

    draw_subcard(11.9, 0.6, 3.0, 1.15, "User Store & Reports", [
        "users.json & portfolios.json",
        "Dynamic fpdf2 Report Generator",
        "APK Binary Storage"
    ])

    plt.tight_layout()
    plt.savefig(output_path, dpi=300, facecolor=bg_color, edgecolor='none', bbox_inches='tight')
    plt.close()
    print(f"Architecture diagram successfully generated at: {output_path}")

if __name__ == "__main__":
    out_dir = r"c:\Users\hp\Downloads\Qfinopt"
    target_png = os.path.join(out_dir, "architecture_diagram.png")
    create_architecture_diagram(target_png)
