CREATE TABLE propiedades (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_inmueble VARCHAR(30) NOT NULL,
    tipo_operacion VARCHAR(20) NOT NULL,
    precio NUMERIC(14,2) NOT NULL CHECK (precio > 0),
    moneda VARCHAR(3) NOT NULL CHECK (moneda IN ('PEN', 'USD')),
    area_m2 NUMERIC(10,2) NOT NULL CHECK (area_m2 > 0),
    dormitorios INTEGER NOT NULL DEFAULT 0 CHECK (dormitorios >= 0),
    banos INTEGER NOT NULL DEFAULT 0 CHECK (banos >= 0),
    direccion VARCHAR(250) NOT NULL,
    distrito VARCHAR(100) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    propietario_id BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_propiedades_estado ON propiedades(estado);
CREATE INDEX idx_propiedades_ciudad ON propiedades(ciudad);
CREATE INDEX idx_propiedades_operacion ON propiedades(tipo_operacion);
