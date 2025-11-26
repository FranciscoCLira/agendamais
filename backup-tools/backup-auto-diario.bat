@echo off
REM ======================================================
REM Backup Automático Diário PostgreSQL - AgendaMais
REM ======================================================
REM Agendar este script no Agendador de Tarefas do Windows
REM para executar todos os dias às 23:00 (ou horário desejado)
REM ======================================================
setlocal

REM Configuração
set PGUSER=agenda
set PGPASSWORD=agenda
set PGHOST=localhost
set PGPORT=5432

REM Diretório de backup com data
for /f "tokens=1-4 delims=/ " %%a in ('date /t') do set HOJE=%%d-%%b-%%c
for /f "tokens=1-2 delims=: " %%a in ('time /t') do set HORA=%%a-%%b
set HORA=%HORA::=-%

set BACKUP_DIR=%~dp0db-backups\postgres\auto-diario\%HOJE%
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo ======================================================
echo Backup Automático Diário - %HOJE% %HORA%
echo ======================================================
echo.

REM Backup DEV
echo [1/2] Fazendo backup de agendadb_dev...
docker exec agendamais-db pg_dump -U %PGUSER% agendadb_dev > "%BACKUP_DIR%\dev-%HOJE%-%HORA%.sql"
if %ERRORLEVEL% EQU 0 (
    echo ✓ Backup DEV concluído
) else (
    echo ✗ ERRO no backup DEV
)
echo.

REM Backup PROD
echo [2/2] Fazendo backup de agendadb_prod...
docker exec agendamais-db pg_dump -U %PGUSER% agendadb_prod > "%BACKUP_DIR%\prod-%HOJE%-%HORA%.sql"
if %ERRORLEVEL% EQU 0 (
    echo ✓ Backup PROD concluído
) else (
    echo ✗ ERRO no backup PROD
)
echo.

REM Limpar backups antigos (manter últimos 30 dias)
echo Limpando backups com mais de 30 dias...
forfiles /P "%~dp0db-backups\postgres\auto-diario" /D -30 /C "cmd /c if @isdir==TRUE rd /s /q @path" 2>nul
echo.

echo ======================================================
echo ✓ Backup automático concluído!
echo Local: %BACKUP_DIR%
echo ======================================================
echo.

endlocal
