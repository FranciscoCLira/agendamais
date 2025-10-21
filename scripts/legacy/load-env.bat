@echo off
REM load-env.bat - utility to parse .env, generate .env.generated.bat and .env.generated.vars.txt, call it
if exist ".env" (
	echo Gerando .env.generated.bat e .env.generated.vars.txt a partir de .env...
	powershell -NoProfile -Command "Get-Content -Raw '.env' -ErrorAction SilentlyContinue -Encoding UTF8 | Select-String -Pattern '^(?!\s*#)\s*.*' | ForEach-Object { $line=$_.Line; $parts=$line -split '=',2; if ($parts.Length -ge 2) { $k=$parts[0].Trim(); $v=$parts[1].Trim(); $v=$v.Trim('\"'' '); $KUp=$k.ToUpper(); $out = 'set ' + $k + '=' + $v; if ($KUp -match 'SPRING\.MAIL\.HOST|SPRING_MAIL_HOST|SPRING.MAIL.HOST') { $out += "`nset SPRING_MAIL_HOST=$v" }; if ($KUp -match 'SPRING\.MAIL\.USERNAME|SPRING_MAIL_USERNAME|SPRING.MAIL.USERNAME') { $out += "`nset SPRING_MAIL_USERNAME=$v" }; if ($KUp -match 'SPRING\.MAIL\.PASSWORD|SPRING_MAIL_PASSWORD|SPRING.MAIL.PASSWORD') { $out += "`nset SPRING_MAIL_PASSWORD=$v" }; if ($KUp -match 'SPRING\.MAIL\.PORT|SPRING_MAIL_PORT|SPRING.MAIL.PORT') { $out += "`nset SPRING_MAIL_PORT=$v" }; if ($KUp -match 'GESTOR_EMAIL|GESTOR.EMAIL|GESTOR_EMAIL') { $out += "`nset GESTOR_EMAIL=$v" }; $out } } | Out-File -Encoding ASCII .env.generated.bat;"

	REM extrai nomes de variaveis do arquivo gerado
	powershell -NoProfile -Command "Select-String -Path '.env.generated.bat' -Pattern '^set\s+([A-Za-z0-9_]+)=' -AllMatches | ForEach-Object { foreach ($m in $_.Matches) { $m.Groups[1].Value } } | Sort-Object -Unique | Out-File -Encoding ASCII .env.generated.vars.txt;"

	if exist .env.generated.bat (
		call .env.generated.bat
		REM comportamento DEBUG: print e preserve
		if /i "%DEBUG_ENV%"=="1" (
			echo DEBUG_ENV=1 : preservando .env.generated.bat para depuracao
			type .env.generated.bat
			echo --- Variaveis definidas pelo parser:
			for /f "usebackq delims=" %%V in (".env.generated.vars.txt") do (
				call echo %%V=%%%V%%
			)
		) else if /i "%DEBUG_ENV%"=="true" (
			echo DEBUG_ENV=true : preservando .env.generated.bat para depuracao
			type .env.generated.bat
			echo --- Variaveis definidas pelo parser:
			for /f "usebackq delims=" %%V in (".env.generated.vars.txt") do (
				call echo %%V=%%%V%%
			)
		) else (
			del /f /q .env.generated.bat >nul 2>&1
			del /f /q .env.generated.vars.txt >nul 2>&1
		)
	)
) else (
	REM no .env found, nothing to do
)

exit /b 0

@echo off
REM Preserved legacy copy of scripts\load-env.bat
type ..\load-env.bat
exit /b 0
@echo off
REM Legacy copy of load-env.bat preserved for history
type ..\..\load-env.bat
exit /b 0
