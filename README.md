# 🏠 CASA O NADA

## Plataforma Inmobiliaria Distribuida basada en Microservicios

CASA O NADA es una plataforma distribuida orientada a la gestión de compra, alquiler y administración de propiedades inmobiliarias.

El sistema implementa una arquitectura basada en microservicios independientes, permitiendo escalabilidad, separación de responsabilidades y comunicación eficiente entre servicios.


## Objetivo

Desarrollar una plataforma inmobiliaria donde los usuarios puedan:

- Consultar propiedades disponibles.
- Gestionar clientes.
- Crear reservas.
- Procesar órdenes de compra o alquiler.
- Registrar pagos.
- Generar contratos.
- Recibir notificaciones.


# Arquitectura

La solución utiliza:

- Spring Boot
- Spring Cloud
- Eureka Server
- Config Server
- Spring Cloud Gateway
- PostgreSQL
- Docker
- Keycloak JWT


# Microservicios


| Microservicio | Descripción |
|-|-|
| cliente-ms | Gestión de clientes |
| propiedad-ms | Gestión de propiedades |
| agente-ms | Administración de agentes inmobiliarios |
| visita-ms | Gestión de visitas |
| reserva-ms | Gestión de reservas |
| orden-ms | Gestión de operaciones compra/alquiler |
| pago-ms | Procesamiento de pagos |
| contrato-ms | Gestión contractual |
| notificacion-ms | Comunicación con usuarios |


# Infraestructura


| Componente | Función | Puerto |
|-|-|-|
| Config Server | Configuración centralizada | 18888 |
| Eureka Server | Registro y descubrimiento | 18761 |
| Gateway | Punto único de acceso | 18080 |
| PostgreSQL | Persistencia | Docker |


# Equipo - Grupo 9

| Integrante | Responsabilidad |
|-|-|
| Grace | cliente-ms, orden-ms, notificacion-ms |
| Olger | propiedad-ms, reserva-ms, visita-ms |
| Alanguia | agente-ms, pago-ms, contrato-ms |


# Ejecución

Ver:

[Guía de ejecución](GUIA_EJECUCION.md)
