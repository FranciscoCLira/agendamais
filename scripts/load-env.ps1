param(
    [switch]$ShowVars,
    [switch]$PrintFull
)

$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env'
$outBat = Join-Path $projectRoot '.env.generated.bat'
$outVars = Join-Path $projectRoot '.env.generated.vars.txt'
if (-Not (Test-Path $envFile)) { if ($ShowVars) { exit 0 } else { exit 0 } }

$lines = Get-Content $envFile -ErrorAction SilentlyContinue -Encoding UTF8 | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" -and -not $_.StartsWith('#') }
$outputs = @()
foreach ($line in $lines) {
    $parts = $line -split '=',2
    if ($parts.Length -ge 2) {
        $k = $parts[0].Trim()
        $v = $parts[1].Trim()
        $v = $v.Trim('"'' ')
        $Kup = $k.ToUpper()
        $outputs += "set $k=$v"
        if ($Kup -match 'SPRING\\.MAIL\\.HOST|SPRING_MAIL_HOST|SPRING.MAIL.HOST') { $outputs += "set SPRING_MAIL_HOST=$v" }
        if ($Kup -match 'SPRING\\.MAIL\\.USERNAME|SPRING_MAIL_USERNAME|SPRING.MAIL.USERNAME') { $outputs += "set SPRING_MAIL_USERNAME=$v" }
        if ($Kup -match 'SPRING\\.MAIL\\.PASSWORD|SPRING_MAIL_PASSWORD|SPRING.MAIL.PASSWORD') { $outputs += "set SPRING_MAIL_PASSWORD=$v" }
        if ($Kup -match 'SPRING\\.MAIL\\.PORT|SPRING_MAIL_PORT|SPRING.MAIL.PORT') { $outputs += "set SPRING_MAIL_PORT=$v" }
        if ($Kup -match 'GESTOR_EMAIL|GESTOR.EMAIL|GESTOR_EMAIL') { $outputs += "set GESTOR_EMAIL=$v" }
    }
}

if ($outputs.Count -gt 0) {
    $outputs | Out-File -FilePath $outBat -Encoding ASCII
    Select-String -Path $outBat -Pattern '^set\s+([A-Za-z0-9_]+)=' -AllMatches | ForEach-Object { foreach ($m in $_.Matches) { $m.Groups[1].Value } } | Sort-Object -Unique | Out-File -FilePath $outVars -Encoding ASCII
    if ($ShowVars) {
        if ($PrintFull) {
            Get-Content $outBat | Write-Output
        }
        Get-Content $outVars | ForEach-Object { Write-Output ("$($_)=" + [Environment]::GetEnvironmentVariable($_)) }
    }
}
exit 0
