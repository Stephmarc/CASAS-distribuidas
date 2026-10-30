# Cambios de la entrega CASAS

- Se incorporó el backend estabilizado aportado por el equipo: cliente, orden, notificación, outbox/inbox, compensaciones y recuperación.
- Se completó Angular a partir del prototipo, con catálogo, visitas, reserva, adelanto, contrato, avisos y gestión por rol.
- El cliente público Keycloak exige PKCE S256. Los scripts usan un cliente de pruebas separado.
- Se aislaron puertos de CASAS para evitar colisiones con otros ejercicios locales.
- Se añadió observabilidad con métricas, logs, panel provisionado y trazas entre llamadas/eventos.
- Se corrigió la búsqueda de propiedades sin filtros sobre PostgreSQL con especificaciones dinámicas.
- Se incorporó geocodificación real de ciudades, con timeout, caché y degradación sin bloquear reservas.
- Se corrigieron los scripts de prueba para enumerar respuestas JSON y comprobar CORS en PowerShell.
- Se organizaron Unidad I y sesiones S06–S11, producto U2, evidencia y guion S12 en MkDocs.
- Se añadieron workflows de backend, frontend y Pages, scripts de inicio completo y publicación.

La implementación y revisión fueron asistidas por IA. El equipo debe revisar el código y demostrar su comprensión y aporte real al sustentar.
