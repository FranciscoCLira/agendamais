@echo off
REM Alias para imprimir variaveis extraidas do .env via PowerShell runner
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\run-with-env.ps1 -PrintVarsOnly -Debug
exit /b %ERRORLEVEL%
