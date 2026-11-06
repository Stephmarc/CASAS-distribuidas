# Guía del código: qué tocar cuando el docente pida un cambio

## 1. Cómo está armado

```
Navegador (Angular 22, :4300)
   │  login Keycloak (PKCE) → token JWT
   ▼
Gateway (:28080)  ─ reparte /api/v1/... según infra/casa-config/config-repo/casa-gateway-dev.yml
   │  (encuentra a los servicios por nombre en Eureka :28761; su configuración viene de Config Server :28888)
   ▼
9 microservicios Spring Boot (puertos 28101 a 28109), cada uno con su propia base PostgreSQL
   └── Kafka (tópico casa.operaciones.v1): se avisan entre ellos con eventos (outbox / inbox)
```

| Servicio | Puerto | Qué hace |
|---|---|---|
| cliente-ms | 28101 | Clientes, registro con DNI |
| propiedad-ms | 28102 | Inmuebles + ubicación externa (Open-Meteo) |
| agente-ms | 28103 | Agentes y su acceso |
| visita-ms | 28104 | Visitas |
| reserva-ms | 28105 | Reservas (usa Feign hacia propiedad) |
| orden-ms | 28106 | **Director de la SAGA** |
| pago-ms | 28107 | Pagos con captura y revisión |
| contrato-ms | 28108 | Contratos |
| notificacion-ms | 28109 | Avisos al cliente |

## 2. Dónde está cada cosa

```
common/casa-common/src/main/java/.../shared/   ← código compartido por todos
   Identidad.java (rol, cliente_id del JWT) · Outbox / Inbox / OperacionEvent (Kafka)
   CasaClients + ConsultasRemotas (Feign) · KeycloakAdmin · Mailer · EmailTemplates

services/<nombre>-ms/src/main/java/pe/edu/upeu/casaonada/<nombre>/
   api/        Controller: las URL (@GetMapping, @PostMapping...)
   service/    Lógica de negocio
   domain/     Entidades JPA y enums de estados
   dto/        Lo que entra y sale por la API (Request / Response)
   mapper/     Convierte entidad ↔ DTO
   repository/ Consultas a la base
   config/     SecurityConfig.java = permisos por rol de ESE servicio
   messaging/  Eventos Kafka que envía y recibe
services/<nombre>-ms/src/main/resources/db/migration/   ← scripts SQL de Flyway (V1, V2, ... V14)
services/<nombre>-ms/src/test/                          ← pruebas unitarias

infra/casa-config/config-repo/*.yml    Configuración de cada servicio (puertos, base, Feign, Circuit Breaker)
infra/casa-gateway/.../config/SecurityConfig.java   Permisos por rol EN EL GATEWAY
infra/keycloak/casa-o-nada-realm.json  Usuarios, roles y clientes de Keycloak
compose-dev.yml                        Docker: Postgres, Kafka, Keycloak, Grafana, Mailpit...
observability/                         Prometheus, Loki, Grafana
frontend/src/  core.ts (API, etiquetas, COBRO, adelanto) · store.ts (estado y acciones)
               pages-public.ts (inicio, catálogo, detalle) · pages-client.ts (Mi espacio)
               panel.ts (agente y admin) · modals.ts (todos los diálogos) · app.ts (cabecera)
frontend/styles.css                    Colores y estilos (variables en :root)
scripts/                               Arranque, pruebas y carga de datos demo
```

## 3. Dónde vive cada sesión del curso

| Sesión | Qué es | Dónde mirar |
|---|---|---|
| S01 | Servicio base con DTO, validación y BD | `services/propiedad-ms` completo |
| S02 | Config Server | `infra/casa-config` y `config-repo/*.yml` |
| S03 | Eureka | `infra/casa-eureka` |
| S04 | Gateway y balanceo | `infra/casa-gateway` y `casa-gateway-dev.yml` |
| S06 | Feign + Circuit Breaker | `reserva-ms/.../integration/` y `resilience4j` en `casa-reserva-ms-dev.yml` |
| S07 | JWT / Keycloak y roles | cada `SecurityConfig.java`, gateway y `Identidad.java` |
| S08 | Kafka, outbox, inbox, DLT | `shared/Outbox.java`, `Inbox.java`, `KafkaConfiguration.java`, `*/messaging/` |
| S09 | SAGA | `orden-ms/.../domain/SagaTransitions.java`, `EstadoOrden.java`, `OrdenService.java` |
| S10 | Observabilidad | `observability/` y Grafana :23000 |
| S11 | Angular + servicio externo | `frontend/src/` y `propiedad-ms/.../UbicacionController.java` |

