param([switch]$PasoAPaso)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$base='http://localhost:28080/api/v1/propiedades'
function Pause-Demo([string]$text){Write-Host $text -ForegroundColor Cyan;if($PasoAPaso){Read-Host 'Enter para continuar'|Out-Null}}
$login=Invoke-RestMethod -Method Post 'http://localhost:28090/realms/casa-o-nada/protocol/openid-connect/token' -Body @{client_id='casa-o-nada-tests';grant_type='password';username='admin';password='Admin123!'}
$headers=@{Authorization="Bearer $($login.access_token)"}
$body=@{titulo="Demo CRUD $(Get-Date -Format 'yyyyMMdd-HHmmss')";descripcion='Vivienda ficticia creada solo para demostrar CRUD.';ciudad='Juliaca';direccion='Direccion ficticia de demostracion';precio=350000;habitaciones=3;banos=2;areaM2=120;tipoOperacion='COMPRA';estado='DISPONIBLE';agenteId=$null;imagenPrincipalUrl='https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=80'}
$steps=@()
Pause-Demo 'CREATE: POST por Gateway. Se crea un registro nuevo exclusivo de esta demo.'
$r=Invoke-WebRequest -UseBasicParsing -Method Post $base -Headers $headers -ContentType 'application/json' -Body ($body|ConvertTo-Json)
if([int]$r.StatusCode -ne 201){throw 'CREATE no devolvio 201'}
$created=$r.Content|ConvertFrom-Json
$steps+=@{operacion='CREATE';method='POST';status=201;id=$created.id;precio=$created.precio}
Write-Host "CREATE 201 - ID $($created.id), precio $($created.precio)"
Pause-Demo 'READ: GET del registro recien creado.'
$read=Invoke-RestMethod "$base/$($created.id)"
if($read.id -ne $created.id){throw 'READ no coincide'}
$steps+=@{operacion='READ';method='GET';status=200;id=$read.id;precio=$read.precio}
Write-Host "READ 200 - $($read.titulo), precio $($read.precio)"
Pause-Demo 'UPDATE: PUT cambia el precio de 350000 a 365000.'
$body.precio=365000
$updated=Invoke-RestMethod -Method Put "$base/$($created.id)" -Headers $headers -ContentType 'application/json' -Body ($body|ConvertTo-Json)
$persisted=Invoke-RestMethod "$base/$($created.id)"
if($updated.precio -ne 365000 -or $persisted.precio -ne 365000){throw 'UPDATE no persistio'}
$steps+=@{operacion='UPDATE';method='PUT';status=200;id=$updated.id;precio=$persisted.precio}
Write-Host "UPDATE 200 - precio persistido $($persisted.precio)"
Pause-Demo 'DELETE: se elimina exclusivamente el registro creado por esta ejecucion.'
$deleted=Invoke-WebRequest -UseBasicParsing -Method Delete "$base/$($created.id)" -Headers $headers
if([int]$deleted.StatusCode -ne 204){throw 'DELETE no devolvio 204'}
$steps+=@{operacion='DELETE';method='DELETE';status=204;id=$created.id}
try{Invoke-WebRequest -UseBasicParsing "$base/$($created.id)"|Out-Null;throw 'El registro sigue presente'}catch{if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 404){throw}}
$steps+=@{operacion='VERIFY_DELETED';method='GET';status=404;id=$created.id}
$report=@{fecha=[datetime]::UtcNow.ToString('o');gateway=$base;registroExclusivoDePrueba=$true;pasos=$steps;resultado='PASS'}
$report|ConvertTo-Json -Depth 8|Set-Content -LiteralPath (Join-Path $root '.run/demo-crud.json') -Encoding utf8
Write-Host 'DELETE 204, GET posterior 404. CRUD COMPLETO PASS. No se tocaron otras viviendas.' -ForegroundColor Green
