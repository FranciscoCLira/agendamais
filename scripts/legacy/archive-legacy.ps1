param(
    [switch]$Delete
)

$legacyDir = Split-Path -Parent $PSScriptRoot
if (-Not (Test-Path $legacyDir)) { Write-Error "Legacy dir not found: $legacyDir"; exit 1 }

$timestamp = (Get-Date).ToString('yyyyMMdd-HHmmss')
$archiveDir = Join-Path $legacyDir "archive-$timestamp"
New-Item -ItemType Directory -Path $archiveDir | Out-Null

$files = Get-ChildItem -Path $legacyDir -File | Where-Object { $_.Name -ne (Split-Path -Leaf $MyInvocation.MyCommand.Path) }
if ($files.Count -eq 0) { Write-Host "No legacy files to archive."; exit 0 }

foreach ($f in $files) {
    Move-Item -Path $f.FullName -Destination (Join-Path $archiveDir $f.Name)
}
Write-Host "Archived $($files.Count) files to $archiveDir"

if ($Delete) {
    Write-Host "Deleting archive directory contents (Delete flag provided)"
    Get-ChildItem -Path $archiveDir -File | Remove-Item -Force -Verbose
}

exit 0
