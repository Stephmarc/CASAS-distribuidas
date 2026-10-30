# Evidencias de la entrega

Pruebas ejecutadas entre el 29 y el 30 de octubre de 2026 (hora de Lima). Los archivos JSON conservan marcas de tiempo ISO en UTC; por eso algunas corresponden al día 30 mientras la hora local aún era del día 29.

## Validación final con Docker (30 de octubre)

Se recuperó Docker Desktop conservando volúmenes, se arrancó el Compose propio y se repitieron las pruebas sobre los puertos definitivos de CASAS.

- **Maven normal: BUILD SUCCESS**, trece módulos Java y 78 pruebas sin fallos. [Salida completa](assets/maven-verify.txt) y [resumen JUnit](assets/maven-verify.json).
- **50 comprobaciones HTTP PASS** y seguridad directa en los nueve servicios. Los JSON de la tabla siguiente corresponden a esta última ejecución.
- Caída real de propiedad y recuperación CLOSED → OPEN → HALF_OPEN → CLOSED.
- Reenvío del mismo evento Kafka con un solo contrato y `duplicate=true` registrado por el consumidor.
- **Compose validado**, PostgreSQL y Kafka saludables; Keycloak, Kafka UI, Prometheus, Loki, Alloy y Grafana iniciados.
- [Observabilidad Docker](assets/observabilidad-docker.json): diez aplicaciones disponibles, once archivos de logs recogidos por Alloy y un mismo trace en siete servicios.
- Se cargaron nueve inmuebles ficticios con fotografías ilustrativas y dos agentes. La carga repetida no duplicó registros; el inicio completo ahora la ejecuta automáticamente (opción `-SkipDemoData` para omitirla).

![Catálogo real poblado](assets/CASAS-Catalogo-Actual.jpg)

## Resultados

| Comprobación | Resultado | Archivo |
|---|---|---|
| Java / JUnit | 78 pruebas correctas; cero fallos | [Resumen](assets/resultados-unitarios.json) |
| HTTP de negocio y estabilidad | 50 comprobaciones PASS | [Resultados HTTP](assets/resultados-http.json) |
| JWT directo a servicios | 9 servicios; 401 sin token y 403 para cliente en ruta ADMIN | [Seguridad](assets/seguridad-servicios.json) |
| Caída real de propiedad | CLOSED → OPEN → HALF_OPEN → CLOSED | [Resiliencia](assets/resiliencia-real.json) |
| Reproceso del mismo evento Kafka | Duplicado reconocido; un contrato antes y después | [Kafka](assets/reprocesamiento-kafka.json) |
| Migraciones y restricciones SQL | 80 comprobaciones correctas con PostgreSQL embebido | `validation/check-sql.mjs` |
| Catálogo y filtros opcionales | Cuatro consultas HTTP 200, incluido listado sin filtros | [Regresión](assets/catalogo-geocodificacion.json) |
| Servicio externo real | Geocodificación de Juliaca, Puno, Perú | [Respuesta](assets/catalogo-geocodificacion.json) |
| Angular y PKCE | Login real, reserva, pago simulado y orden #9 CONFIRMADA | [Flujo web](assets/flujo-frontend.json) |
| Prometheus, Loki y Grafana | Métricas de diez aplicaciones y logs de once | [Observabilidad](assets/observabilidad.json) |

Después del ajuste de búsqueda y del reintento de lectura se volvieron a compilar propiedad y reserva y se repitieron sus siete pruebas unitarias, sin fallos. La caída real se volvió a verificar con esa configuración.

## Cliente Angular

La portada adapta el prototipo aportado por el equipo. La ilustración es propia del proyecto; no representa una fotografía ni acredita un inmueble real.

![Portada CASAS](assets/CASAS-Inicio.jpg)

La operación #9 nació desde el cliente web y el administrador aprobó el adelanto en el simulador local. El cliente recibió la confirmación por consulta periódica de la API.

![Operación confirmada](assets/CASAS-Operacion.jpg)

## Diagnóstico de varios servicios

El tablero consulta Prometheus y Loki reales. La disponibilidad actual se combina con el historial de caídas provocadas y la latencia observada; no se ocultan los fallos de prueba.

![Tablero Grafana](assets/CASAS-Grafana.jpg)

La misma operación conserva el trace `a3f922ae-69a2-450b-9785-cb5e1bc6b94f` en llamadas HTTP y eventos. El filtro de logs permite seguir reserva, orden, pago y contrato.

![Logs correlacionados](assets/CASAS-Trazabilidad.jpg)

## Procedencia y límites

El ZIP de backend estabilizado y los prototipos fueron aportados por el equipo. El Git local tenía una versión anterior de seis servicios; se integraron los nueve dominios del ZIP, se añadieron frontend y observabilidad y se corrigió una consulta que enviaba parámetros nulos ambiguos a PostgreSQL.

Las pruebas operativas utilizaron bases exclusivas `casas_u2_*`, un realm `casa-o-nada` y tópicos `casa.*`. No se utilizaron entidades ni tablas de la tarea personal. Para estas comprobaciones se reutilizó el motor local disponible de PostgreSQL/Kafka/Keycloak con configuración temporal, sin convertirlo en la configuración de despliegue del repositorio.

La primera revisión utilizó binarios oficiales y bases de validación separadas. En la validación final se obtuvo acceso al contexto normal de Windows y se ejecutó **el Compose real y Maven verify**; las limitaciones anteriores de esas dos comprobaciones quedaron resueltas. Las capturas iniciales se conservan como antecedentes, y la nueva captura de catálogo corresponde al despliegue final con fotografías.

