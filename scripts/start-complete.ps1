param([switch]$SkipBuild,[switch]$SkipDemoData)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $root
. "$PSScriptRoot/ensure-node.ps1"
$toolchain=Get-CasaNode $root
$node=$toolchain.Node
& "$PSScriptRoot/start-u2.ps1" -SkipBuild:$SkipBuild
if(-not $SkipDemoData){& "$PSScriptRoot/seed-demo.ps1"}
if(Get-NetTCPConnection -LocalPort 4300 -State Listen -ErrorAction SilentlyContinue){throw 'El puerto 4300 esta ocupado. El backend quedo iniciado; detenga el frontend anterior.'}
Push-Location "$root/frontend"
try {
 & $node $toolchain.Npm ci --no-audit --no-fund
 if($LASTEXITCODE -ne 0){throw 'No se pudieron instalar dependencias frontend.'}
 & $node build.mjs
 if($LASTEXITCODE -ne 0){throw 'Fallo la compilacion frontend.'}
 $p=Start-Process -FilePath $node -ArgumentList @('server.mjs') -WorkingDirectory "$root/frontend" -WindowStyle Hidden -RedirectStandardOutput "$root/.run/frontend.out.log" -RedirectStandardError "$root/.run/frontend.err.log" -PassThru
 @{id=$p.Id;started=$p.StartTime.ToUniversalTime().ToString('o')}|ConvertTo-Json|Set-Content "$root/.run/frontend-process.json"
} finally {Pop-Location}
Write-Host 'CASAS listo: http://localhost:4300 | Grafana: http://localhost:23000' -ForegroundColor Green
Start-Process 'http://localhost:4300'
