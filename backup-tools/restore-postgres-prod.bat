@echo off
REM ======================================================
REM Restore PostgreSQL - AgendaMais PROD
REM ======================================================
setlocal

REM Configuração
set PGUSER=agenda
set PGPASSWORD=agenda
set PGHOST=localhost
set PGPORT=5432
set PGDATABASE=agendadb_prod

REM Path do psql (ajustar se necessário)
set PGBIN=C:\Program Files\PostgreSQL\15\bin

REM Solicitar arquivo de backup
echo ======================================================
echo Restore PostgreSQL - AgendaMais PROD
echo ======================================================
echo.
echo ⚠️  AVISO: Este processo ira SUBSTITUIR todos os dados!
echo.
echo Arquivos de backup disponiveis:
echo.
dir /b db-backups\postgres\*.sql
echo.
set /p BACKUP_FILE="Digite o nome do arquivo (ex: backup-prod-2025-11-24.sql): "

set BACKUP_PATH=db-backups\postgres\%BACKUP_FILE%

if not exist "%BACKUP_PATH%" (
    echo.
    echo ✗ ERRO: Arquivo nao encontrado!
    echo %BACKUP_PATH%
    pause
    exit /b 1
)

echo.
echo ======================================================
echo Restore sera executado com:
echo ======================================================
echo Database: %PGDATABASE%
echo Host: %PGHOST%:%PGPORT%
echo User: %PGUSER%
echo Arquivo: %BACKUP_PATH%
echo.
echo ⚠️  TODOS OS DADOS ATUAIS SERAO PERDIDOS!
echo.
set /p CONFIRM="Confirma restore? (S/N): "

if /i not "%CONFIRM%"=="S" (
    echo.
    echo Restore cancelado.
    pause
    exit /b 0
)

echo.
echo Executando restore...
echo.

REM Executar psql
"%PGBIN%\psql.exe" -U %PGUSER% -h %PGHOST% -p %PGPORT% -d %PGDATABASE% -f "%BACKUP_PATH%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================================
    echo ✓ RESTORE CONCLUIDO COM SUCESSO!
    echo ======================================================
    echo.
) else (
    echo.
    echo ======================================================
    echo ✗ ERRO AO FAZER RESTORE!
    echo ======================================================
    echo Verifique os logs acima para detalhes.
    echo.
)

pause
