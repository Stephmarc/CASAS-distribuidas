param([string]$Gateway='http://localhost:28080')
$ErrorActionPreference='Stop'
. "$PSScriptRoot/test-u2.ps1" -Gateway $Gateway
try {
 $agentToken=Token 'agente' 'Agente123!'
 ExpectStatus 403 {Api GET "/api/v1/pagos/$($payment.id)" $other} 'Otro cliente no lee pagos'
 ExpectStatus 403 {Api GET "/api/v1/contratos/$($contracts[0].id)" $other} 'Otro cliente no lee contratos'
 ExpectStatus 400 {Api PATCH "/api/v1/propiedades/$($case.propiedad.id)/estado" $admin @{}} 'Falta de estado devuelve 400'
 ExpectStatus 400 {Api POST '/api/v1/propiedades' $admin @{titulo='No debe venderse manualmente';descripcion='Prueba';ciudad='Juliaca';direccion='Universidad';precio=100000;habitaciones=3;banos=2;areaM2=100;tipoOperacion='COMPRA';estado='VENDIDA'}} 'Venta manual requiere SAGA'
 ExpectStatus 400 {Api POST '/api/v1/pagos' $client @{ordenId=1;monto=1;moneda='PEN';descripcion='Prueba';payerEmail='c@c.pe';paymentMethodId='visa';installments=1}} 'Pago sin clave de idempotencia devuelve 400'
 ExpectStatus 403 {Api GET '/api/v1/operaciones/eventos' $client} 'Eventos tecnicos requieren administrador'
 $null=Api GET '/api/v1/operaciones/eventos' $admin
 Check $true 'Estado de eventos disponible para administrador'
 $cors=Invoke-WebRequest -UseBasicParsing -Method Options -Uri "$Gateway/api/v1/propiedades" -Headers @{Origin='http://localhost:4300';'Access-Control-Request-Method'='GET';'Access-Control-Request-Headers'='authorization'}
 Check ([string]$cors.Headers['Access-Control-Allow-Origin'] -eq 'http://localhost:4300') 'Preflight CORS permite el frontend configurado'
 $contractId=$contracts[0].id
 $null=Api PATCH "/api/v1/contratos/$contractId/estado" $admin @{estado='PENDIENTE_ACEPTACION'}
 ExpectStatus 403 {Api POST "/api/v1/contratos/$contractId/aceptar" $other} 'Otro cliente no acepta el contrato'
 $accepted=Api POST "/api/v1/contratos/$contractId/aceptar" $client
 Check ($accepted.estado -eq 'ACEPTADO') 'Cliente acepta su contrato de orden confirmada'
 ExpectStatus 409 {Api PATCH "/api/v1/contratos/$contractId/estado" $admin @{estado='GENERADO'}} 'Contrato aceptado no retrocede'
 $suffix=[guid]::NewGuid().ToString('N')
 $agent=Api POST '/api/v1/agentes' $admin @{nombres='Agente';apellidos='Pruebas';email="agente-$suffix@casa.local";telefono='999999999';matricula=$suffix;estado='ACTIVO'}
 $property=Api POST '/api/v1/propiedades' $admin @{titulo="Visitas $suffix";descripcion='Inmueble para pruebas de agenda';ciudad='Juliaca';direccion='Universidad';precio=100000;habitaciones=3;banos=2;areaM2=100;tipoOperacion='COMPRA';estado='DISPONIBLE';agenteId=$agent.id}
 $visitBody=@{propiedadId=$property.id;agenteId=$agent.id;fechaHora=(Get-Date).ToUniversalTime().AddDays(2).ToString('o');observaciones='Prueba de estabilidad';estado='CONFIRMADA'}
 $visit=Api POST '/api/v1/visitas' $client $visitBody
 Check ($visit.estado -eq 'SOLICITADA' -and $visit.clienteId -eq 1) 'Visita inicia solicitada y usa identidad del JWT'
 ExpectStatus 409 {Api POST '/api/v1/visitas' $client $visitBody} 'Agenda evita duplicar el horario'
 ExpectStatus 403 {Api GET "/api/v1/visitas/$($visit.id)" $other} 'Otro cliente no lee la visita'
 $null=Api PATCH "/api/v1/visitas/$($visit.id)/estado" $agentToken @{estado='CONFIRMADA'}
 ExpectStatus 409 {Api PATCH "/api/v1/visitas/$($visit.id)/estado" $admin @{estado='REALIZADA'}} 'No se marca realizada una visita futura'
 $cancelVisit=Api POST "/api/v1/visitas/$($visit.id)/cancelar" $client
 Check ($cancelVisit.estado -eq 'CANCELADA') 'Cliente cancela su visita'
 $visitBody.clienteId=1
 $visitBody.fechaHora=(Get-Date).ToUniversalTime().AddDays(3).ToString('o')
 ExpectStatus 400 {Api PUT "/api/v1/visitas/$($visit.id)" $admin $visitBody} 'Una visita cancelada no se reabre'
 $tracking=Api GET "/api/v1/pagos/$($payment.id)/seguimiento" $client
 Check ($tracking.estado -eq 'APROBADO' -and -not $tracking.pendienteConciliacion) 'Seguimiento del pago disponible para su cliente'
 Write-Host 'Pruebas de estabilidad completadas.' -ForegroundColor Cyan
} catch {
 if($results.Count -eq 0 -or $results[$results.Count-1].status -ne 'FAIL'){$results.Add(@{test=('Estabilidad interrumpida: '+$_.Exception.Message);status='FAIL'})}
 throw
} finally {
 $out=Join-Path (Split-Path -Parent $PSScriptRoot) '.run'
 $results | ConvertTo-Json -Depth 8 | Set-Content "$out/test-stability-results.json" -Encoding UTF8
}
