param([string]$Keycloak='http://localhost:28090')
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$masterPassword=if($env:KC_BOOTSTRAP_ADMIN_PASSWORD){$env:KC_BOOTSTRAP_ADMIN_PASSWORD}else{'admin'}
$token=(Invoke-RestMethod -Method Post "$Keycloak/realms/master/protocol/openid-connect/token" -Body @{client_id='admin-cli';grant_type='password';username='admin';password=$masterPassword}).access_token
$headers=@{Authorization="Bearer $token"}
$realm=Get-Content "$root/infra/keycloak/casa-o-nada-realm.json" -Raw|ConvertFrom-Json
foreach($client in $realm.clients){$existing=@(Invoke-RestMethod "$Keycloak/admin/realms/casa-o-nada/clients?clientId=$($client.clientId)" -Headers $headers);$body=$client|ConvertTo-Json -Depth 20;if($existing.Count){Invoke-RestMethod -Method Put "$Keycloak/admin/realms/casa-o-nada/clients/$($existing[0].id)" -Headers $headers -ContentType 'application/json' -Body $body|Out-Null}else{Invoke-RestMethod -Method Post "$Keycloak/admin/realms/casa-o-nada/clients" -Headers $headers -ContentType 'application/json' -Body $body|Out-Null}}
Write-Host 'Clientes OAuth sincronizados: SPA con PKCE; pruebas locales separadas.'
