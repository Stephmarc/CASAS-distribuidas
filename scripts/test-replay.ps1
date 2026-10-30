$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location "$root/validation"
try{npm.cmd ci --no-audit --no-fund;if($LASTEXITCODE -ne 0){throw 'No se pudieron instalar las herramientas de prueba.'};node replay.mjs;if($LASTEXITCODE -ne 0){throw 'Fallo la prueba de reprocesamiento.'}}finally{Pop-Location}
