# 🏠 CASA O NADA

## Plataforma Inmobiliaria Distribuida

Sistema basado en arquitectura de microservicios para la gestión de clientes, propiedades, reservas, órdenes, pagos, contratos y notificaciones.


# 🚀 Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Cloud
- PostgreSQL
- Docker
- Eureka Server
- Config Server
- Spring Cloud Gateway
- Keycloak JWT


# 🏗️ Arquitectura


```
Cliente Web / Móvil

        ↓

Spring Cloud Gateway

        ↓

Eureka Server

        ↓

Microservicios

        ↓

PostgreSQL
```


# 📦 Microservicios

| Servicio | Función |
|---|---|
| cliente-ms | Gestión de clientes |
| propiedad-ms | Gestión de propiedades |
| agente-ms | Gestión de agentes inmobiliarios |
| visita-ms | Gestión de visitas |
| reserva-ms | Gestión de reservas |
| orden-ms | Gestión de operaciones de compra/alquiler |
| pago-ms | Procesamiento de pagos |
| contrato-ms | Gestión de contratos |
| notificacion-ms | Gestión de notificaciones |


# ⚙️ Requisitos

Antes de ejecutar el proyecto instalar:

- Java 21
- Docker Desktop
- Git


Verificar instalación:

```bash
java -version

docker --version

git --version
```


# ▶️ Ejecución del proyecto


## 1. Clonar repositorio

```bash
git clone https://github.com/Stephmarc/CASAS-distribuidas.git

cd CASAS-distribuidas
```


## 2. Levantar infraestructura


Ejecutar:

```bash
docker compose -f compose-dev.yml up -d
```


Verificar contenedores:

```bash
docker ps
```


---

# 3. Config Server


Ingresar:

```bash
cd infra/casa-config
```


Ejecutar:

```bash
../../mvnw spring-boot:run
```


Disponible en:

```
http://localhost:18888
```


---

# 4. Eureka Server


Ingresar:

```bash
cd infra/casa-eureka
```


Ejecutar:

```bash
../../mvnw spring-boot:run
```


Dashboard:

```
http://localhost:18761
```


---

# 5. Ejecutar Microservicios


Ejemplo:

```bash
cd services/propiedad-ms

../../mvnw spring-boot:run
```


## Puertos disponibles


| Servicio | Puerto |
|---|---|
| cliente-ms | 18101 |
| propiedad-ms | 18102 |
| agente-ms | 18103 |
| visita-ms | 18104 |
| reserva-ms | 18105 |
| orden-ms | 18106 |
| pago-ms | 18107 |
| contrato-ms | 18108 |
| notificacion-ms | 18109 |


---

# 6. Spring Cloud Gateway


Ejecutar:

```bash
cd infra/casa-gateway

../../mvnw spring-boot:run
```


Punto único de acceso:

```
http://localhost:18080
```


# 🔎 Pruebas del sistema


## Eureka Server

```
http://localhost:18761
```


## Swagger API

Ejemplo:

```
http://localhost:18102/swagger-ui/index.html
```


## Health Check

Ejemplo:

```
http://localhost:18102/actuator/health
```


# 👥 Equipo - Grupo 9


| Integrante | Microservicios |
|---|---|
| Grace | cliente-ms, orden-ms, notificacion-ms |
| Olger | propiedad-ms, reserva-ms, visita-ms |
| Alanguia | agente-ms, pago-ms, contrato-ms |


# 📌 Flujo principal del negocio


```
Cliente

 ↓

Búsqueda de propiedad

 ↓

Reserva

 ↓

Orden de compra/alquiler

 ↓

Pago

 ↓

Contrato

 ↓

Notificación
```


# 📄 Documentación

La documentación técnica y evidencias del proyecto se encuentran dentro del repositorio.
