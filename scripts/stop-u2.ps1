param([switch]$StopInfrastructure)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
$pidFile=Join-Path $projectRoot '.run/processes.json'
if(Test-Path -LiteralPath $pidFile){
 foreach($entry in @(Get-Content -LiteralPath $pidFile -Raw | ConvertFrom-Json|ForEach-Object {$_})){
  $p=Get-Process -Id $entry.id -ErrorAction SilentlyContinue
  if($p -and $p.ProcessName -eq 'java' -and $p.StartTime.ToUniversalTime() -eq ([datetime]$entry.started).ToUniversalTime()){Stop-Process -Id $p.Id}
 }
 Remove-Item -LiteralPath $pidFile
}
if($StopInfrastructure){Set-Location -LiteralPath $projectRoot;docker compose -f compose-dev.yml stop}
