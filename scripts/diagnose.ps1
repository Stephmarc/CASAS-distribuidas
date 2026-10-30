$ErrorActionPreference='Continue'
$root=Split-Path -Parent $PSScriptRoot
Set-Location $root
java --version
docker compose -f compose-dev.yml ps
$ports=@(28888,28761,28101,28102,28103,28104,28105,28106,28107,28108,28109,28080)
foreach($port in $ports){try{$r=Invoke-RestMethod -Uri "http://localhost:$port/actuator/health" -TimeoutSec 4;[pscustomobject]@{Port=$port;Status=$r.status}}catch{[pscustomobject]@{Port=$port;Status='NO_RESPONDE'}}}
