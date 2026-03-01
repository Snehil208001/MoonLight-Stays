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

# EB requires files at ZIP ROOT (no parent folder). Use temp dir to ensure correct structure.
$tempDir = "target\eb-deploy-temp"
if (Test-Path $tempDir) { Remove-Item $tempDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

Write-Host "Creating deployment package..." -ForegroundColor Cyan
Copy-Item $jarPath -Destination "$tempDir\application.jar" -Force
Copy-Item "Procfile" -Destination "$tempDir\Procfile" -Force
Copy-Item ".ebextensions" -Destination "$tempDir\.ebextensions" -Recurse -Force

# Use .NET ZipFile (Compress-Archive excludes .ebextensions on Windows)
Add-Type -AssemblyName System.IO.Compression.FileSystem
$airBnbAppDir = Join-Path $projectRoot "airBnbApp"
$tempDirFull = Join-Path $airBnbAppDir $tempDir
$zipFullPath = Join-Path $airBnbAppDir $zipPath
$zip = [System.IO.Compression.ZipFile]::Open($zipFullPath, 'Create')
try {
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, (Join-Path $tempDirFull "application.jar"), "application.jar", 'Optimal') | Out-Null
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, (Join-Path $tempDirFull "Procfile"), "Procfile", 'Optimal') | Out-Null
    Get-ChildItem (Join-Path $tempDirFull ".ebextensions") -Recurse -File | ForEach-Object {
        $rel = $_.FullName.Substring($tempDirFull.Length + 1).Replace('\', '/')
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, $rel, 'Optimal') | Out-Null
    }
} finally { $zip.Dispose() }
Remove-Item $tempDir -Recurse -Force

Write-Host "Done: $zipFullPath" -ForegroundColor Green
Write-Host "Upload this zip to Elastic Beanstalk: Upload and deploy" -ForegroundColor Yellow
