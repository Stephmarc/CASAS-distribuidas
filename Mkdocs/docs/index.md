# Unidad II · Sistema de distribución robusto

## Evidencia grupal — CASA O NADA

### Datos del equipo

| Campo | Información |
|---|---|
| **Grupo** | 9 |
| **Ciclo** | V |
| **Integrantes** | Alanguia Japura Miguel Angel · Jannys Graciela Navarro Acrota · Olger Meza Rupa |
| **Curso** | Desarrollo de Aplicaciones Distribuidas |
| **Proyecto** | CASA O NADA |
| **Repositorio** | [CASAS-distribuidas](https://github.com/Stephmarc/CASAS-distribuidas) |

## 1. Presentación de la unidad

En esta unidad se fortalece la arquitectura distribuida del proyecto **CASA O NADA**. El trabajo se concentra en dos problemas reales de una arquitectura de microservicios: la comunicación síncrona resiliente entre servicios y el control de acceso mediante tokens JWT.

La documentación se divide únicamente en las **dos tareas solicitadas**. Dentro de cada tarea, todo el contenido se presenta de forma continua para poder desplazarse verticalmente, igual que en la documentación de la Unidad I.

## 2. Tareas desarrolladas

### S06 · Feign y Circuit Breaker

Se documenta el descubrimiento mediante Eureka, la comunicación de `reserva-ms` con `propiedad-ms` usando OpenFeign, el funcionamiento normal del flujo, la apertura del Circuit Breaker, el fallback y la recuperación del servicio.

### S07 · Seguridad distribuida con JWT

Se documenta Keycloak como emisor de JWT, la configuración de los microservicios como Resource Server, la protección del Gateway y las pruebas de autorización por roles con respuestas `200`, `401`, `403` y `201`.
