@echo off
REM scripts\load-env.bat - safe loader that delegates parsing to PowerShell helper
REM Usage: set DEBUG_ENV=1 and/or set PRINT_VARS_ONLY=1 before calling to alter output

set "PROJECT_ROOT=%~dp0..\"
pushd "%PROJECT_ROOT%" >nul 2>&1 || (echo Erro: nao foi possivel acessar %PROJECT_ROOT% & exit /b 1)

if not exist ".env" (
    popd >nul 2>&1
    exit /b 0
)

echo Gerando .env.generated.bat e .env.generated.vars.txt a partir de .env (via PowerShell)...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0load-env.ps1"

if exist .env.generated.bat (
    echo DEBUG: about to call .env.generated.bat
    call .env.generated.bat
    echo DEBUG: returned from .env.generated.bat
    REM comportamento DEBUG
    if /i "%DEBUG_ENV%"=="1" (
        echo DEBUG_ENV=1 : preservando .env.generated.bat para depuracao
        if /i "%PRINT_VARS_ONLY%"=="1" (
            echo --- Variaveis definidas pelo parser (conciso):
            powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0load-env.ps1" -ShowVars
        ) else (
            type .env.generated.bat
            echo --- Variaveis definidas pelo parser:
            powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0load-env.ps1" -ShowVars -PrintFull
        )
    ) else ( 
        if /i "%DEBUG_ENV%"=="true" (
            echo DEBUG_ENV=true : preservando .env.generated.bat para depuracao
            if /i "%PRINT_VARS_ONLY%"=="1" (
                echo --- Variaveis definidas pelo parser (conciso):
                powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0load-env.ps1" -ShowVars
            ) else (
                type .env.generated.bat
                echo --- Variaveis definidas pelo parser:
                powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0load-env.ps1" -ShowVars -PrintFull
            )
        ) else (
            del /f /q .env.generated.bat >nul 2>&1
            del /f /q .env.generated.vars.txt >nul 2>&1
        )
)
    echo DEBUG: end of if exist .env.generated.bat block

popd >nul 2>&1
exit /b 0
