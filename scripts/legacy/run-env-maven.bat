
@echo off
REM Carrega variáveis do .env (ignora comentários e linhas vazias) e executa o Maven
if exist ".env" (
	echo Gerando arquivo temporario .env.generated.bat...
	powershell -NoProfile -Command "Get-Content -Raw '.env' -ErrorAction SilentlyContinue -Encoding UTF8 | Select-String -Pattern '^(?!\s*#)\s*.*' | ForEach-Object { $line=$_.Line; $parts=$line -split '=',2; if ($parts.Length -ge 2) { $k=$parts[0].Trim(); $v=$parts[1].Trim(); $v=$v.Trim('\"'' '); $KUp=$k.ToUpper(); $out = 'set ' + $k + '=' + $v; if ($KUp -match 'SPRING\.MAIL\.HOST|SPRING_MAIL_HOST|SPRING.MAIL.HOST') { $out += "`nset SPRING_MAIL_HOST=$v" }; if ($KUp -match 'SPRING\.MAIL\.USERNAME|SPRING_MAIL_USERNAME|SPRING.MAIL.USERNAME') { $out += "`nset SPRING_MAIL_USERNAME=$v" }; if ($KUp -match 'SPRING\.MAIL\.PASSWORD|SPRING_MAIL_PASSWORD|SPRING.MAIL.PASSWORD') { $out += "`nset SPRING_MAIL_PASSWORD=$v" }; if ($KUp -match 'SPRING\.MAIL\.PORT|SPRING_MAIL_PORT|SPRING.MAIL.PORT') { $out += "`nset SPRING_MAIL_PORT=$v" }; if ($KUp -match 'GESTOR_EMAIL|GESTOR.EMAIL|GESTOR_EMAIL') { $out += "`nset GESTOR_EMAIL=$v" }; $out } } | Out-File -Encoding ASCII .env.generated.bat;"
	REM Forward any CLI args to the PowerShell runner (e.g. -PrintVarsOnly -Debug)
	powershell -NoProfile -ExecutionPolicy Bypass -File ..\run-with-env.ps1 -Profile dev %*

	REM If runner returned non-zero, stop
	if %ERRORLEVEL% NEQ 0 (
		exit /b %ERRORLEVEL%
	)
)

REM Validação de variáveis obrigatórias configurável por perfil
REM default: este script assume PROFILE=dev (construção local). Use PROFILE=prod para forçar comportamento de prod.
set PROFILE=dev

REM Overrides por variável de ambiente
if /i "%FORCE_ENV_VALIDATION%"=="1" set _VALIDATE=1
if /i "%FORCE_ENV_VALIDATION%"=="true" set _VALIDATE=1
if /i "%SKIP_ENV_VALIDATION%"=="1" set _VALIDATE=0
if /i "%SKIP_ENV_VALIDATION%"=="true" set _VALIDATE=0

if not defined _VALIDATE (
	if /i "%PROFILE%"=="prod" (
		set _VALIDATE=1
	) else (
		set _VALIDATE=0
	)
)

if "%_VALIDATE%"=="1" (
	REM Determinar lista de variaveis obrigatorias: REQUIRED_ENV_VARS (CSV) > .env.required file > padrao
	set _REQUIRED=
	if exist .env.required (
		for /f "usebackq delims=" %%A in (".env.required") do (
			if not "%%~A"=="" (
				if defined _REQUIRED (
					set _REQUIRED=!_REQUIRED!,%%~A
				) else (
					set _REQUIRED=%%~A
				)
			)
		)
	)
	if defined REQUIRED_ENV_VARS set _REQUIRED=%REQUIRED_ENV_VARS%
	if not defined _REQUIRED set _REQUIRED=SPRING_MAIL_HOST,SPRING_MAIL_USERNAME,SPRING_MAIL_PASSWORD

	REM Checar cada variavel na lista
	setlocal enabledelayedexpansion
	for %%V in (%_REQUIRED:,= %) do (
		if "!%%V!"=="" (
			echo ERRO: variavel obrigatoria %%V nao definida. Verifique o arquivo .env ou variaveis de ambiente.
			echo Para ignorar esta validacao defina SKIP_ENV_VALIDATION=1
			endlocal
			exit /b 1
		)
	)
	endlocal
)

	REM Para compatibilidade, execute build e run separadamente
	mvn clean install
	mvn spring-boot:run

