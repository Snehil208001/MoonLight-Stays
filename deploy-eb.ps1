# Build and create Elastic Beanstalk deployment package (manual deployment)
# Run from project root: .\deploy-eb.ps1
# CI/CD disabled - use this script to deploy without CodePipeline/CodeBuild

$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot
Set-Location $projectRoot\airBnbApp

Write-Host "Building JAR..." -ForegroundColor Cyan
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) { exit 1 }

$jarPath = "target\application.jar"
if (-not (Test-Path $jarPath)) { $jarPath = "target\airBnbApp-0.0.1-SNAPSHOT.jar" }
if (-not (Test-Path $jarPath)) {
    Write-Host "JAR not found at $jarPath" -ForegroundColor Red
    exit 1
}

$zipPath = "target\airbnb-eb-deploy.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath }

Write-Host "Creating deployment package..." -ForegroundColor Cyan
Copy-Item $jarPath -Destination "application.jar" -Force
Compress-Archive -Path "application.jar", ".ebextensions", "Procfile" -DestinationPath $zipPath -Force
Remove-Item "application.jar" -Force

$fullPath = (Resolve-Path $zipPath).Path
Write-Host "Done: $fullPath" -ForegroundColor Green
Write-Host "Upload this zip to Elastic Beanstalk: Upload and deploy" -ForegroundColor Yellow
