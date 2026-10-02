param(
    [string]$Gateway = "http://localhost:18080",
    [string]$Keycloak = "http://localhost:18090"
)

$ErrorActionPreference = "Stop"
$realm = "casa-o-nada"
$clientId = "casa-o-nada-web"

function Get-Token([string]$Username, [string]$Password) {
    $response = Invoke-RestMethod -Method Post `
        -Uri "$Keycloak/realms/$realm/protocol/openid-connect/token" `
        -ContentType "application/x-www-form-urlencoded" `
        -Body @{ client_id=$clientId; grant_type="password"; username=$Username; password=$Password }
    return $response.access_token
}

Write-Host "Obteniendo tokens JWT..." -ForegroundColor Cyan
$adminToken = Get-Token "admin" "Admin123!"
$agenteToken = Get-Token "agente" "Agente123!"
$clienteToken = Get-Token "cliente" "Cliente123!"

Write-Host "`n[JWT] 200: identidad y roles obtenidos del token" -ForegroundColor Green
Invoke-RestMethod -Uri "$Gateway/api/v1/auth/me" -Headers @{Authorization="Bearer $clienteToken"} | ConvertTo-Json -Depth 6

Write-Host "`n[JWT] 401 esperado: recurso protegido sin token" -ForegroundColor Yellow
try { Invoke-WebRequest -Uri "$Gateway/api/v1/reservas" -UseBasicParsing | Out-Null }
catch { Write-Host "HTTP $([int]$_.Exception.Response.StatusCode)" }

Write-Host "`n[JWT] 403 esperado: CLIENTE intentando crear una propiedad" -ForegroundColor Yellow
$propiedadBody = @{
    titulo="Casa demo S06-S07"
    descripcion="Propiedad creada para probar JWT, Feign y Circuit Breaker"
    ciudad="Juliaca"
    direccion="Av. Demo 123"
    precio=150000
    habitaciones=3
    banos=2
    areaM2=120
    tipoOperacion="COMPRA"
    estado="DISPONIBLE"
    agenteId=1
    imagenPrincipalUrl="https://example.com/casa.jpg"
} | ConvertTo-Json
try {
    Invoke-WebRequest -Method Post -Uri "$Gateway/api/v1/propiedades" -Headers @{Authorization="Bearer $clienteToken"} -ContentType "application/json" -Body $propiedadBody -UseBasicParsing | Out-Null
} catch { Write-Host "HTTP $([int]$_.Exception.Response.StatusCode)" }

Write-Host "`n[JWT] 201: AGENTE crea una propiedad" -ForegroundColor Green
$propiedad = Invoke-RestMethod -Method Post -Uri "$Gateway/api/v1/propiedades" -Headers @{Authorization="Bearer $agenteToken"} -ContentType "application/json" -Body $propiedadBody
$propiedad | ConvertTo-Json -Depth 5

Write-Host "`n[Feign] CLIENTE crea una reserva. reserva-ms valida la propiedad llamando a propiedad-ms por Eureka." -ForegroundColor Green
$reservaBody = @{
    propiedadId=$propiedad.id
    fechaExpiracion=(Get-Date).AddDays(1).ToString("o")
    montoReserva=1000
    estado="PENDIENTE"
} | ConvertTo-Json
$reserva = Invoke-RestMethod -Method Post -Uri "$Gateway/api/v1/reservas" -Headers @{Authorization="Bearer $clienteToken"} -ContentType "application/json" -Body $reservaBody
$reserva | ConvertTo-Json -Depth 5

Write-Host "`n[Feign] Detalle enriquecido desde propiedad-ms" -ForegroundColor Green
Invoke-RestMethod -Uri "$Gateway/api/v1/reservas/detalle/$($reserva.id)" -Headers @{Authorization="Bearer $clienteToken"} | ConvertTo-Json -Depth 7

Write-Host "`n[CB] Estado actual" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$Gateway/api/v1/reservas/_circuit-breaker" -Headers @{Authorization="Bearer $clienteToken"} | ConvertTo-Json -Depth 5

Write-Host "`nReserva demo ID: $($reserva.id). Para abrir el Circuit Breaker, detén propiedad-ms y ejecuta scripts/abrir-circuit-breaker.ps1 -ReservaId $($reserva.id)" -ForegroundColor Cyan
