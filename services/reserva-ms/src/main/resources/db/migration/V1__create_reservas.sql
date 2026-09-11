CREATE TABLE reservas (
 id BIGSERIAL PRIMARY KEY,
 cliente_id BIGINT NOT NULL,
 propiedad_id BIGINT NOT NULL,
 fecha_expiracion TIMESTAMPTZ NOT NULL,
 monto_reserva NUMERIC(14,2) NOT NULL CHECK (monto_reserva >= 0),
 estado VARCHAR(30) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
