param([string]$Gateway='http://localhost:28080',[string]$Keycloak='http://localhost:28090')
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$token=(Invoke-RestMethod -Method Post "$Keycloak/realms/casa-o-nada/protocol/openid-connect/token" -Body @{client_id='casa-o-nada-tests';grant_type='password';username='admin';password='Admin123!'}).access_token
$h=@{Authorization="Bearer $token"}
$null=Invoke-RestMethod "$Gateway/api/v1/clientes/1" -Headers $h -TimeoutSec 25
function Send-Json([string]$Path,$Body){Invoke-RestMethod -Method Post "$Gateway/api/v1/$Path" -Headers $h -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes(($Body|ConvertTo-Json -Depth 8))) -TimeoutSec 25}
$agents=@(Invoke-RestMethod "$Gateway/api/v1/agentes" -Headers $h -TimeoutSec 25 | ForEach-Object {$_})
$agentIds=@()
for($i=0;$i -lt 2;$i++){
 $email="agente.demo.$i@casas.example.invalid"
 $found=$agents|Where-Object {$_.email -eq $email}|Select-Object -First 1
 if(-not $found){$found=Send-Json 'agentes' @{nombres=@('Lucia','Mateo')[$i];apellidos='Equipo CASAS (demo)';email=$email;telefono=@('999000101','999000102')[$i];matricula="CASAS-DEMO-9-$i";estado='ACTIVO'}}
 $agentIds+=([long]$found.id)
}
$existing=@(Invoke-RestMethod "$Gateway/api/v1/propiedades" -Headers $h -TimeoutSec 25 | ForEach-Object {$_})
$items=@(Get-Content -LiteralPath "$root/data/demo-propiedades.json" -Raw -Encoding UTF8|ConvertFrom-Json|ForEach-Object {$_})
$created=0
foreach($item in $items){
 if(@($existing|Where-Object {$_.titulo -eq $item.titulo}).Count){continue}
 $body=@{};$item.PSObject.Properties|Where-Object {$_.Name -ne 'agenteDemo'}|ForEach-Object {$body[$_.Name]=$_.Value}
 $body.agenteId=$agentIds[[int]$item.agenteDemo]
 Send-Json 'propiedades' $body|Out-Null
 $created++
}
$all=@(Invoke-RestMethod "$Gateway/api/v1/propiedades" -Headers $h -TimeoutSec 25 | ForEach-Object {$_})
New-Item -ItemType Directory -Force "$root/.run"|Out-Null
@{fecha=(Get-Date).ToUniversalTime().ToString('o');creadas=$created;total=$all.Count;agentes=$agentIds}|ConvertTo-Json -Depth 6|Set-Content "$root/.run/seed-results.json" -Encoding UTF8
Write-Host "Catalogo preparado: $created inmuebles nuevos, $($all.Count) en total. Fotos ilustrativas y datos ficticios de laboratorio." -ForegroundColor Green
