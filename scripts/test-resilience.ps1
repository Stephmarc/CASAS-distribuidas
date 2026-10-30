$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
node "$root/validation/resilience.mjs" prepare
if($LASTEXITCODE -ne 0){throw 'No se pudo preparar el caso.'}
$entry=@(Get-Content "$root/.run/processes.json" -Raw|ConvertFrom-Json)|Where-Object {$_.name -eq 'propiedad-ms'}|Select-Object -First 1
if(-not $entry){throw 'Inicie el proyecto con start-complete.ps1 para identificar su proceso.'}
$p=Get-Process -Id $entry.id -ErrorAction Stop
if($p.ProcessName -ne 'java' -or $p.StartTime.ToUniversalTime() -ne ([datetime]$entry.started).ToUniversalTime()){throw 'El PID no coincide con el proceso registrado de este proyecto.'}
$failure=$null
try{Stop-Process -Id $p.Id;Wait-Process -Id $p.Id -Timeout 30 -ErrorAction SilentlyContinue;node "$root/validation/resilience.mjs" failure;if($LASTEXITCODE -ne 0){throw 'Fallo la comprobacion del fallback.'}}catch{$failure=$_}finally{& "$PSScriptRoot/restart-service.ps1" -Name propiedad-ms}
if($failure){throw $failure}
node "$root/validation/resilience.mjs" recover
if($LASTEXITCODE -ne 0){throw 'La recuperacion no quedo verificada.'}
Write-Host 'PASS: caida real, fallback, OPEN, HALF_OPEN y recuperacion CLOSED.' -ForegroundColor Green
