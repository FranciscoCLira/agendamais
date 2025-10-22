@echo off
REM recreate-h2-db.bat
REM Script to recreate the H2 database from scratch for development
REM This will delete existing database files and recreate with fresh schema and data

echo ==================================================
echo   Recreating H2 Database for Development
echo ==================================================
echo.

REM Define database directory and file patterns
set DATA_DIR=.\data
set DB_FILE1=%DATA_DIR%\agendadb.mv.db
set DB_FILE2=%DATA_DIR%\agendadb.trace.db

echo Step 1: Checking for existing H2 database files...
set DB_EXISTS=0
if exist "%DB_FILE1%" (
    echo   Found: %DB_FILE1%
    set DB_EXISTS=1
)
if exist "%DB_FILE2%" (
    echo   Found: %DB_FILE2%
    set DB_EXISTS=1
)

if %DB_EXISTS%==0 (
    echo   No existing database files found.
) else (
    echo.
    echo Step 2: Deleting existing database files...
    if exist "%DB_FILE1%" (
        del /F /Q "%DB_FILE1%"
        echo   Deleted: %DB_FILE1%
    )
    if exist "%DB_FILE2%" (
        del /F /Q "%DB_FILE2%"
        echo   Deleted: %DB_FILE2%
    )
    echo   Database files removed successfully.
)

echo.
echo Step 3: Setting up application to recreate database...
echo   - Using spring.jpa.hibernate.ddl-auto=create
echo   - Using app.reload-data=true
echo.

REM Create temporary properties override
set TEMP_PROPS=recreate-db-temp.properties
(
echo # Temporary properties to recreate database from scratch
echo spring.jpa.hibernate.ddl-auto=create
echo app.reload-data=true
) > %TEMP_PROPS%

echo Step 4: Starting application to recreate database and load initial data...
echo   (This will compile and run the Spring Boot application)
echo   Press Ctrl+C to stop after seeing "DataLoader.java - Recarregou a base de dados"
echo.

REM Load environment variables from .env if it exists, then run Maven
if exist .env (
    echo Loading environment variables from .env file...
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\run-with-env.ps1 -Profile dev -PrintVarsOnly > .env.generated.vars.txt
    REM Load the variables into current session
    for /f "usebackq tokens=1,* delims==" %%a in (".env.generated.vars.txt") do set "%%a=%%b"
    del /F /Q .env.generated.vars.txt
)

REM Run Maven with additional properties
call mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.config.additional-location=file:./%TEMP_PROPS%"

set EXIT_CODE=%ERRORLEVEL%

REM Cleanup temporary file
if exist %TEMP_PROPS% del /F /Q %TEMP_PROPS%

if %EXIT_CODE%==0 (
    echo.
    echo ==================================================
    echo   H2 Database recreated successfully!
    echo ==================================================
    echo.
    echo You can now run the application normally using:
    echo   run-dev.bat
    echo.
) else (
    echo.
    echo ERROR: Application failed to start. Exit code: %EXIT_CODE%
    echo Please check the logs above for details.
    exit /b %EXIT_CODE%
)

exit /b 0
