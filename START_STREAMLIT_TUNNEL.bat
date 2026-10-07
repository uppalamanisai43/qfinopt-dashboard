@echo off
title Q-FinOpt Streamlit Public Tunnel
cd /d "%~dp0"
echo =========================================================
echo   Starting Free Cloudflare Public HTTPS Tunnel for Streamlit
echo   Share this link to access the website from ANY device / phone!
echo =========================================================
echo.
.\cloudflared.exe tunnel --url http://localhost:8501
pause
