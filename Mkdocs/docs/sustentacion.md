# Sustentación del equipo

## Apertura sugerida (60 segundos)

“CASA O NADA reúne una operación inmobiliaria desde la búsqueda hasta el contrato. En Unidad I construimos la base: servicios, configuración, descubrimiento y Gateway. En Unidad II añadimos identidad con Keycloak, comunicación resiliente y coordinación por eventos. Si un pago falla, el sistema compensa; si un evento se repite, no duplica el efecto. El cliente Angular permite seguir ese recorrido y Grafana ayuda a diagnosticarlo.”

## Preparación individual

La siguiente es una distribución **sugerida para practicar**; cada integrante debe verificarla y ajustarla a su aporte real. No constituye evidencia de autoría ni una declaración de tareas realizadas personalmente.

| Integrante | Demostración propuesta |
|---|---|
| Jannys Graciela Navarro Acrota | Frontend, PKCE y permisos de cliente |
| Olger Meza Rupa | Resiliencia, Gateway y diagnóstico por trace |
| Miguel Angel Alanguia Japura | Kafka, idempotencia y compensación de saga |

## Orden de la demo

1. Alcance y arquitectura (2–3 minutos cada uno).
2. Distribución real de tareas (2–3 minutos).
3. Cada integrante demuestra su capacidad (3 minutos): fallo, seguridad, evento, compensación, panel o frontend.
4. Cada integrante responde preguntas (3 minutos).

## Preguntas para practicar

- ¿Qué diferencia hay entre timeout, reintento y Circuit Breaker?
- ¿Por qué la escritura falla con 503 en vez de inventar disponibilidad?
- ¿Por qué validar JWT dentro de cada servicio?
- ¿Cómo impide `cliente_id` consultar datos de otra persona?
- ¿Qué pasa si Kafka entrega el evento dos veces?
- ¿Cómo se recupera una orden si el pago se rechaza después del bloqueo?
- ¿Qué diferencia existe entre una saga orquestada y coreografiada?
- ¿Por qué PKCE y memoria para una SPA?
- ¿Qué trace permite encontrar el mismo proceso en dos servicios?

## Revisión previa

Verificar aporte individual, comunicación y tiempo, actitud profesional, repositorio y estándares, MkDocs publicado, y pitch/demo. La página S12 menciona siete subaspectos, mientras la Tabla 3 de `u2-producto` enumera seis; se cubren esos seis y se añade la explicación de una decisión propia (saga orquestada).

El video pitch y la defensa deben grabarlos y realizarlos los integrantes. Los guiones y capturas no los sustituyen.
