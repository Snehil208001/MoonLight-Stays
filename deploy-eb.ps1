# Build and create Elastic Beanstalk deployment package
# Run from project root: .\deploy-eb.ps1

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\airBnbApp

Write-Host "Building JAR..." -ForegroundColor Cyan
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) { exit 1 }

$jarPath = "target\application.jar"
# Fallback if finalName not applied
if (-not (Test-Path $jarPath)) { $jarPath = "target\airBnbApp-0.0.1-SNAPSHOT.jar" }
if (-not (Test-Path $jarPath)) {
    Write-Host "JAR not found at $jarPath" -ForegroundColor Red
    exit 1
}

$zipPath = "target\airbnb-eb-deploy.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath }

Write-Host "Creating deployment package..." -ForegroundColor Cyan
$jarName = Split-Path $jarPath -Leaf
Copy-Item $jarPath -Destination "application.jar" -Force
Compress-Archive -Path "application.jar", ".ebextensions" -DestinationPath $zipPath -Force
Remove-Item "application.jar" -Force

Write-Host "Done: $zipPath" -ForegroundColor Green
Write-Host "Upload this zip to Elastic Beanstalk (Upload and deploy)" -ForegroundColor Yellow
