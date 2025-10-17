@echo off
REM Test script for load-env: uses .env.sample to simulate parser output
echo Preparando simulacao com .env.sample...
copy /Y ..\.env.sample ..\.env >nul
set DEBUG_ENV=1
set PRINT_VARS_ONLY=1
echo Executando scripts\load-env.bat (simulacao)...
call "%~dp0load-env.bat"
echo Simulacao completa. (arquivo .env.generated.bat preservado no diretorio do projeto)
exit /b 0
