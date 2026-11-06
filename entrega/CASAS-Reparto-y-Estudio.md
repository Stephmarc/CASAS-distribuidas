# Reparto de la exposición y plan de estudio · Jannys y Alanguia

El docente dijo que en clase se editará el código. Por eso cada uno debe saber **explicar** su parte y **modificarla en vivo**. Este documento reparte la exposición de forma pareja y dice qué código estudiar. Los nombres vienen del guion del equipo; el grupo expone con sus dos integrantes. El reparto organiza la exposición; no atribuye quién programó cada archivo.

## 1. Criterio de justicia

- **Mismo tiempo:** 3 minutos de demo y 3 de preguntas para cada uno, más las partes grupales divididas a la mitad.
- **Misma carga:** cada uno tiene 2 backends que dominar, una parte de infraestructura o frontend y 5 ejercicios de edición.
- **Mismo piso común:** los dos deben poder contar la operación completa (reserva → pago → contrato) y la saga en dos minutos. Así ninguna pregunta deja a uno sin respuesta.
- **Intercambio:** cada uno estudia 1 archivo clave del otro (sección 5).

## 2. Quién cubre qué

| | **Jannys · producto y negocio** | **Alanguia · infraestructura y consistencia** |
|---|---|---|
| Sesiones | S01 servicio base, S05 integración, S11 Angular y servicio externo, S10 Observabilidad (panel de Grafana), S07 (roles y token en la web) | S02 Config, S03 Eureka, S04 Gateway, S06 Feign y Circuit Breaker, S08 Kafka, S09 SAGA, S07 (validación del JWT en servidores) |
| Servicios | propiedad, reserva, pago, contrato, cliente | orden, notificación, visita, agente + infraestructura (config, eureka, gateway) |
| Código de la web | `frontend/src/` completo | solo leer `core.ts` (cómo se llama a la API) |
| Demo (3 min) | Registro, reserva, pago con captura, aprobación del agente, contrato; panel del admin y el trace de esa operación en Grafana | Eureka, caída real de propiedad y Circuit Breaker, evento duplicado en Kafka y compensación |

Nota de equilibrio: Alanguia tiene más sesiones, pero cada una se demuestra en pocos segundos; Jannys tiene las demos más largas y más código nuevo. En conjunto la carga es pareja.

## 3. Tiempos (18 a 21 minutos)

| Bloque | Tiempo | Quién |
|---|---|---|
| Alcance: qué resuelve CASAS | 2–3 min | Jannys |
| Arquitectura: gateway, Keycloak, Eureka, Kafka, saga | 2–3 min | Alanguia |
| Tareas: S01–S05 y S11 | 1–1,5 min | Jannys |
| Tareas: S06–S10 | 1–1,5 min | Alanguia |
| Demo de Jannys | 3 min | Jannys |
| Preguntas a Jannys | 3 min | Jannys |
| Demo de Alanguia | 3 min | Alanguia |
| Preguntas a Alanguia | 3 min | Alanguia |
| Cierre | 1 min | Los dos |

## 4. Guion de las dos demos

**Jannys (3 min):**
1. Inicio y catálogo: filtros y un inmueble con la ubicación real (servicio externo).
2. «Crear cuenta» con DNI → reservar → responder si quiere visita → confirmar. Mostrar el correo en Mailpit (`:28025`).
3. «Pagar adelanto» con Yape y la captura → entrar como `agente` → Pagos → ver captura → aprobar.
4. Volver al cliente: operación confirmada; el agente envía el contrato, el cliente lo acepta.
5. Entrar como `admin`: resumen, crear un agente, desactivar un cliente.
6. Grafana (`:23000`): tomar el trace de la reserva recién creada y ver que recorre varios servicios.

**Alanguia (3 min):**
1. Eureka (`:28761`): los servicios registrados y Gateway como única entrada.
2. `scripts/test-resilience.ps1`: se detiene propiedad, el circuito pasa a OPEN y se recupera a CLOSED.
3. Kafka UI (`:28085`) y `test-replay.ps1`: el mismo evento reenviado produce un solo contrato.
4. Un rechazo de pago: la saga cancela el contrato, libera el inmueble y cancela la reserva.

## 5. Código que debe dominar cada uno

### Jannys

| Archivo | Qué debes poder decir |
|---|---|
| `propiedad-ms/.../PropiedadController`, `PropiedadService`, `PropiedadRequest`, `PropiedadMapper` | Controller recibe, Service decide, DTO valida (`@NotBlank`, `@Positive`), Mapper convierte; Hibernate valida el esquema contra las migraciones de Flyway |
| `propiedad-ms/.../UbicacionController` | Llama a la API real de Open-Meteo, con timeout, caché de 30 min y respuesta controlada si falla |
| `pago-ms/.../PagoService.comprobante()` y `revisar()` | El cliente sube la captura (hasta 3 MB, PNG/JPG/WEBP) que se guarda en `pago_comprobantes`; el agente aprueba, observa (el pago sigue pendiente) o rechaza (cancela la operación); no se puede aprobar sin captura |
| `contrato-ms/.../ContratoService.cambiarEstado()` | Estados GENERADO → PENDIENTE_ACEPTACION → ACEPTADO → VIGENTE → FINALIZADO y quién puede avanzar cada uno |
| `cliente-ms/.../RegistroController` | Crea el usuario en Keycloak con rol CLIENTE y `cliente_id`; si falla la base, borra el usuario |
| `frontend/src/store.ts` y `core.ts` | Estado con signals, llamadas con el token, cálculo de los pasos de cada operación (`build()`), adelanto (`deposit()`) |
| Intercambio: `orden-ms/.../SagaTransitions.java` | Tabla de estados de la saga |

