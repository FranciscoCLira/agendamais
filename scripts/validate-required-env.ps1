param(
    [string]$Required
)

Set-StrictMode -Version Latest

$projectRoot = Split-Path -Parent $PSScriptRoot

# Determine required variables list
if ($Required) {
    $required = $Required.Split(',') | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' }
} elseif (Test-Path (Join-Path $projectRoot '.env.required')) {
    $required = Get-Content (Join-Path $projectRoot '.env.required') | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' }
} else {
    $required = @('SPRING_MAIL_HOST','SPRING_MAIL_USERNAME','SPRING_MAIL_PASSWORD')
}

$missing = @()
foreach ($r in $required) {
    $val = [Environment]::GetEnvironmentVariable($r,'Process')
    if ([string]::IsNullOrEmpty($val)) { $missing += $r }
}

if ($missing.Count -gt 0) {
    Write-Error "Missing required environment variables: $($missing -join ', ')"
    exit 1
} else {
    Write-Host "All required environment variables are present: $($required -join ', ')"
    exit 0
}
