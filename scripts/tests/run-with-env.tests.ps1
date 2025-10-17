Import-Module Pester -ErrorAction Stop

Describe 'run-with-env.ps1 parsing and mapping' {
    It 'parses simple key=value and maps spring.mail.host variants' {
    # tests are in scripts/tests; project root is two levels up
    $projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
        $envFile = Join-Path $projectRoot '.env'
        $backup = $null
        if (Test-Path $envFile) { $backup = "$envFile.bak"; Copy-Item $envFile $backup -Force }

        @(
            '# comment'
            'spring.mail.host=smtp.test'
            'SPRING_MAIL_USERNAME=user@test'
            'spring_mail_password=pass'
        ) | Out-File -FilePath $envFile -Encoding UTF8

        $script = Join-Path (Split-Path -Parent $PSScriptRoot) 'run-with-env.ps1'
    $out = & $script -PrintVarsOnly -Debug -Profile dev -RequiredEnvVars 'SPRING_MAIL_HOST,SPRING_MAIL_USERNAME,SPRING_MAIL_PASSWORD' | Out-String
    if (-not ($out -match 'SPRING_MAIL_HOST=smtp.test')) { throw "Expected SPRING_MAIL_HOST=smtp.test in output: `n$out" }

        Remove-Item $envFile -ErrorAction SilentlyContinue
        if ($backup) { Move-Item $backup $envFile -Force }
    }

    It 'ignores empty lines and comments' {
    # tests are in scripts/tests; project root is two levels up
    $projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
        $envFile = Join-Path $projectRoot '.env'
        $backup = $null
        if (Test-Path $envFile) { $backup = "$envFile.bak"; Copy-Item $envFile $backup -Force }

        @(
            ''
            '# another comment'
            'GESTOR_EMAIL=gestor@example.com'
        ) | Out-File -FilePath $envFile -Encoding UTF8

        $script = Join-Path (Split-Path -Parent $PSScriptRoot) 'run-with-env.ps1'
    $out = & $script -PrintVarsOnly -Debug -Profile dev -RequiredEnvVars 'GESTOR_EMAIL' | Out-String
    if (-not ($out -match 'GESTOR_EMAIL=gestor@example.com')) { throw "Expected GESTOR_EMAIL=gestor@example.com in output: `n$out" }

        Remove-Item $envFile -ErrorAction SilentlyContinue
        if ($backup) { Move-Item $backup $envFile -Force }
    }
}
