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
