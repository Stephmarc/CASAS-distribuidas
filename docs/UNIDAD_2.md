> Actualización: consulte CAMBIOS_ESTABILIDAD.md y VERIFICACION.md. Esta descripción corresponde a la base de Unidad II; la estabilización añade intención de pago persistente y recuperación por referencia.

# Implementación de Unidad II

## Estructura y continuidad

Se mantienen los seis servicios existentes, sus paquetes `pe.edu.upeu.casaonada`, DTO, PostgreSQL, Config Server, Eureka, Gateway y Keycloak. Se agregan cliente, orden y notificación, ya previstos por la arquitectura del proyecto. `common/casa-common` contiene únicamente infraestructura propia: clientes Feign, contexto de identidad, eventos y outbox/inbox.

Los servicios siguen separados por dominio y base de datos. No hay entidades JPA compartidas ni consultas SQL entre bases.

## Feign y Circuit Breaker

Reserva consulta propiedad. Orden consulta reserva, cliente y propiedad. Pago y contrato consultan orden. Los clientes usan nombres lógicos Eureka y LoadBalancer. Se propagan Authorization y X-Trace-ID. Las consultas tienen timeouts y circuit breakers; los 4xx no se contabilizan como indisponibilidad. Un fallo al validar una operación devuelve 503 y no inventa datos. La consulta enriquecida de reserva conserva su respuesta degradada.

Para observar el circuito, use el endpoint autenticado `/api/v1/reservas/_circuit-breaker`. Detenga solo propiedad-ms, consulte el detalle de una reserva varias veces, observe OPEN, vuelva a iniciar propiedad-ms y consulte tras la ventana de recuperación. El fallback de lectura devuelve los datos locales de reserva e indica `degradado`. No se utiliza ese fallback para aprobar una escritura.

## JWT y pertenencia

Keycloak emite JWT. Gateway y cada servicio verifican firma mediante JWK y el emisor configurado. Los roles se obtienen de realm_access.roles. No existe un interruptor que abra toda la API sin autenticación.

CLIENTE usa su claim cliente_id para crear y consultar sus operaciones; no puede elegir otro cliente enviando un ID en el cuerpo. ADMIN administra perfiles, simulación y operaciones técnicas. AGENTE consulta operaciones y mantiene el catálogo según los permisos de la API. El acceso por roles se complementa con controles de pertenencia en visitas, reservas, órdenes, pagos, contratos y notificaciones.

Gateway no sustituye la validación en los servicios: acceder a un puerto interno requiere el mismo JWT. Las interfaces de documentación y health son públicas en DEV; los endpoints técnicos y de recuperación requieren ADMIN.

## S08: mensajería

Tópico: `casa.operaciones.v1`; DLT: `casa.operaciones.v1.DLT`. Ambos tienen tres particiones en el broker local. Cada servicio tiene un grupo independiente; la clave es sagaId para conservar la afinidad de la operación. Los servicios filtran los tipos que les corresponden.

El evento contiene eventId, sagaId, type, ordenId, reservaId, propiedadId, clienteId, monto del adelanto, total del inmueble, moneda, tipo de operación, traceId y motivo. No contiene contraseñas, JWT ni tokens de tarjeta.

La entidad y el evento saliente se escriben en una misma transacción PostgreSQL. El publicador de outbox espera confirmación del broker y marca publicado después; si no puede publicar, el evento permanece pendiente. Un fallo después de publicar y antes de confirmar en PostgreSQL puede repetir el mensaje: inbox impide aplicar dos veces el mismo eventId.

El consumidor aplica inbox, cambios de dominio y respuesta outbox en la misma transacción local. Si falla, se revierte todo y Kafka reintenta. Después de tres reintentos, se publica en DLT. Las compensaciones atascadas se reenvían periódicamente y permanecen visibles hasta recibir su resultado; no se declara una cancelación completada por un simple timeout.

## SAGA orquestada

| Etapa de orden | Comando o acción | Resultado esperado |
|---|---|---|
| VALIDANDO_RESERVA | ASOCIAR_RESERVA | RESERVA_ASOCIADA |
| BLOQUEANDO | BLOQUEAR_PROPIEDAD | PROPIEDAD_BLOQUEADA |
| ESPERANDO_PAGO | Cliente crea pago; proveedor o simulador lo resuelve | PAGO_APROBADO |
| GENERANDO_CONTRATO | CREAR_CONTRATO | CONTRATO_GENERADO |
| CONFIRMANDO_PROPIEDAD | CONFIRMAR_PROPIEDAD | PROPIEDAD_CONFIRMADA |
| CONVIRTIENDO_RESERVA | CONVERTIR_RESERVA | RESERVA_CONVERTIDA |
| CONFIRMADA | ORDEN_CONFIRMADA | Aviso persistente |

