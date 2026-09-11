CREATE TABLE propiedades (
 id BIGSERIAL PRIMARY KEY,
 titulo VARCHAR(160) NOT NULL,
 descripcion VARCHAR(2000) NOT NULL,
 ciudad VARCHAR(120) NOT NULL,
 direccion VARCHAR(220) NOT NULL,
 precio NUMERIC(14,2) NOT NULL CHECK (precio >= 0),
 habitaciones INTEGER NOT NULL CHECK (habitaciones >= 0),
 banos INTEGER NOT NULL CHECK (banos >= 0),
 area_m2 NUMERIC(10,2) NOT NULL CHECK (area_m2 > 0),
 tipo_operacion VARCHAR(20) NOT NULL,
 estado VARCHAR(30) NOT NULL,
 agente_id BIGINT,
 imagen_principal_url VARCHAR(500),
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
