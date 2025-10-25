param(
    [switch]$PrintVarsOnly,
    [switch]$Debug,
    [string]$Profile = 'dev',
    [switch]$ForceValidate,
    [switch]$SkipValidate,
    [string]$RequiredEnvVars
)

Set-StrictMode -Version Latest

$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot

$envFile = Join-Path $projectRoot '.env'
if (-Not (Test-Path $envFile)) {
    Write-Verbose ".env not found"
    Pop-Location
    return
}

# Read .env lines, ignore comments/empty
$lines = Get-Content $envFile -ErrorAction Stop | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' -and -not $_.StartsWith('#') }

$map = @{}
foreach ($line in $lines) {
    $parts = $line -split '=',2
    if ($parts.Count -ge 2) {
        $k = $parts[0].Trim()
        $v = $parts[1].Trim().Trim('"')
        $map[$k] = $v
        $Kup = $k.ToUpper()
        if ($Kup -match 'SPRING\.MAIL\.HOST|SPRING_MAIL_HOST|SPRING.MAIL.HOST') { $map['SPRING_MAIL_HOST'] = $v }
        if ($Kup -match 'SPRING\.MAIL\.USERNAME|SPRING_MAIL_USERNAME|SPRING.MAIL.USERNAME') { $map['SPRING_MAIL_USERNAME'] = $v }
        if ($Kup -match 'SPRING\.MAIL\.PASSWORD|SPRING_MAIL_PASSWORD|SPRING.MAIL.PASSWORD') { $map['SPRING_MAIL_PASSWORD'] = $v }
        if ($Kup -match 'SPRING\.MAIL\.PORT|SPRING_MAIL_PORT|SPRING.MAIL.PORT') { $map['SPRING_MAIL_PORT'] = $v }
        if ($Kup -match 'GESTOR_EMAIL|GESTOR.EMAIL|GESTOR_EMAIL') { $map['GESTOR_EMAIL'] = $v }
    }
}

# Export to process env
foreach ($k in $map.Keys) { [System.Environment]::SetEnvironmentVariable($k, $map[$k], 'Process') }

if ($Debug) { Write-Host "-- Debug mode: variables loaded --" }

if ($PrintVarsOnly) {
    foreach ($k in ($map.Keys | Sort-Object)) {
        Write-Output "$k=$($map[$k])"
    }
    Pop-Location
    return
}

# Determine validation
$validate = $false
if ($ForceValidate) { $validate = $true }
elseif ($SkipValidate) { $validate = $false }
else { $validate = ($Profile -eq 'prod') }

# Determine required list
if ($RequiredEnvVars) { $required = $RequiredEnvVars.Split(',') } elseif (Test-Path (Join-Path $projectRoot '.env.required')) { $required = (Get-Content (Join-Path $projectRoot '.env.required') | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' } ) } else { $required = @('SPRING_MAIL_HOST','SPRING_MAIL_USERNAME','SPRING_MAIL_PASSWORD') }

if ($validate) {
    foreach ($r in $required) {
        if (-not [System.String]::IsNullOrEmpty([System.Environment]::GetEnvironmentVariable($r,'Process'))) { continue } else { Write-Error "Variavel obrigatoria $r nao definida"; Pop-Location; exit 1 }
    }
}

# If we reach here, either run mvn or just indicate success
Write-Host "Environment loaded. Profile=$Profile Validate=$validate"
if ($Profile -eq 'dev') {
    $args = @('spring-boot:run', '-Dspring-boot.run.profiles=dev')
} elseif ($Profile -eq 'prod') {
    $args = @('spring-boot:run', '-Dspring-boot.run.profiles=prod')
} else {
    $args = @('spring-boot:run', "-Dspring-boot.run.profiles=$Profile")
}

Write-Host "Starting mvn $($args -join ' ')"
& mvn @args

Pop-Location
exit 0
