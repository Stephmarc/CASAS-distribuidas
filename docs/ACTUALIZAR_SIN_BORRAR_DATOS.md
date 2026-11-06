# Actualizar desde la entrega anterior

## Antes de cambiar la carpeta

En la carpeta anterior ejecute scripts/stop-u2.ps1 sin el parámetro StopInfrastructure. Se detienen las aplicaciones y PostgreSQL continúa disponible para el respaldo. Si inició algún JAR manualmente, detenga también esa terminal con Ctrl C.

Extraiga esta actualización en una carpeta nueva. No copie target, .run ni archivos de resultados antiguos. Conserve su .env si lo había personalizado.

Desde la carpeta nueva ejecute:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\scripts\backup-db.ps1
```

El respaldo se guarda en backups con fecha y hora. Espere que se completen los nueve archivos SQL antes de continuar. La configuración Compose mantiene el nombre del proyecto y los volúmenes PostgreSQL y Kafka anteriores.

## Aplicar la actualización

```powershell
.\scripts\start-u2.ps1
```

El script compila y prueba antes de iniciar, verifica los puertos, crea las bases que falten y levanta las aplicaciones. Flyway aplica solo las migraciones nuevas. No ejecute docker compose down -v: ese parámetro elimina volúmenes.

No cambie CASA_DB_USER o CASA_DB_PASSWORD para un volumen existente sin haber actualizado antes las credenciales dentro de PostgreSQL. Cambiar una variable no modifica los usuarios ya creados en esa base.

Si una migración detecta datos anteriores duplicados, se detiene en lugar de borrarlos. Revise esos registros con el respaldo disponible. Las restricciones adicionales protegen horarios de visitas, pagos e identificadores externos.

## Comprobar la versión nueva

```powershell
Start-Process "http://localhost:28761"
.\scripts\test-stability.ps1
Get-Content .\.run\test-stability-results.json
```

El resultado final esperado es Pruebas de estabilidad completadas. Un FAIL conserva su mensaje y detiene la ejecución; no se oculta como una prueba exitosa.

## Operación diaria

```powershell
.\scripts\diagnose.ps1
.\scripts\restart-service.ps1 -Name propiedad-ms
.\scripts\stop-u2.ps1 -StopInfrastructure
```

El reinicio por servicio actualiza el registro de procesos. Así, el script de parada podrá detener también el proceso que se reinició. No se usa Stop-Process para detener todos los procesos Java del equipo.

## Pagos y datos de desarrollo

Por defecto se utiliza SANDBOX_LOCAL. La revisión del pago (aprobar, observar o rechazar con la captura) está habilitada para AGENTE y ADMIN; la simulación directa solo para ADMIN. Para cambiar al proveedor externo, configure MERCADOPAGO_ENABLED=true, PAGOS_SANDBOX_ENABLED=false y su token de prueba. El arranque rechaza tener ambos modos activos o ambos desactivados.

El realm y los usuarios de demostración de Keycloak se mantienen. Esta configuración continúa siendo de desarrollo local; antes de recrear o sustituir un Keycloak personalizado, preserve también sus usuarios y configuración. Los respaldos SQL de la aplicación no incluyen la base interna de Keycloak.
