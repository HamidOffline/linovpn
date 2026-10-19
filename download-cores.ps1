# Download Core Libraries Script for LinoVPN
param(
    [string]$TargetDir = "app\src\main\jniLibs"
)

$ErrorActionPreference = "Continue"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " LinoVPN Native Cores Downloader" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$abis = @("arm64-v8a", "x86_64")

foreach ($abi in $abis) {
    $dir = Join-Path $TargetDir $abi
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
}

Write-Host "Ready to download Xray-core, sing-box and Clash Meta native libraries..." -ForegroundColor Green
Write-Host "Target directory: $TargetDir" -ForegroundColor Yellow
Write-Host "You can also open this project directly in Android Studio." -ForegroundColor Cyan
