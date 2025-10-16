@echo off
echo Carregando variaveis do .env (se existir)...
if exist ".env" (
	echo Gerando arquivo temporario .env.generated.bat...
	powershell -NoProfile -Command "Get-Content -Raw '.env' -ErrorAction SilentlyContinue -Encoding UTF8 | Select-String -Pattern '^(?!\s*#)\s*.*' | ForEach-Object { $line=$_.Line; $parts=$line -split '=',2; if ($parts.Length -ge 2) { $k=$parts[0].Trim(); $v=$parts[1].Trim(); $v=$v.Trim('"'' '); $KUp=$k.ToUpper(); $out = 'set ' + $k + '=' + $v; if ($KUp -match 'SPRING\.MAIL\.HOST|SPRING_MAIL_HOST|SPRING.MAIL.HOST') { $out += "`nset SPRING_MAIL_HOST=$v" }; if ($KUp -match 'SPRING\.MAIL\.USERNAME|SPRING_MAIL_USERNAME|SPRING.MAIL.USERNAME') { $out += "`nset SPRING_MAIL_USERNAME=$v" }; if ($KUp -match 'SPRING\.MAIL\.PASSWORD|SPRING_MAIL_PASSWORD|SPRING.MAIL.PASSWORD') { $out += "`nset SPRING_MAIL_PASSWORD=$v" }; if ($KUp -match 'SPRING\.MAIL\.PORT|SPRING_MAIL_PORT|SPRING.MAIL.PORT') { $out += "`nset SPRING_MAIL_PORT=$v" }; if ($KUp -match 'GESTOR_EMAIL|GESTOR.EMAIL|GESTOR_EMAIL') { $out += "`nset GESTOR_EMAIL=$v" }; $out } } | Out-File -Encoding ASCII .env.generated.bat;"
	if exist .env.generated.bat (
		call .env.generated.bat
		del /f /q .env.generated.bat >nul 2>&1
	)
)

REM --- Validação de variáveis obrigatórias ---
REM --- Validação de variáveis obrigatórias (pular se SKIP_ENV_VALIDATION estiver definido) ---
if /i "%SKIP_ENV_VALIDATION%"=="1" goto :skip_validation
if /i "%SKIP_ENV_VALIDATION%"=="true" goto :skip_validation

if "%SPRING_MAIL_HOST%"=="" (
	echo ERRO: variavel SPRING_MAIL_HOST nao definida. Verifique o arquivo .env ou variaveis de ambiente.
	echo Para ignorar esta validação defina SKIP_ENV_VALIDATION=1
	exit /b 1
)
if "%SPRING_MAIL_USERNAME%"=="" (
	echo ERRO: variavel SPRING_MAIL_USERNAME nao definida. Verifique o arquivo .env ou variaveis de ambiente.
	echo Para ignorar esta validação defina SKIP_ENV_VALIDATION=1
	exit /b 1
)
if "%SPRING_MAIL_PASSWORD%"=="" (
	echo ERRO: variavel SPRING_MAIL_PASSWORD nao definida. Verifique o arquivo .env ou variaveis de ambiente.
	echo Para ignorar esta validação defina SKIP_ENV_VALIDATION=1
	exit /b 1
)

:skip_validation
echo Iniciando AgendaMais em modo PRODUCAO...
mvn spring-boot:run -Dspring-boot.run.profiles=prod
