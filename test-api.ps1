# Quick API test after CI/CD deploy
# Usage: .\test-api.ps1

$baseUrl = "http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1"
$today = (Get-Date).ToString("yyyy-MM-dd")
$tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")

Write-Host "Testing Moonlight Stays API..." -ForegroundColor Cyan
Write-Host ""

# Test 1: Hotel search
Write-Host "1. Hotel Search (POST /hotels/search)..." -ForegroundColor Yellow
try {
    $body = @{ city = ""; checkInDate = $today; endDate = $tomorrow; roomsCount = 1 } | ConvertTo-Json
    $r = Invoke-WebRequest -Uri "$baseUrl/hotels/search" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
    $data = $r.Content | ConvertFrom-Json
    $count = if ($data.data) { $data.data.Count } else { 0 }
    Write-Host "   OK - Found $count hotels" -ForegroundColor Green
} catch {
    Write-Host "   FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Swagger/health
Write-Host "2. API reachable..." -ForegroundColor Yellow
try {
    $r = Invoke-WebRequest -Uri "$baseUrl/swagger-ui/index.html" -Method GET -UseBasicParsing
    Write-Host "   OK - Swagger UI available" -ForegroundColor Green
} catch {
    Write-Host "   FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Done. API base: $baseUrl" -ForegroundColor Cyan
