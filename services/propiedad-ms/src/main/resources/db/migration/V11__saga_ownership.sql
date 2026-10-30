ALTER TABLE propiedades ADD COLUMN saga_id varchar(36);

CREATE TABLE saga_fences(saga_id varchar(36) PRIMARY KEY,cancelada boolean NOT NULL);
