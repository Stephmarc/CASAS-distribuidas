param([string]$Destination)
$ErrorActionPreference='Stop'
$OutputEncoding=New-Object System.Text.UTF8Encoding($false)
[Console]::OutputEncoding=$OutputEncoding
$root=Split-Path -Parent $PSScriptRoot
Set-Location $root
. "$PSScriptRoot/common.ps1"
Import-CasaEnvironment $root
if(-not $Destination){$Destination=Join-Path $root ('backups/'+(Get-Date -Format 'yyyyMMdd-HHmmss'))}
New-Item -ItemType Directory -Force -Path $Destination|Out-Null
$userName=if($env:CASA_DB_USER){$env:CASA_DB_USER}else{'casa'}
foreach($db in @('casa_cliente','casa_propiedad','casa_agente','casa_visita','casa_reserva','casa_orden','casa_pago','casa_contrato','casa_notificacion')){
 $file=Join-Path $Destination "$db.sql"
 $dump=& docker compose -f compose-dev.yml exec -T postgres pg_dump -U $userName --no-owner --no-acl $db
 if($LASTEXITCODE -ne 0){throw "Fallo el respaldo de $db"}
 $dump | Set-Content -LiteralPath $file -Encoding UTF8
 Write-Host "Respaldo: $file" -ForegroundColor Green
}
