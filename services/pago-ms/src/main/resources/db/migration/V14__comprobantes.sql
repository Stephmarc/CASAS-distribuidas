ALTER TABLE pagos ADD COLUMN metodo varchar(20);
ALTER TABLE pagos ADD COLUMN nro_operacion varchar(40);
ALTER TABLE pagos ADD COLUMN comprobante_at timestamptz;
ALTER TABLE pagos ADD COLUMN motivo_revision varchar(200);
CREATE TABLE pago_comprobantes(pago_id bigint PRIMARY KEY REFERENCES pagos(id), tipo varchar(40) NOT NULL, datos bytea NOT NULL);
