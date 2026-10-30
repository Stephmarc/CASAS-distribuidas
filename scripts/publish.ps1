$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $root
$branch=(& git branch --show-current).Trim()
if(-not $branch){throw 'Seleccione una rama antes de publicar.'}
git status --short
git add -A
if($LASTEXITCODE -ne 0){throw 'No se pudieron preparar los cambios.'}
git diff --cached --quiet
if($LASTEXITCODE -eq 1){git commit -m 'Completar CASAS Unidad II: backend, Angular, observabilidad y documentacion';if($LASTEXITCODE -ne 0){throw 'Fallo el commit. Configure su identidad Git.'}}
git push -u origin $branch
if($LASTEXITCODE -ne 0){throw 'No se pudo subir. Inicie sesion con Stephmarc en Git y repita el script.'}
Write-Host "Cambios subidos a $branch. MkDocs se despliega al integrar a main; revise GitHub Actions." -ForegroundColor Green
