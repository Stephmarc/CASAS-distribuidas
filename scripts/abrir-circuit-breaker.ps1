param(
    [Parameter(Mandatory=$true)][long]$ReservaId,
    [string]$Gateway = "http://localhost:18080",
    [string]$Keycloak = "http://localhost:18090"
)

$token = (Invoke-RestMethod -Method Post `
    -Uri "$Keycloak/realms/casa-o-nada/protocol/openid-connect/token" `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{client_id="casa-o-nada-web"; grant_type="password"; username="cliente"; password="Cliente123!"}).access_token
$headers = @{Authorization="Bearer $token"}

Write-Host "IMPORTANTE: propiedad-ms debe estar detenido para esta prueba." -ForegroundColor Yellow
Write-Host "Ejecutando 4 llamadas para superar la ventana minima del Circuit Breaker..." -ForegroundColor Cyan
for ($i=1; $i -le 4; $i++) {
    $r = Invoke-RestMethod -Uri "$Gateway/api/v1/reservas/detalle/$ReservaId" -Headers $headers
    Write-Host "Llamada $i -> degradado=$($r.degradado) | $($r.mensaje)"
}

Write-Host "`nEstado del Circuit Breaker:" -ForegroundColor Green
Invoke-RestMethod -Uri "$Gateway/api/v1/reservas/_circuit-breaker" -Headers $headers | ConvertTo-Json -Depth 5
