CREATE TABLE clientes (
 id BIGSERIAL PRIMARY KEY,
 keycloak_user_id VARCHAR(120) UNIQUE,
 nombres VARCHAR(100) NOT NULL,
 apellidos VARCHAR(100) NOT NULL,
 email VARCHAR(160) NOT NULL UNIQUE,
 telefono VARCHAR(30),
 presupuesto_min NUMERIC(14,2),
 presupuesto_max NUMERIC(14,2),
 ciudad_preferida VARCHAR(120),
 tipo_operacion_preferida VARCHAR(20),
 estado VARCHAR(20) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
