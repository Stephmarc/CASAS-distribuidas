param([Parameter(Mandatory=$true)][ValidateSet('cliente-ms','propiedad-ms','agente-ms','visita-ms','reserva-ms','orden-ms','pago-ms','contrato-ms','notificacion-ms','casa-config','casa-eureka','casa-gateway')][string]$Name)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/common.ps1"
Import-CasaEnvironment $root
$javaExe=Get-CasaJava
$entries=@(Get-Content "$root/.run/processes.json" -Raw|ConvertFrom-Json)
$old=$entries|Where-Object {$_.name -eq $Name}
foreach($entry in $old){$p=Get-Process -Id $entry.id -ErrorAction SilentlyContinue;if($p -and $p.ProcessName -eq 'java' -and $p.StartTime.ToUniversalTime() -eq ([datetime]$entry.started).ToUniversalTime()){Stop-Process -Id $p.Id;Wait-Process -Id $p.Id -Timeout 30 -ErrorAction SilentlyContinue}}
$entries=@($entries|Where-Object {$_.name -ne $Name})
Save-CasaProcesses $root $entries
$names=@('cliente-ms','propiedad-ms','agente-ms','visita-ms','reserva-ms','orden-ms','pago-ms','contrato-ms','notificacion-ms')
$port=if($Name -eq 'casa-config'){28888}elseif($Name -eq 'casa-eureka'){28761}elseif($Name -eq 'casa-gateway'){28080}else{28101+[array]::IndexOf($names,$Name)}
if(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue){throw "El puerto $port sigue ocupado por otro proceso."}
$module=if($Name.StartsWith('casa-')){"infra/$Name"}else{"services/$Name"}
$process=Start-CasaProcess $root $module $port $javaExe
$entries+=@{id=$process.Id;name=$Name;started=$process.StartTime.ToUniversalTime().ToString('o')}
Save-CasaProcesses $root $entries
Wait-CasaHttp "http://localhost:$port/actuator/health" 180 $process
Write-Host "$Name reiniciado. Espere su registro en Eureka." -ForegroundColor Green
