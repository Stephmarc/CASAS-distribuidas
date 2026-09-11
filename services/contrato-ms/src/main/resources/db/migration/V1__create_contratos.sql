CREATE TABLE contratos (
 id BIGSERIAL PRIMARY KEY,
 orden_id BIGINT NOT NULL UNIQUE,
 cliente_id BIGINT NOT NULL,
 propiedad_id BIGINT NOT NULL,
 tipo VARCHAR(20) NOT NULL,
 fecha_inicio TIMESTAMPTZ NOT NULL,
 fecha_fin TIMESTAMPTZ,
 monto NUMERIC(14,2) NOT NULL CHECK (monto >= 0),
 version INTEGER NOT NULL CHECK (version > 0),
 estado VARCHAR(30) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
