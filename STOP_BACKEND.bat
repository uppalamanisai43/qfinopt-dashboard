@echo off
echo Stopping Q-FinOpt backend processes...
taskkill /F /IM python.exe /FI "WINDOWTITLE eq Q-FinOpt Backend Server*" >nul 2>&1
taskkill /F /FI "IMAGENAME eq python.exe" /FI "MEMUSAGE gt 50000" >nul 2>&1
powershell -Command "Stop-Process -Id (Get-NetTCPConnection -LocalPort 8000 -State Listen -ErrorAction SilentlyContinue).OwningProcess -Force -ErrorAction SilentlyContinue" >nul 2>&1
echo Backend server stopped successfully.
pause
