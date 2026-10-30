# S08 — Mensajería asíncrona

Implementación en `common/casa-common` y los paquetes `messaging` de cada participante. Kafka desacopla orden, reserva, propiedad, pago, contrato y notificación. Outbox mantiene los eventos pendientes ante caída del broker; inbox evita aplicar duplicados. Reintentos y DLT permiten recuperar errores.

La ejecución, contratos de eventos, reintentos y endpoints técnicos se explican en `docs/UNIDAD_2.md`. Para la evidencia ejecute `scripts/test-u2.ps1`, observe el tópico casa.operaciones.v1 y consulte el estado de eventos con ADMIN. No use las capturas antiguas como evidencia de esta sesión.