La orden espera dos minutos en etapas técnicas y treinta en espera de pago. El planificador comprueba los plazos cada treinta segundos. El plazo de una orden nunca excede la expiración original de la reserva; las reservas vencidas sin orden pasan a EXPIRADA y liberan su exclusividad. El estado y deadline están en la base, por lo que un reinicio no elimina la operación.

Ante rechazo, cancelación o plazo agotado:

1. CANCELAR_CONTRATO → CONTRATO_CANCELADO.
2. REEMBOLSAR_PAGO → PAGO_REEMBOLSADO. Si estaba pendiente, se cancela; si fue aprobado, se reembolsa; si nunca existió, la compensación confirma que no hay cobro que revertir.
3. LIBERAR_PROPIEDAD → PROPIEDAD_LIBERADA.
4. CANCELAR_RESERVA → RESERVA_CANCELADA.
5. Orden CANCELADA y aviso ORDEN_CANCELADA.

Los participantes guardan cercos de cancelación para rechazar comandos tardíos. Los bloqueos usan sagaId: una saga no libera el inmueble reservado por otra. Los locks de fila serializan la concurrencia y los índices únicos sostienen las reglas ante dos peticiones simultáneas. Una orden confirmada no se cancela con este flujo; una devolución posterior requiere otro proceso de negocio.

## Pagos y proveedor externo

DEV habilita `SANDBOX_LOCAL`: POST /pagos crea PENDIENTE y solo ADMIN puede usar POST /pagos/{id}/simular con APROBADO o RECHAZADO. La simulación no modifica pagos de Mercado Pago.

Para probar la integración externa, utilice credenciales de prueba de Mercado Pago y configure `MERCADOPAGO_ENABLED=true`, `PAGOS_SANDBOX_ENABLED=false` y `MERCADOPAGO_ACCESS_TOKEN`. El código conserva Payments API: crea el pago con clave de idempotencia, consulta resultados pendientes cada treinta segundos y utiliza cancelación/reembolso del proveedor para compensar. No marca reembolsado si el proveedor no lo confirma. Esta integración externa no se ejecutó en el entorno de entrega.

La llamada a un proveedor externo no forma una transacción atómica con PostgreSQL. La versión estabilizada guarda primero una intención en una transacción independiente y marca el inicio del envío antes de contactar al proveedor. Si se pierde la respuesta, busca el pago por su referencia y verifica importe y moneda. Mientras el resultado no pueda establecerse, mantiene la compensación pendiente y no vuelve a cobrar automáticamente. La respuesta de creación y el endpoint de seguimiento permiten consultar esa situación. La conexión real con Mercado Pago requiere validación con sus credenciales de prueba.

Referencias oficiales: [Payments API: cancelación](https://www.mercadopago.com.pe/developers/es/reference/online-payments/checkout-api-payments/create-cancellation/put), [reembolso](https://www.mercadopago.com.pe/developers/es/reference/online-payments/checkout-api-payments/create-refund/post), [manejo de errores Kafka](https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html).

## Fallos y recuperación

Con token ADMIN, `GET /api/v1/operaciones/eventos` muestra pendientes, publicados, procesados y fallos archivados del servicio. Gateway dirige ese endpoint a orden-ms. Cada servicio también lo expone en su puerto interno con JWT ADMIN.

`GET /api/v1/operaciones/eventos/fallidos` lista hasta cien mensajes DLT archivados. Después de corregir la causa, `POST /api/v1/operaciones/eventos/fallidos/{failure_id}/reintentar` vuelve a poner el mismo evento en outbox. El eventId se conserva para que inbox proteja contra duplicados. Un evento inválido debe corregirse en su origen; reenviarlo sin resolver la causa vuelve a fallar.

## Datos y migraciones

Flyway sustituye ddl-auto=update. Un esquema nuevo ejecuta V1 y V2 existentes, luego las nuevas migraciones V10 en adelante. Un esquema preexistente sin historial se basa en versión 2; la aplicación verifica el esquema con Hibernate. Se conservan volúmenes y datos.

Antes de migrar datos antiguos, haga respaldo y compruebe que no existan dos reservas vigentes para el mismo inmueble ni dos pagos vigentes para la misma orden. Los índices nuevos rechazan ese estado en lugar de eliminar registros silenciosamente. Los pagos manuales antiguos sin una orden/SAGA asociada requieren conciliación antes de incorporarlos al flujo nuevo.

## Sustentación sugerida

Explique primero la operación inmobiliaria; después muestre Eureka y el acceso por Gateway, el bloqueo de una reserva, la aprobación del adelanto y la confirmación con un solo contrato. Repita con un rechazo y muestre que inmueble y reserva quedan liberados/cancelados. Finalmente explique que cada base confirma su transacción local y que la SAGA coordina mediante mensajes; no es una transacción distribuida ACID ni existe una garantía global de entrega exactamente una vez.
