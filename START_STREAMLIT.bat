@echo off
title Q-FinOpt Streamlit Web Dashboard
cd /d "%~dp0"
echo ===================================================
echo   Starting Q-FinOpt Streamlit Web Application
echo   Local Web URL:   http://localhost:8501
echo ===================================================
echo.
python -m streamlit run app.py --server.port 8501
pause