## 4. Si te piden... edita esto

| Pedido | Dónde |
|---|---|
| Agregar un campo al inmueble (ej. «pisos») | `Propiedad.java`, `PropiedadRequest/Response`, `PropiedadMapper`, **nueva** migración `V14__...sql` en propiedad-ms, y el formulario en `frontend/src/modals.ts` (editor de inmueble) |
| Un endpoint nuevo | método en el `Controller` + `Service`, permiso en el `SecurityConfig` del servicio **y** en el del gateway (si la ruta es nueva, también en `casa-gateway-dev.yml`) |
| Cambiar quién puede hacer algo | `SecurityConfig.java` del servicio **y** `infra/casa-gateway/.../SecurityConfig.java` |
| Cambiar el % del adelanto (hoy 1 %) | `frontend/src/core.ts` → `deposit()` |
| Cambiar la duración de la reserva (24 h) | `core.ts` → `RESERVA_HORAS` y `OrdenService.java` (línea del `plusMinutes(... ? 1440 : 2)`) |
| Cambiar un estado o paso de la saga | `SagaTransitions.java`, `EstadoOrden.java`, y la prueba `SagaTransitionsTest.java` |
| Cambiar timeout de Feign o umbral del Circuit Breaker | `config-repo/application-dev.yml` y `casa-reserva-ms-dev.yml` |
| Cambiar el método de pago o datos de cobro (Yape, BCP) | `frontend/src/core.ts` → `COBRO` y la lista `metodos` en `modals.ts`; validación en `PagoService.comprobante()` |
| Cambiar cómo revisa el agente | `PagoService.revisar()` y `panel.ts` (sección pagos) |
| Textos de correos | `shared/EmailTemplates.java` |
| Colores, tipografía, tamaños | `frontend/styles.css` (variables arriba, en `:root`) |
| Nueva pantalla en la web | ruta en `store.ts` (`ROUTES`), componente en `pages-*.ts`, y el `@switch` de `app.ts` |
| Nuevo rol | `casa-o-nada-realm.json` + `SecurityConfig` de cada servicio y del gateway |
| Cambiar los inmuebles de demo | `data/demo-propiedades.json` (luego `scripts/seed-demo.ps1`) |
| Cambiar un puerto | `server.port` en el yml del `config-repo` (los scripts calculan 28101 + posición) |

## 5. Cómo aplicar un cambio rápido

**Solo frontend** (no reinicia nada):
```powershell
cd frontend; node build.mjs
```
y recarga el navegador con Ctrl+F5.

**Un servicio Java** (ejemplo pago-ms): compilar y reiniciar solo ese:
```powershell
.\mvnw.cmd -pl services/pago-ms -am -DskipTests package
.\scripts\restart-service.ps1 -Name pago-ms
```
Si cambiaste un archivo de `config-repo/`, también basta con reiniciar ese servicio. Si cambiaste `casa-common`, recompila y reinicia **todos** (`scripts/start-complete.ps1`).

**Pruebas unitarias de un servicio:**
```powershell
.\mvnw.cmd -pl services/orden-ms -am test
```

## 6. Reglas para no romper nada

1. **Nunca edites una migración ya aplicada** (V1 a V13). Crea una nueva con el siguiente número; Flyway rechaza versiones menores a las ya aplicadas.
2. Los permisos se validan en **dos sitios**: gateway y servicio. Si cambias uno, cambia el otro.
3. Si agregas un campo al DTO, ajusta también el `Mapper`, la entidad y la migración (Hibernate valida el esquema al arrancar).
4. El nombre de los campos JSON es el que usa el frontend (`frontend/src/*.ts`): si cambias uno, cámbialo allá.
5. Después de cambiar lógica de la saga o de pagos, corre sus pruebas unitarias y `scripts/test-stability.ps1`.

## 7. Usuarios de prueba

| Rol | Usuario | Clave |
|---|---|---|
| Cliente | cliente | Cliente123! |
| Agente | agente | Agente123! |
| Administrador | admin | Admin123! |

Web: http://localhost:4300 · Gateway: :28080 · Eureka: :28761 · Grafana: :23000 · Correos (Mailpit): :28025
