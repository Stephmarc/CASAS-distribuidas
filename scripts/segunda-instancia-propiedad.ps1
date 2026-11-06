$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/common.ps1"
Import-CasaEnvironment $root
$java=Get-CasaJava
if(Get-NetTCPConnection -LocalPort 28112 -State Listen -ErrorAction SilentlyContinue){throw 'Puerto 28112 ocupado.'}
$jar=Get-ChildItem "$root/services/propiedad-ms/target/*.jar"|Where-Object {$_.Name -notlike '*.original'}|Select-Object -First 1
if(-not $jar){throw 'Compile primero el proyecto.'}
$p=Start-Process -FilePath $java -ArgumentList @('-Xms64m','-Xmx192m','-jar',('"'+$jar.FullName+'"'),'--server.port=28112') -WorkingDirectory $root -WindowStyle Hidden -RedirectStandardOutput "$root/.run/propiedad-secondary.out.log" -RedirectStandardError "$root/.run/propiedad-secondary.err.log" -PassThru
$entries=@(Get-Content "$root/.run/processes.json" -Raw|ConvertFrom-Json|ForEach-Object {$_})+@{id=$p.Id;name='propiedad-secondary';started=$p.StartTime.ToUniversalTime().ToString('o')}
Save-CasaProcesses $root $entries
Wait-CasaHttp 'http://localhost:28112/actuator/health' 180 $p
Write-Host 'Segunda instancia registrada en 28112. Consulte el Gateway repetidas veces para observar el balanceo.'
