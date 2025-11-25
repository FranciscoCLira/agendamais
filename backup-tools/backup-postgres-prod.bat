@echo off
REM ======================================================
REM Backup PostgreSQL - AgendaMais PROD
REM ======================================================
setlocal

REM Configuração
set PGUSER=agenda
set PGPASSWORD=agenda
set PGHOST=localhost
set PGPORT=5432
set PGDATABASE=agendadb_prod

REM Path do pg_dump (ajustar se necessário)
set PGBIN=C:\Program Files\PostgreSQL\15\bin

REM Diretório de backup
set BACKUP_DIR=%~dp0db-backups\postgres
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM Nome do arquivo com timestamp
for /f "tokens=2-4 delims=/ " %%a in ('date /t') %%e in ('time /t') do (
    set TIMESTAMP=%%c-%%a-%%b-%%e
)
set TIMESTAMP=%TIMESTAMP::=-%
set TIMESTAMP=%TIMESTAMP: =_%
set BACKUP_FILE=%BACKUP_DIR%\backup-prod-%TIMESTAMP%.sql

echo ======================================================
echo Backup PostgreSQL - AgendaMais PROD
echo ======================================================
echo.
echo Database: %PGDATABASE%
echo Host: %PGHOST%:%PGPORT%
echo User: %PGUSER%
echo.
echo Backup sera salvo em:
echo %BACKUP_FILE%
echo.
echo Iniciando backup...
echo.

REM Executar pg_dump
"%PGBIN%\pg_dump.exe" -U %PGUSER% -h %PGHOST% -p %PGPORT% %PGDATABASE% > "%BACKUP_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================================
    echo ✓ BACKUP CONCLUIDO COM SUCESSO!
    echo ======================================================
    echo Arquivo: %BACKUP_FILE%
    
    REM Mostrar tamanho do arquivo
    for %%A in ("%BACKUP_FILE%") do (
        echo Tamanho: %%~zA bytes
    )
    echo.
) else (
    echo.
    echo ======================================================
    echo ✗ ERRO AO FAZER BACKUP!
    echo ======================================================
    echo Verifique:
    echo - PostgreSQL esta rodando?
    echo - Credenciais estao corretas?
    echo - Path do pg_dump esta correto?
    echo.
)

pause
