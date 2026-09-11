$ErrorActionPreference = "Stop"
Write-Host "1/4 PostgreSQL y Keycloak" -ForegroundColor Cyan
docker compose -f compose-dev.yml up -d
Write-Host "2/4 Abre otra terminal: cd infra\casa-config ; .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
Write-Host "3/4 Luego: cd infra\casa-eureka ; .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
Write-Host "4/4 Levanta servicios y al final casa-gateway. Ver README.md" -ForegroundColor Yellow
