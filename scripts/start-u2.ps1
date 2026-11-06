param([switch]$SkipBuild)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
. "$PSScriptRoot/common.ps1"
Import-CasaEnvironment $projectRoot
$javaExe=Get-CasaJava
docker info *> $null
if($LASTEXITCODE -ne 0){throw 'Inicie Docker Desktop y espere a que el motor este listo.'}
$modules=@(@{path='infra/casa-config';port=28888},@{path='infra/casa-eureka';port=28761})
$names=@('cliente','propiedad','agente','visita','reserva','orden','pago','contrato','notificacion')
for($i=0;$i -lt $names.Count;$i++){$modules+=@{path="services/$($names[$i])-ms";port=(28101+$i)}}
$modules+=@{path='infra/casa-gateway';port=28080}
foreach($m in $modules){if(Get-NetTCPConnection -LocalPort $m.port -State Listen -ErrorAction SilentlyContinue){throw "Puerto $($m.port) ocupado. Detenga la version anterior con scripts/stop-u2.ps1."}}
if(Test-Path .run/processes.json){foreach($entry in @(Get-Content .run/processes.json -Raw|ConvertFrom-Json|ForEach-Object {$_})){if(Get-Process -Id $entry.id -ErrorAction SilentlyContinue){throw 'Hay procesos registrados del inicio anterior. Ejecute scripts/stop-u2.ps1 antes de iniciar.'}}}
if(-not $SkipBuild){& .\mvnw.cmd -B -ntp clean verify;if($LASTEXITCODE -ne 0){throw 'La compilacion o las pruebas fallaron; no se iniciaron aplicaciones.'}}
docker compose -f compose-dev.yml up -d --wait postgres kafka
if($LASTEXITCODE -ne 0){throw 'PostgreSQL o Kafka no estan listos.'}
docker compose -f compose-dev.yml run --rm db-init
if($LASTEXITCODE -ne 0){throw 'No se pudieron crear las bases de datos.'}
docker compose -f compose-dev.yml up -d mailpit keycloak
if($LASTEXITCODE -ne 0){throw 'Keycloak no pudo iniciar.'}
Wait-CasaHttp 'http://localhost:28090/realms/casa-o-nada/.well-known/openid-configuration'
& "$PSScriptRoot/sync-keycloak.ps1"
New-Item -ItemType Directory -Force -Path .run,.run/logs | Out-Null
$processes=@()
try{
 foreach($m in $modules){
  $name=Split-Path -Leaf $m.path
  $p=Start-CasaProcess $projectRoot $m.path $m.port $javaExe
  $processes+=@{id=$p.Id;name=$name;started=$p.StartTime.ToUniversalTime().ToString('o')}
  Save-CasaProcesses $projectRoot $processes
  try{Wait-CasaHttp "http://localhost:$($m.port)/actuator/health" 180 $p}
  catch{Get-Content ".run/$name.err.log" -Tail 35;Get-Content ".run/$name.out.log" -Tail 35;throw}
  Write-Host "$name listo en $($m.port)" -ForegroundColor Green
 }
 docker compose -f compose-dev.yml up -d kafka-ui prometheus loki alloy grafana
 if($LASTEXITCODE -ne 0){throw 'No se pudo iniciar observabilidad.'}
 Write-Host 'Inicio completo. Espere el registro UP en Eureka y ejecute scripts/test-stability.ps1.' -ForegroundColor Cyan
}catch{
 $failure=$_
 & "$PSScriptRoot/stop-u2.ps1"
 Write-Host 'Se detuvieron solo las aplicaciones de este intento. Los datos e infraestructura se conservan.' -ForegroundColor Yellow
 throw $failure
}
