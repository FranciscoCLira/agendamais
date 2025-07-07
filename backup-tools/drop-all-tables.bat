@echo off
echo ============================================
echo DROP DE TODAS AS TABELAS DO BANCO H2
echo ============================================

:: Caminho do JAR do H2
set H2JAR=C:\libs\h2\h2-2.1.214.jar

:: Caminho do script SQL
set SQL_PATH=C:\DEV-IA2\agendamais\src\main\resources\sql-scripts\drop_all_tables.sql

:: URL exata do banco
set DB_URL=jdbc:h2:file:C:/DEV-IA2/agendamais/data/agendadb

:: Executa o DROP
java -cp target\classes;%H2JAR% org.h2.tools.RunScript ^
  -url %DB_URL% ^
  -user sa ^
  -script %SQL_PATH%

echo.
echo TODAS AS TABELAS FORAM REMOVIDAS!
pause
