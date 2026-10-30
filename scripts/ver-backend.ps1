param([ValidateSet('propiedades','reservas','ordenes','pagos','contratos','notificaciones','visitas','agentes','clientes')][string]$Entidad='propiedades')
$ErrorActionPreference='Stop'
$login=Invoke-RestMethod -Method Post 'http://localhost:28090/realms/casa-o-nada/protocol/openid-connect/token' -Body @{client_id='casa-o-nada-tests';grant_type='password';username='admin';password='Admin123!'}
Write-Host "GET http://localhost:28080/api/v1/$Entidad - lectura real por Gateway" -ForegroundColor Cyan
Invoke-RestMethod "http://localhost:28080/api/v1/$Entidad" -Headers @{Authorization="Bearer $($login.access_token)"}|ConvertTo-Json -Depth 8
