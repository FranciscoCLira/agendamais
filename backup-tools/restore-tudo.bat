@echo off
echo ==============================================
echo DROP + RESTORE COMPLETO DO BANCO H2 - Agenda Mais
echo ==============================================

:: Caminho do JAR do H2
set H2JAR=C:\libs\h2\h2-2.1.214.jar

:: Caminho do script de DROP
set DROP_SCRIPT=C:\DEV-IA2\agendamais\src\main\resources\sql-scripts\drop_all_tables.sql

:: Caminho do script de RESTORE
set RESTORE_SCRIPT=C:\DEV-IA2\agendamais\src\main\resources\sql-backups\backup_completo.sql

:: URL do banco (ajuste conforme seu path)
set DB_URL=jdbc:h2:file:C:/DEV-IA2/agendamais/data/agendadb

:: Rodar DROP TABLES
echo.
echo Executando DROP de todas as tabelas...
java -cp target\classes;%H2JAR% org.h2.tools.RunScript ^
  -url %DB_URL% ^
  -user sa ^
  -script %DROP_SCRIPT%

:: Rodar RESTORE
echo.
echo Restaurando backup completo...
java -cp target\classes;%H2JAR% org.h2.tools.RunScript ^
  -url %DB_URL% ^
  -user sa ^
  -script %RESTORE_SCRIPT%

echo.
echo DROP + RESTORE FINALIZADO COM SUCESSO!
pause
