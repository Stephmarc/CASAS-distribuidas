$ErrorActionPreference = "Stop"

Write-Host "1/5 PostgreSQL" -ForegroundColor Cyan
docker compose -f compose-dev.yml up -d postgres

Write-Host "2/5 Bases de datos + Keycloak (se recrea para importar realm, usuarios y roles)" -ForegroundColor Cyan
docker compose -f compose-dev.yml up -d db-init
docker compose -f compose-dev.yml up -d --force-recreate keycloak

Write-Host "3/5 Config Server" -ForegroundColor Yellow
Write-Host "Abre otra terminal: cd infra\casa-config ; .\mvnw.cmd spring-boot:run"

Write-Host "4/5 Eureka" -ForegroundColor Yellow
Write-Host "Cuando Config Server este arriba: cd infra\casa-eureka ; .\mvnw.cmd spring-boot:run"

Write-Host "5/5 Microservicios y Gateway" -ForegroundColor Yellow
Write-Host "Levanta primero propiedad-ms y reserva-ms para S06; luego los demas servicios y al final casa-gateway."
Write-Host "Prueba automatica S06/S07: .\scripts\test-s06-s07.ps1" -ForegroundColor Green
