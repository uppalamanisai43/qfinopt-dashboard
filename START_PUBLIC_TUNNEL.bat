@echo off
title Q-FinOpt Cloudflare Public Tunnel
cd /d "%~dp0"
echo =========================================================
echo   Starting Free Cloudflare Public HTTPS Tunnel
echo   This allows your phone to connect from ANY Wi-Fi or 4G/5G
echo =========================================================
echo.
.\cloudflared.exe tunnel --url http://localhost:8000
pause
