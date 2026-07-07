# Start all dev servers: Backend, Frontend, Stripe CLI
# Run from project root: .\start-dev.ps1

Write-Host "Starting Backend, Frontend, and Stripe webhook listener..." -ForegroundColor Cyan
Write-Host ""

$backend = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\airBnbApp'; Get-Content .env | ForEach-Object { if (`$_.Trim() -and -not `$_.StartsWith('#')) { `$name, `$value = `$_.Split('=', 2); [System.Environment]::SetEnvironmentVariable(`$name.Trim(), `$value.Trim(), 'Process') } }; mvn spring-boot:run" -PassThru
$frontend = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\moonlight-stays'; npm run dev" -PassThru
$stripe = Start-Process powershell -ArgumentList "-NoExit", "-Command", "stripe listen --forward-to localhost:5000/api/v1/webhooks/payment" -PassThru

Write-Host "Started 3 windows:" -ForegroundColor Green
Write-Host "  - Backend (Spring Boot) - port 5000"
Write-Host "  - Frontend (Next.js) - port 3000"
Write-Host "  - Stripe CLI - forwarding webhooks"
Write-Host ""
Write-Host "Close each window to stop that service." -ForegroundColor Yellow
