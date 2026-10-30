param([string]$Gateway='http://localhost:28080')
$ErrorActionPreference='Stop'
$results=[System.Collections.Generic.List[object]]::new()
function Check([bool]$Condition,[string]$Name){if(-not $Condition){$results.Add(@{test=$Name;status='FAIL'});throw "FALLO: $Name"};$results.Add(@{test=$Name;status='PASS'});Write-Host "PASS: $Name" -ForegroundColor Green}
function Token([string]$User,[string]$Password){(Invoke-RestMethod -Method Post -Uri 'http://localhost:28090/realms/casa-o-nada/protocol/openid-connect/token' -ContentType 'application/x-www-form-urlencoded' -Body @{grant_type='password';client_id='casa-o-nada-tests';username=$User;password=$Password}).access_token}
function Api([string]$Method,[string]$Path,[string]$Jwt,$Body=$null,[string]$Key=''){
 $h=@{};if($Jwt){$h.Authorization="Bearer $Jwt"};if($Key){$h['X-Idempotency-Key']=$Key}
 $requestArgs=@{Method=$Method;Uri="$Gateway$Path";Headers=$h;TimeoutSec=15}
 if($null -ne $Body){$requestArgs.ContentType='application/json';$requestArgs.Body=($Body|ConvertTo-Json -Depth 12)}
 $result=Invoke-RestMethod @requestArgs
 if($result -is [array]){$result | ForEach-Object {$_}}else{$result}
}
function ExpectStatus([int]$Expected,[scriptblock]$Action,[string]$Name){try{& $Action|Out-Null;throw "Se esperaba HTTP $Expected"}catch {if(-not $_.Exception.Response){throw};Check ([int]$_.Exception.Response.StatusCode -eq $Expected) $Name}}
function AwaitState([long]$Id,[string]$Expected){$until=(Get-Date).AddSeconds(100);do{$o=Api GET "/api/v1/ordenes/$Id" $client;if($o.estado -eq $Expected){return $o};if($o.estado -eq 'CANCELADA' -and $Expected -ne 'CANCELADA'){throw "Orden cancelada: $($o.motivo)"};Start-Sleep -Seconds 2}while((Get-Date)-lt $until);throw "Estado esperado $Expected, recibido $($o.estado)"}
function Begin-Order([string]$Type){
 $suffix=[guid]::NewGuid().ToString('N')
 $p=Api POST '/api/v1/propiedades' $admin @{titulo="Demo $suffix";descripcion='Inmueble de prueba U2';ciudad='Juliaca';direccion='Av. Universidad 100';precio=100000;habitaciones=3;banos=2;areaM2=100;tipoOperacion=$Type;estado='DISPONIBLE';agenteId=$null;imagenPrincipalUrl=$null}
 $r=Api POST '/api/v1/reservas' $client @{clienteId=999;propiedadId=$p.id;fechaExpiracion=(Get-Date).ToUniversalTime().AddHours(2).ToString('o');montoReserva=500;estado='PENDIENTE'}
 Check ($r.clienteId -eq 1) 'JWT impide suplantar clienteId'
 $body=@{reservaId=$r.id;moneda='PEN'}
 $o=Api POST '/api/v1/ordenes' $client $body $suffix
 $duplicate=Api POST '/api/v1/ordenes' $client $body $suffix
 Check ($duplicate.id -eq $o.id) 'Orden idempotente'
 $o=AwaitState $o.id 'ESPERANDO_PAGO'
 Check ((Api GET "/api/v1/propiedades/$($p.id)" $client).estado -eq 'RESERVADA') 'Propiedad bloqueada por la SAGA'
 return @{orden=$o;propiedad=$p;reserva=$r}
}
function New-Payment($Case){$key=[guid]::NewGuid().ToString('N');$body=@{ordenId=$Case.orden.id;monto=500;moneda='PEN';descripcion='Adelanto inmobiliario';payerEmail='cliente@casa.local';paymentMethodId='visa';installments=1};$p=Api POST '/api/v1/pagos' $client $body $key;$dup=Api POST '/api/v1/pagos' $client $body $key;Check ($p.id -eq $dup.id) 'Pago idempotente';return $p}
try{
 $admin=Token 'admin' 'Admin123!'
$client=Token 'cliente' 'Cliente123!'
$other=Token 'cliente2' 'Cliente2123!'
 ExpectStatus 401 {Api GET '/api/v1/ordenes' ''} 'Sin JWT devuelve 401'
 ExpectStatus 403 {Api POST '/api/v1/clientes' $client @{id=99;nombres='No autorizado';email='x@x.com';activo=$true}} 'Cliente no administra perfiles'
 $case=Begin-Order 'COMPRA'
 ExpectStatus 403 {Api GET "/api/v1/ordenes/$($case.orden.id)" $other} 'Otro cliente no lee la orden'
 ExpectStatus 403 {Api GET "/api/v1/reservas/$($case.reserva.id)" $other} 'Otro cliente no lee la reserva'
 ExpectStatus 409 {Api POST '/api/v1/pagos' $client @{ordenId=$case.orden.id;monto=1;moneda='PEN';descripcion='Manipulado';payerEmail='cliente@casa.local';paymentMethodId='visa';installments=1} ([guid]::NewGuid().ToString())} 'Monto adulterado devuelve 409'
 $payment=New-Payment $case
 ExpectStatus 403 {Api POST "/api/v1/pagos/$($payment.id)/simular" $client @{estado='APROBADO'}} 'Cliente no puede aprobar su pago'
 $null=Api POST "/api/v1/pagos/$($payment.id)/simular" $admin @{estado='APROBADO'}
 $order=AwaitState $case.orden.id 'CONFIRMADA'
 Check ((Api GET "/api/v1/propiedades/$($case.propiedad.id)" $client).estado -eq 'VENDIDA') 'Compra aprobada vende la propiedad'
 Check ((Api GET "/api/v1/reservas/$($case.reserva.id)" $client).estado -eq 'CONVERTIDA') 'Reserva convertida'
 $contracts=@(Api GET '/api/v1/contratos' $client | Where-Object {$_.ordenId -eq $order.id})
 Check ($contracts.Count -eq 1 -and $contracts[0].monto -eq 100000) 'Un contrato por el valor total del inmueble'
 $null=Api POST "/api/v1/pagos/$($payment.id)/simular" $admin @{estado='APROBADO'}
 Check (@(Api GET '/api/v1/contratos' $client | Where-Object {$_.ordenId -eq $order.id}).Count -eq 1) 'Aprobacion duplicada no duplica contrato'
 $rejected=Begin-Order 'ALQUILER';$rp=New-Payment $rejected
 $null=Api POST "/api/v1/pagos/$($rp.id)/simular" $admin @{estado='RECHAZADO'}
 $null=AwaitState $rejected.orden.id 'CANCELADA'
 Check ((Api GET "/api/v1/propiedades/$($rejected.propiedad.id)" $client).estado -eq 'DISPONIBLE') 'Rechazo libera propiedad'
 Check ((Api GET "/api/v1/reservas/$($rejected.reserva.id)" $client).estado -eq 'CANCELADA') 'Rechazo cancela reserva'
 $cancelled=Begin-Order 'COMPRA';$cp=New-Payment $cancelled
 $null=Api POST "/api/v1/ordenes/$($cancelled.orden.id)/cancelar" $client
 $null=AwaitState $cancelled.orden.id 'CANCELADA'
 Check ((Api GET "/api/v1/pagos/$($cp.id)" $client).estado -eq 'CANCELADO') 'Cancelacion revierte pago pendiente'
 ExpectStatus 409 {Api POST "/api/v1/pagos/$($cp.id)/simular" $admin @{estado='APROBADO'}} 'Pago tardio no resucita saga cancelada'
 $rent=Begin-Order 'ALQUILER';$rpay=New-Payment $rent
 $null=Api POST "/api/v1/pagos/$($rpay.id)/simular" $admin @{estado='APROBADO'}
 $null=AwaitState $rent.orden.id 'CONFIRMADA'
 Check ((Api GET "/api/v1/propiedades/$($rent.propiedad.id)" $client).estado -eq 'ALQUILADA') 'Alquiler aprobado cambia estado correctamente'
 $until=(Get-Date).AddSeconds(30)
 do {$notifications=@(Api GET '/api/v1/notificaciones' $client | Where-Object {$_.ordenId -eq $order.id});if($notifications.Count -gt 0){break};Start-Sleep -Seconds 1}while((Get-Date)-lt $until)
 Check ($notifications.Count -eq 1) 'Notificacion persistida una sola vez'
 Write-Host 'Pruebas U2 completadas.' -ForegroundColor Cyan
}catch{
 if($results.Count -eq 0 -or $results[$results.Count-1].status -ne 'FAIL'){$results.Add(@{test=('Ejecucion interrumpida: '+$_.Exception.Message);status='FAIL'})}
 throw
}finally{
 $out=Join-Path (Split-Path -Parent $PSScriptRoot) '.run'
 New-Item -ItemType Directory -Force -Path $out | Out-Null
 $results | ConvertTo-Json -Depth 8 | Set-Content "$out/test-u2-results.json"
}
