# Comprobar SQL sin tocar las bases del proyecto

Herramienta opcional de validación para desarrolladores. Requiere Node.js 20 o superior y acceso a npm.

Desde validation:

```powershell
npm ci
node check-sql.mjs
```

Ejecuta las migraciones en bases temporales en memoria mediante PGlite con PostgreSQL 18.6. Comprueba también el orden de outbox y restricciones de exclusividad. No necesita sus credenciales y no se conecta al PostgreSQL de Docker. No sustituye una prueba distribuida de concurrencia con PostgreSQL 16.

## Pruebas operativas

resilience.mjs coordina las comprobaciones HTTP antes, durante y despues de la caida real. Lo invoca scripts/test-resilience.ps1.

replay.mjs consume un evento CREAR_CONTRATO ya procesado, publica exactamente el mismo evento y exige un solo contrato y duplicate=true en el log del consumidor. Lo invoca scripts/test-replay.ps1 despues de test-stability.ps1.

Las variables CASA_GATEWAY, CASA_KEYCLOAK y KAFKA_BOOTSTRAP_SERVERS permiten apuntar estas herramientas a una instalacion de laboratorio propia. No se exportan JWT ni contrasenas en sus reportes.
