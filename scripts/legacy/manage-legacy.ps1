param(
    [switch]$ListOnly,
    [switch]$Delete
)

$legacyDir = Split-Path -Parent $PSScriptRoot
Write-Host "Legacy scripts directory: $legacyDir"
$files = Get-ChildItem -Path $legacyDir -File | Select-Object -ExpandProperty Name

if ($files.Count -eq 0) { Write-Host "No legacy files found."; exit 0 }

Write-Host "Found legacy files:`n  $($files -join "`n  ")"

if ($ListOnly) { exit 0 }

if ($Delete) {
    Write-Host "Deleting legacy files..."
    Get-ChildItem -Path $legacyDir -File | Remove-Item -Force -Verbose
    Write-Host "Deleted."
    exit 0
}

Write-Host "To delete these files non-interactively run this script with -Delete."
exit 0
