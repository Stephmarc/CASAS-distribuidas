# Verificación de la versión estabilizada

Fecha de cierre: 16 de octubre de 2026.

## Resultados ejecutados en esta actualización

| Verificación | Resultado real | Alcance |
|---|---|---|
| Maven clean verify | BUILD SUCCESS en los 14 proyectos del reactor | Java 21 y dependencias originales de Spring Boot 4.0.8 y Spring Cloud 2025.1.3; sin adaptadores ni sustitución de versiones. |
| JUnit | 78 pruebas exitosas, 0 fallidas, 0 errores, 0 omitidas | Reglas, identidad, coordinación, permisos de pertenencia, bloqueo de propiedad, agenda, cancelación, estados atrasados y pagos. |
| Proveedor HTTP simulado | 8 pruebas dentro de las 78 | Referencia e idempotencia, conciliación, importes y referencias incompatibles, estado desconocido y errores HTTP. Se usa un servidor HTTP local real, no el servicio de Mercado Pago. |
| SQL | 80 comprobaciones exitosas | Todas las migraciones de los nueve dominios, actualización de colas ya existentes, exclusividad y orden por operación. Motor PostgreSQL 18.6 embebido mediante PGlite; no equivale a una prueba de concurrencia de varios servidores PostgreSQL 16. |
| Migraciones originales | 28 archivos conservados sin cambios | Se añaden V12, V13 o V14 según el dominio; no se reescribe el historial ya aplicado. |
| YAML | 27 archivos validados sin claves duplicadas | Incluye Compose, configuración y CI. |
| PowerShell | Sintaxis validada con PowerShell 7.4.6 | Los scripts de Windows no se ejecutaron contra Docker en este entorno Linux. |

Los resultados detallados se encuentran en docs/evidencias/estabilidad-tests.json, estabilidad-build.txt y estabilidad-sql.txt. La antigua evidencia junit-results.txt pertenece a la entrega anterior.

## Ejecución integrada conocida

La versión anterior fue compilada e iniciada por el equipo en Windows con sus dependencias reales y pasó las 32 comprobaciones HTTP de test-u2.ps1, según la salida compartida en esta sesión. Esa ejecución sirve como base, pero no demuestra por sí misma los cambios nuevos.

La versión estabilizada debe pasar scripts/test-stability.ps1 sobre los contenedores del equipo. El script ejecuta primero las 32 comprobaciones originales y luego amplía pertenencia de pagos y contratos, aceptación de contrato, agenda, estados inválidos, CORS y seguimiento de pagos. Guarda PASS o FAIL; una interrupción también queda registrada.

No se ejecutó aquí el conjunto completo de nueve servicios con Kafka y Keycloak. Tampoco se usaron credenciales reales de Mercado Pago. La compilación y las pruebas de esta versión sí se ejecutaron; las verificaciones HTTP distribuidas nuevas quedan claramente separadas.

## Reproducción

1. Java 21, Docker Desktop y acceso a Maven Central.
2. scripts/start-u2.ps1 compila con clean verify antes de iniciar.
3. Espere los servicios UP en Eureka.
4. scripts/test-stability.ps1 ejecuta la batería HTTP y conserva resultados en .run.
5. Para repetir las comprobaciones SQL aisladas: entre a validation, ejecute npm ci y node check-sql.mjs. Esta herramienta no accede a sus bases de datos.

Las pruebas del proveedor simulado comprueban el comportamiento del código frente a respuestas controladas. La integración financiera externa requiere su prueba con credenciales de Mercado Pago Sandbox antes de utilizarse fuera de la demostración local.