Docker Desktop presentó un fallo de inicio de sockets temporales. Se preservaron sus directorios de runtime y se recrearon vacíos; no se hizo factory reset ni se borraron volúmenes. Tras recuperar el motor se comprobaron de nuevo PostgreSQL, Kafka, autenticación y el flujo de negocio.

Las capturas son vistas del navegador integrado. No incluyen reloj ni barra de escritorio; los JSON y logs conservan las marcas de ejecución. Las imágenes anteriores del repositorio son antecedentes, no resultados nuevos.

## Pendientes de la presentación

- El equipo debe grabar su video pitch, sustentar y confirmar su aporte real.
- El proveedor final de identidad es Keycloak; no se afirma haber construido un emisor JWT propio.
- Mercado Pago real requiere credenciales y validación del equipo. Los pagos demostrados son simulados; la geocodificación sí consume un servicio externo real.
- Los seis topics base están configurados. Falta confirmar y añadir la sección académica real antes de entregar.
- La publicación remota se comprueba mediante el estado del workflow de Pages; una captura local por sí sola no la demuestra.

## Publicación comprobada

Código subido a `main` (commit de implementación `01060c7`). Backend y Angular terminaron en verde en GitHub Actions. La primera publicación fue bloqueada por una política antigua que solo permitía `gh-pages`; se autorizó también `main` y se cambió Pages a la fuente GitHub Actions. El despliegue posterior terminó correctamente y la página pública respondió HTTP 200 con estas evidencias.

- [Backend aprobado](https://github.com/Stephmarc/CASAS-distribuidas/actions/runs/41046849489).
- [Angular aprobado](https://github.com/Stephmarc/CASAS-distribuidas/actions/runs/41046849797).
- [MkDocs publicado](https://github.com/Stephmarc/CASAS-distribuidas/actions/runs/41047148163).
- [Documentación pública](https://stephmarc.github.io/CASAS-distribuidas/).

## Capturas de los requisitos S12 · ejecución final

Las siguientes capturas proceden del Compose de CASAS y la aplicación en sus puertos definitivos el 30/10/2026. Se acompañan de los reportes de las pruebas; una captura por sí sola no reemplaza la demo en vivo.

### Frontend por rol y estados de negocio

Sesión real CLIENTE: operaciones confirmadas y canceladas. La orden 2 corresponde a rechazo de pago en el simulador, comprobado por la prueba integral y los eventos de compensación.

![Cliente y operaciones reales](assets/CASAS-Cliente-Operaciones.jpg)

![Cancelación por pago rechazado](assets/CASAS-Compensacion-Pago.jpg)

### Resiliencia observada

Durante `test-resilience.ps1`, propiedad-ms cae realmente: el panel muestra 0 para ese servicio mientras los restantes siguen disponibles. Después se recupera; el reporte verifica CLOSED → OPEN → HALF_OPEN → CLOSED.

![Caída real de propiedad](assets/CASAS-Grafana-Fallo.jpg)

![Recuperación de disponibilidad](assets/CASAS-Grafana-Recuperado.jpg)

### Evento y reprocesamiento

Kafka UI muestra CREAR_CONTRATO reenviado con el mismo eventId `ab4dfc2b-0ae7-423f-9393-e75fb0ece522`. El [reporte del consumidor](assets/reprocesamiento-kafka.json) comprueba un contrato antes y después y `duplicate=true`.

![Evento real de Kafka](assets/CASAS-Kafka-Evento.jpg)

### Observabilidad correlacionada

Grafana consulta Loki con el trace `07da5641-e729-402d-a640-14a64108827e`. Se ven publicación y consumo de eventos en servicios distintos. El filtro usa `{job="casas"}` y conserva el rango horario del caso.

![Misma operación entre servicios](assets/CASAS-Grafana-Trace-Actual.jpg)

### Backend iniciado

Eureka registra nueve microservicios y Gateway en UP. El aviso de self-preservation desactivado corresponde a la configuración local de laboratorio, no a una política recomendada de producción.

![Backend real en Eureka](assets/CASAS-Backend-Eureka.jpg)

### Publicación de una vivienda con fotografía

Administrador y agente disponen del campo de URL HTTPS, vista previa y validación. Se comprobó que HTTP no habilita la publicación y que una imagen HTTPS válida persiste y aparece en el detalle. No se ha implementado almacenamiento de archivos subidos desde el equipo.

![Vista previa en Gestión](assets/CASAS-Admin-Imagen.jpg)

![Vivienda guardada con imagen](assets/CASAS-Vivienda-Publicada.jpg)

### Seguridad directa: resultados ejecutados

Esta tabla se genera a partir del JSON de la prueba real, no de estados escritos manualmente. El acceso permitido y el flujo por rol se verifican además en las 50 comprobaciones HTTP.

| Puerto | Sin token | CLIENTE en ruta ADMIN |
|---|---:|---:|
| 28101 | 401 | 403 |
| 28102 | 401 | 403 |
| 28103 | 401 | 403 |
| 28104 | 401 | 403 |
| 28105 | 401 | 403 |
| 28106 | 401 | 403 |
| 28107 | 401 | 403 |
| 28108 | 401 | 403 |
| 28109 | 401 | 403 |

[JSON original de seguridad](assets/seguridad-servicios.json) · [salida real de Maven](assets/maven-verify.txt). Las salidas son archivos reales de ejecución; no se presentan como capturas de una ventana CMD que no se haya fotografiado.
