# 🏠 CASA O NADA

## Plataforma Inmobiliaria Distribuida

Sistema basado en microservicios para gestionar clientes, propiedades, reservas, órdenes, pagos, contratos y notificaciones.


## Tecnologías

- Java 21
- Spring Boot
- Spring Cloud
- PostgreSQL
- Docker
- Eureka Server
- Config Server
- Spring Cloud Gateway
- Keycloak JWT


# Arquitectura


Cliente
|
Gateway
|
Eureka Server
|
Microservicios
|
PostgreSQL



# Microservicios

| Servicio | Función |
|---|---|
| cliente-ms | Gestión de clientes |
| propiedad-ms | Gestión de propiedades |
| agente-ms | Gestión de agentes |
| visita-ms | Gestión de visitas |
| reserva-ms | Gestión de reservas |
| orden-ms | Gestión de órdenes |
| pago-ms | Procesamiento de pagos |
| contrato-ms | Gestión de contratos |
| notificacion-ms | Notificaciones |


# Requisitos

Instalar:

- Java 21
- Docker Desktop
- Git


Verificar:

```bash
java -version
docker --version
Ejecución del proyecto
1. Clonar repositorio
git clone https://github.com/Stephmarc/CASAS-distribuidas.git

cd CASAS-distribuidas
2. Levantar base de datos
docker compose -f compose-dev.yml up -d

Verificar:

docker ps
3. Config Server

Abrir terminal:

cd infra/casa-config

../../mvnw spring-boot:run

Disponible:

http://localhost:18888
4. Eureka Server

Abrir terminal:

cd infra/casa-eureka

../../mvnw spring-boot:run

Disponible:

http://localhost:18761
5. Ejecutar Microservicios

Ejemplo:

cd services/propiedad-ms

../../mvnw spring-boot:run

Servicios disponibles:

Servicio	Puerto
cliente-ms	18101
propiedad-ms	18102
agente-ms	18103
visita-ms	18104
reserva-ms	18105
orden-ms	18106
pago-ms	18107
contrato-ms	18108
notificacion-ms	18109
6. Gateway

Ejecutar:

cd infra/casa-gateway

../../mvnw spring-boot:run

Entrada principal:

http://localhost:18080
Pruebas
Eureka
http://localhost:18761
Swagger

Ejemplo:

http://localhost:18102/swagger-ui/index.html
Health Check

Ejemplo:

http://localhost:18102/actuator/health
Equipo - Grupo 9
Integrante	Microservicios
Grace	cliente-ms, orden-ms, notificacion-ms
Olger	propiedad-ms, reserva-ms, visita-ms
Alanguia	agente-ms, pago-ms, contrato-ms

Este README queda más como los proyectos reales de GitHub:
- alguien clona → sabe qué instalar
- sabe qué levantar
- sabe los puertos
- sabe dónde probar
- no se pierde leyendo teoría

Después encima de este hacemos **la presentación**, pero ya con capturas reales del sistema funcionando
