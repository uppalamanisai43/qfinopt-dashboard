@echo off
title Q-FinOpt Backend Server
cd /d "%~dp0backend"
echo ===================================================
echo   Starting Q-FinOpt Live Backend (FastAPI)
echo   Local Wi-Fi Address: http://10.0.64.79:8000
echo   Direct APK Download: http://10.0.64.79:8000/download/apk
echo ===================================================
echo.
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
pause
