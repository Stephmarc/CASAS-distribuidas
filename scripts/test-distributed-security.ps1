param([string]$Keycloak='http://localhost:28090')
$ErrorActionPreference='Stop'
$token=(Invoke-RestMethod -Method Post "$Keycloak/realms/casa-o-nada/protocol/openid-connect/token" -Body @{client_id='casa-o-nada-tests';grant_type='password';username='cliente';password='Cliente123!'}).access_token
$checks=@(@{port=28101;path='clientes'},@{port=28102;path='operaciones/eventos'},@{port=28103;path='operaciones/eventos'},@{port=28104;path='visitas'},@{port=28105;path='reservas'},@{port=28106;path='ordenes'},@{port=28107;path='pagos'},@{port=28108;path='contratos'},@{port=28109;path='notificaciones'})
$results=@()
foreach($c in $checks){$url="http://localhost:$($c.port)/api/v1/$($c.path)";try{Invoke-WebRequest -UseBasicParsing $url|Out-Null;throw 'La ruta protegida acepto una peticion anonima'}catch{if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 401){throw};$results+=@{port=$c.port;withoutToken=401}}
 try {Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:$($c.port)/api/v1/operaciones/eventos" -Headers @{Authorization="Bearer $token"}|Out-Null;throw 'Cliente accedio a ruta ADMIN'}catch{if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 403){throw};$results[-1].clientAdminRoute=403}

}
$results|ConvertTo-Json -Depth 5|Set-Content (Join-Path (Split-Path -Parent $PSScriptRoot) '.run/security-results.json')
Write-Host 'JWT requerido directamente en los nueve microservicios.' -ForegroundColor Green
