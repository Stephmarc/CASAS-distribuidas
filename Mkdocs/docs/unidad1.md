# Unidad I · Sistema distribuido base

## S01 · Servicio base

`propiedad-ms` administra inmuebles con DTO, validación, capa de servicio, repositorio y errores HTTP. PostgreSQL persiste precio, operación, estado, dirección, área y características. Flyway conserva el historial de migraciones y Hibernate valida el esquema.

## S02 · Configuración centralizada

`infra/casa-config` sirve los archivos de `config-repo` con el patrón `{aplicación}-dev.yml`. Cada aplicación importa Config Server desde su `application.yml`. Se comprueba con `http://localhost:28888/casa-propiedad-ms/dev`. Las contraseñas se resuelven mediante variables, fuera de Git.

## S03 · Registro y descubrimiento

Eureka está en `http://localhost:28761`. Las instancias anuncian nombre lógico y puerto; Feign y Gateway resuelven esos nombres. La segunda instancia de propiedad usa otro puerto y la misma base del dominio; no crea un segundo dominio.

## S04 · Gateway y balanceo

El cliente utiliza `http://localhost:28080/api/v1`. Las rutas `lb://casa-…-ms` pasan por LoadBalancer y Eureka. La identidad sigue validándose dentro de cada microservicio. El script de segunda instancia permite demostrar distribución sin cambiar la URL del cliente.

## S05 · Integración de la base

Los seis servicios originales son propiedad, agente, visita, reserva, pago y contrato. Unidad II agrega cliente, orden y notificación, además de la infraestructura compartida de eventos. El README anterior anunciaba servicios que aún no estaban en su reactor; ahora código, módulos y documentación coinciden.

Cada dominio tiene su propia base. El Compose de desarrollo utiliza un servidor PostgreSQL para nueve bases separadas; ningún servicio consulta tablas de otro dominio. Una separación física en servidores distintos es una evolución de despliegue, no una transacción compartida.

## Verificación reproducible

Ejecutar `scripts/start-u2.ps1`, comprobar Config Server y Eureka y consultar propiedades por Gateway. `scripts/segunda-instancia-propiedad.ps1` permite la prueba de balanceo. Los scripts no eliminan volúmenes.

## Servicio externo real

El detalle de un inmueble consulta la ciudad por `propiedad-ms` usando la API real de geocodificación de Open-Meteo / GeoNames. Angular solicita esa información únicamente por Gateway y ofrece un enlace al mapa de la ciudad. La consulta tiene timeout y caché de 30 minutos; si falla, la reserva sigue disponible. Las coordenadas son una referencia urbana, no el domicilio del inmueble. La integración se comprobó con Juliaca (Puno, Perú). El adaptador de Mercado Pago conserva su validación externa pendiente.
