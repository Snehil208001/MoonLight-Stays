# API test script for AI Trip Planner
# Usage: .\test-trip-planner.ps1 -BaseUrl "http://localhost:8080/api/v1" -City "Tokyo" -Interests "food","temples" -Budget "MODERATE"

param (
    [string]$BaseUrl = "https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1",
    [string]$City = "Tokyo",
    [string[]]$Interests = @("food", "temples", "shopping"),
    [string]$Budget = "MODERATE",
    [int]$Nights = 3
)

$today = (Get-Date).ToString("yyyy-MM-dd")
$endDate = (Get-Date).AddDays($Nights).ToString("yyyy-MM-dd")

# Randomize email for registration
$randomNum = Get-Random -Minimum 1000 -Maximum 9999
$email = "testuser_$randomNum@example.com"
$password = "Password123!"
$name = "Test User"

Write-Host "Testing AI Trip Planner API..." -ForegroundColor Cyan
Write-Host "API Endpoint Base: $BaseUrl" -ForegroundColor Gray
Write-Host "Target City: $City" -ForegroundColor Gray
Write-Host "Interests: $($Interests -join ', ')" -ForegroundColor Gray
Write-Host "Budget Level: $Budget" -ForegroundColor Gray
Write-Host "Dates: $today to $endDate ($Nights nights)" -ForegroundColor Gray
Write-Host ""

# Step 1: Register User
Write-Host "1. Registering a test user ($email)..." -ForegroundColor Yellow
$signupBody = @{
    email = $email
    password = $password
    name = $name
} | ConvertTo-Json

try {
    $signupResponse = Invoke-WebRequest -Uri "$BaseUrl/auth/signup" -Method POST -ContentType "application/json" -Body $signupBody -UseBasicParsing
    Write-Host "   OK - User registered successfully" -ForegroundColor Green
} catch {
    Write-Host "   FAIL - $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorDetails = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream()).ReadToEnd()
        Write-Host "   Details: $errorDetails" -ForegroundColor Red
    }
    Exit 1
}

# Step 2: Login User to get JWT token
Write-Host "2. Logging in to retrieve access token..." -ForegroundColor Yellow
$loginBody = @{
    email = $email
    password = $password
} | ConvertTo-Json

$token = $null
try {
    $loginResponse = Invoke-WebRequest -Uri "$BaseUrl/auth/login" -Method POST -ContentType "application/json" -Body $loginBody -UseBasicParsing
    $loginData = $loginResponse.Content | ConvertFrom-Json
    
    # Check if inside standard enveloped response
    if ($loginData.data -and $loginData.data.accessToken) {
        $token = $loginData.data.accessToken
    } elseif ($loginData.accessToken) {
        $token = $loginData.accessToken
    } else {
        # Fallback to check raw content if structure is different
        Write-Host "   FAIL - Access token not found in login response" -ForegroundColor Red
        Write-Host "   Response: $($loginResponse.Content)" -ForegroundColor Gray
        Exit 1
    }
    Write-Host "   OK - Token obtained successfully" -ForegroundColor Green
} catch {
    Write-Host "   FAIL - $($_.Exception.Message)" -ForegroundColor Red
    Exit 1
}

# Step 3: Call AI Trip Planner
Write-Host "3. Generating AI Trip Plan (POST /ai/trip-plan)..." -ForegroundColor Yellow
$tripBody = @{
    city = $City
    checkInDate = $today
    checkOutDate = $endDate
    numberOfGuests = 2
    interests = $Interests
    budgetLevel = $Budget
} | ConvertTo-Json -Depth 5

$headers = @{
    Authorization = "Bearer $token"
}

try {
    $tripResponse = Invoke-WebRequest -Uri "$BaseUrl/ai/trip-plan" -Method POST -ContentType "application/json" -Body $tripBody -Headers $headers -UseBasicParsing
    $tripData = $tripResponse.Content | ConvertFrom-Json
    
    # Extract data from envelope
    $plan = if ($tripData.data) { $tripData.data } else { $tripData }
    
    Write-Host "   OK - Trip Plan Generated!" -ForegroundColor Green
    Write-Host ""
    Write-Host "-------------------- TRIP PLAN RESULT --------------------" -ForegroundColor Cyan
    Write-Host "Destination: $($plan.destination)" -ForegroundColor White
    Write-Host "Summary:     $($plan.summary)" -ForegroundColor White
    Write-Host ""
    
    foreach ($day in $plan.days) {
        Write-Host "Day $($day.day): $($day.title)" -ForegroundColor Yellow
        Write-Host "Meal Suggestion: $($day.mealSuggestion)" -ForegroundColor Gray
        Write-Host "Activities:" -ForegroundColor Gray
        foreach ($act in $day.activities) {
            Write-Host "  [$($act.timeOfDay)] $($act.title) - $($act.description)" -ForegroundColor LightGray
        }
        Write-Host ""
    }
    
    Write-Host "Tips:" -ForegroundColor Yellow
    foreach ($tip in $plan.tips) {
        Write-Host "  * $tip" -ForegroundColor LightGray
    }
    Write-Host "----------------------------------------------------------" -ForegroundColor Cyan

} catch {
    Write-Host "   FAIL - $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorDetails = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream()).ReadToEnd()
        Write-Host "   Details: $errorDetails" -ForegroundColor Red
    }
}
