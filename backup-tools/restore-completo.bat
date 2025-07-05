@echo off
echo ============================================
echo RESTORE COMPLETO DO BANCO H2 - Agenda Mais
echo ============================================

:: Caminho do JAR do H2
set H2JAR=C:\libs\h2\h2-2.1.214.jar


:: Pasta destino do BACKUP 
:: set BACKUP_PATH=.\src\main\resources\sql-backups
   set BACKUP_PATH=C:\DEV-IA2\agendamais\src\main\resources\sql-backups

:: set DB_URL=jdbc:h2:file:./data/agendadb
set DB_URL=jdbc:h2:file:C:/dev-IA2/agendamais/data/agendadb

:: Restore completo
java -cp target\classes;%H2JAR% org.h2.tools.RunScript ^
  -url %DB_URL% ^
  -user sa ^
  -script %BACKUP_PATH%\backup_completo.sql

echo.
echo RESTORE COMPLETO FINALIZADO!
pause