### Alanguia

| Archivo | Qué debes poder decir |
|---|---|
| `infra/casa-config/config-repo/*.yml` | Un archivo por servicio (`{app}-dev.yml`); cada servicio los importa al arrancar; prueba en `:28888/casa-propiedad-ms/dev` |
| `infra/casa-gateway/.../SecurityConfig` y `casa-gateway-dev.yml` | Rutas `lb://casa-...-ms` por Eureka; los permisos por rol se repiten en cada servicio |
| `reserva-ms/.../PropiedadIntegrationService` y `PropiedadClient` | Feign con timeout y reintentos solo de lectura; Circuit Breaker CLOSED → OPEN → HALF_OPEN; en lectura devuelve datos locales con `degradado`; en escritura devuelve 503 y nunca inventa datos |
| `common/casa-common/.../Outbox`, `Inbox`, `OperacionEvent`, `KafkaConfiguration` | Evento y cambio de datos en la misma transacción; el publicador espera el ACK; inbox con `eventId` único evita duplicados; tres reintentos y luego DLT; tópico `casa.operaciones.v1` con 3 particiones y clave `sagaId` |
| `orden-ms/.../SagaTransitions`, `EstadoOrden`, `OrdenService`, `OrdenEvents` | La orden orquesta: reserva, bloqueo, pago, contrato, propiedad, conversión; plazos de 2 min en pasos técnicos y 24 h esperando pago; compensación en orden inverso |
| `common/casa-common/.../Identidad` y cada `SecurityConfig` | Cada servicio verifica la firma del JWT con JWK y el emisor; roles desde `realm_access.roles`; `cliente_id` impide ver datos de otro cliente |
| Intercambio: `pago-ms/.../PagoService.revisar()` | Qué evento emite cada decisión del agente y cómo afecta a la saga |

## 6. Ejercicios de edición para practicar (como pedirá el docente)

Cada ejercicio se practica con los comandos de la sección 7 de `GUIA-DEL-CODIGO.md`.

**Jannys**
1. Cambiar el adelanto de 1 % a 2 %: `frontend/src/core.ts`, función `deposit()`.
2. Agregar el campo «pisos» al inmueble: entidad, DTOs, mapper, migración `V14` en propiedad-ms y el formulario en `modals.ts`.
3. Agregar un método de pago «Efectivo»: lista `metodos` en `modals.ts` y el `Set` de métodos en `PagoService.comprobante()`.
4. Cambiar los colores de la web: variables de `:root` en `frontend/styles.css`.
5. Agregar un estado de visita nuevo y su etiqueta: `EstadoVisita.java` y `labelVisita` en `core.ts`.

**Alanguia**
1. Cambiar el timeout de Feign o el umbral del Circuit Breaker: `config-repo/casa-reserva-ms-dev.yml`; reiniciar reserva y repetir `test-resilience.ps1`.
2. Cambiar el plazo de espera de pago de 24 h a 1 h: `OrdenService.java` (`plusMinutes(... ? 1440 : 2)`).
3. Dar permiso a un rol en una ruta: `SecurityConfig` del servicio y del gateway.
4. Cambiar los reintentos antes de DLT (hoy 3, uno por segundo): `KafkaConfiguration.java`, línea `new FixedBackOff(1000L, 3)`.
5. Agregar una ruta nueva en el gateway: `casa-gateway-dev.yml` y `SecurityConfig` del gateway.

## 7. Plan de estudio sugerido (3 sesiones)

1. **Entender:** cada uno lee solo la tabla de la sección 5 y explica en voz alta cada archivo, sin mirar. Anota lo que no sepa.
2. **Tocar:** hacer los 5 ejercicios de la sección 6 con el sistema encendido. Un ejercicio termina cuando el cambio se ve funcionando.
3. **Ensayar:** simular la exposición completa con cronómetro. Después, un compañero elige un archivo al azar del otro y lo pregunta.

## 8. Pendientes honestos antes de exponer

- **El PPT** de `entrega/` sigue mostrando el pago anterior (el administrador aprobaba). Hay que actualizar esa diapositiva y la de la demo.
- **Angular 22:** el proyecto usa la versión 22.0.0, pero sin Angular CLI, Router ni HttpClient. Confirmar con el docente si exige alguno.
- **Mercado Pago real:** no se probó; solo el modo manual y las pruebas con proveedor simulado.
- **Consulta de DNI y correo real:** necesitan el token de apis.net.pe y un SMTP en `.env`. Sin ellos, la web lo avisa y sigue funcionando.
- **Declarar solo lo que cada uno hizo:** el historial de git solo muestra dos autores (Stephmarc y Grace). Cada uno debe poder decir con honestidad qué aportó.
