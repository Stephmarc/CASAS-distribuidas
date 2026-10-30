CREATE TABLE clientes(id bigint PRIMARY KEY,nombres varchar(160) NOT NULL,email varchar(200) NOT NULL UNIQUE,activo boolean NOT NULL DEFAULT true);
INSERT INTO clientes(id,nombres,email,activo) VALUES (1,'Cliente de demostracion','cliente@casa.local',true),(2,'Segundo cliente de demostracion','cliente2@casa.local',true);
