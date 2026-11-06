# CASAS · CASA O NADA

Plataforma inmobiliaria distribuida del Grupo 9: **Jannys Graciela Navarro Acrota, Olger Meza Rupa y Miguel Angel Alanguia Japura**.

Compra y alquiler con catálogo, visitas, reservas, órdenes, adelantos, contratos y notificaciones. Nueve servicios de negocio con bases separadas, Config Server, Eureka, Gateway, Keycloak, Kafka, outbox/inbox y saga orquestada. Frontend Angular con PKCE; Prometheus, Loki, Alloy y Grafana para diagnóstico.

## Iniciar

Requisitos: Docker Desktop, Java 21 y Node.js 24.15+ (línea 24). Desde PowerShell en esta carpeta:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\start-complete.ps1
```

Cliente: http://localhost:4300 · Gateway: http://localhost:28080 · Grafana: http://localhost:23000.

```powershell
.\scripts\seed-demo.ps1
.\scripts\test-stability.ps1
.\scripts\test-distributed-security.ps1
.\scripts\test-resilience.ps1
.\scripts\test-replay.ps1
.\scripts\stop-complete.ps1
```

El primer arranque descarga dependencias, ejecuta las pruebas y construye el frontend. No elimina datos. Las credenciales de demostración y puertos se describen en [Ejecución](Mkdocs/docs/ejecucion.md). Los pagos locales son simulados y no mueven dinero.

## Documentación y entrega

- [Producto de Unidad II](Mkdocs/docs/u2-producto.md).
- [Base de Unidad I](Mkdocs/docs/unidad1.md).
- [Resultados y límites de verificación](Mkdocs/docs/evidencias.md).
- [Sustentación individual](Mkdocs/docs/sustentacion.md).
- [Publicar](Mkdocs/docs/ejecucion.md#subir-y-publicar-mkdocs): `scripts/publish.ps1`.

El backend estabilizado aportado por el equipo se incorpora conservando paquetes y migraciones. Las guías históricas de estabilización permanecen en `docs/`; las páginas de MkDocs documentan la entrega actual. Ninguna prueba automática sustituye el video pitch ni la defensa personal.


APP_DNI_TOKEN=sk_21452.BQb5uz5pePwI3hInzkJG8bh5WpbKJjK2
APP_MAIL_HOST=smtp.gmail.com
APP_MAIL_PORT=587
APP_MAIL_USER=tucorreo@gmail.com
APP_MAIL_PASSWORD=abcdefghijklmnop
APP_MAIL_FROM=CASAS <tucorreo@gmail.com>




powershell -ExecutionPolicy Bypass -File .\scripts\start-complete.ps1

