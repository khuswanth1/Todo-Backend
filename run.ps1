if (Test-Path .env) {
    Write-Host "Loading environment variables from .env..." -ForegroundColor Gray
    Get-Content .env | ForEach-Object {
        if ($_ -match '^\s*setx\s+(\w+)\s+"([^"]*)"') {
            [System.Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], "Process")
        } elseif ($_ -match '^\s*([^#=\s]+)\s*=\s*"([^"]*)"') {
            [System.Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], "Process")
        } elseif ($_ -match '^\s*([^#=\s]+)\s*=\s*([^\s]+)') {
            [System.Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], "Process")
        }
    }
}

Write-Host "Setting up Database..." -ForegroundColor Yellow
# Try to create the 'todo' database if it doesn't exist using the credentials from application.properties
mysql -u root -p2205 -e "CREATE DATABASE IF NOT EXISTS todo;"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Warning: Could not automatically create the database. Make sure MySQL is running." -ForegroundColor Red
} else {
    Write-Host "Database 'todo' is ready!" -ForegroundColor Green
}

Write-Host "Compiling and Running Spring Boot..." -ForegroundColor Cyan
.\mvnw clean spring-boot:run
