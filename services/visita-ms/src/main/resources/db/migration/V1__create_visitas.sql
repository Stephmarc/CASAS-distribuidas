CREATE TABLE visitas (
 id BIGSERIAL PRIMARY KEY,
 cliente_id BIGINT NOT NULL,
 propiedad_id BIGINT NOT NULL,
 agente_id BIGINT NOT NULL,
 fecha_hora TIMESTAMPTZ NOT NULL,
 observaciones VARCHAR(1000),
 estado VARCHAR(30) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
