CREATE TABLE pago_saga_guard(saga_id varchar(36) PRIMARY KEY,orden_id bigint NOT NULL UNIQUE,cancelada boolean NOT NULL,payload text NOT NULL);
CREATE UNIQUE INDEX ux_pago_orden_vigente ON pagos(orden_id) WHERE estado IN ('CREADO','PENDIENTE','APROBADO');
