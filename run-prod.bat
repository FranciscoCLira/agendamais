@echo off
echo Carregando variaveis do .env (se existir) via PowerShell runner...
REM Forward any CLI args to the PowerShell runner (e.g. -PrintVarsOnly -Debug)
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\run-with-env.ps1 -Profile prod %*

REM Delegate execution to the PowerShell runner which will set env vars and start Maven
REM Return the runner's exit code
if %ERRORLEVEL% NEQ 0 (
	exit /b %ERRORLEVEL%
)
exit /b 0
