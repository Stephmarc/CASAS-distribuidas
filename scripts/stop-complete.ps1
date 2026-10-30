param([switch]$StopInfrastructure)
$root=Split-Path -Parent $PSScriptRoot
$file=Join-Path $root '.run/frontend-process.json'
if(Test-Path -LiteralPath $file){$entry=Get-Content -LiteralPath $file -Raw|ConvertFrom-Json;$p=Get-Process -Id $entry.id -ErrorAction SilentlyContinue;if($p -and $p.ProcessName -eq 'node' -and $p.StartTime.ToUniversalTime() -eq ([datetime]$entry.started).ToUniversalTime()){Stop-Process -Id $p.Id};Remove-Item -LiteralPath $file}
& "$PSScriptRoot/stop-u2.ps1" -StopInfrastructure:$StopInfrastructure
