# Ejecutar y publicar

## Iniciar todo

Requisitos: Docker Desktop iniciado, Java 21 y Node.js. El inicio prepara una copia portable oficial de Node 24 si la versión instalada no es compatible con Angular 22. En PowerShell, desde la raíz del repositorio:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\start-complete.ps1
```

El script compila y prueba el backend, levanta infraestructura, inicia aplicaciones, construye Angular y abre el cliente. Conserva bases y volúmenes. El primer arranque descarga dependencias e imágenes.

| Acceso | URL |
|---|---|
| Cliente | http://localhost:4300 |
| Gateway | http://localhost:28080 |
| Keycloak | http://localhost:28090 |
| Eureka | http://localhost:28761 |
| Config | http://localhost:28888 |
| Kafka UI | http://localhost:28085 |
| Grafana | http://localhost:23000 |
| Prometheus | http://localhost:29090 |

Usuarios de demostración: `cliente / Cliente123!`, `cliente2 / Cliente2123!`, `agente / Agente123!`, `admin / Admin123!`. Son datos exclusivos del laboratorio. La consola de Keycloak usa `admin / admin`.

## Comprobar

```powershell
.\scripts	est-stability.ps1
.\scripts	est-distributed-security.ps1
.\scripts	est-resilience.ps1
.\scripts	est-replay.ps1
```

Las pruebas de negocio crean inmuebles y operaciones de laboratorio. Los resultados quedan en `.run`, fuera de Git. Para poblar un catálogo de muestra: `scripts/seed-demo.ps1`.

## Detener

```powershell
.\scripts\stop-complete.ps1
```

Para detener también contenedores, agregar `-StopInfrastructure`. No se usa `down -v`.

## Subir y publicar MkDocs

```powershell
.\scripts\publish.ps1
```

Revisar el resumen de cambios mostrado por el script. Hace commit y push a la rama actual usando la sesión Git de tu PC. El workflow publica MkDocs desde `main`; en GitHub, Settings → Pages → Source debe estar en **GitHub Actions**. Si se trabaja en otra rama, integrarla a `main` para publicar.

El ZIP no contiene `.git`, credenciales locales, bases ni dependencias descargadas. Para usarlo en otra PC, extraerlo en una carpeta nueva, clonar el repositorio y copiar su contenido sobre el clon antes de publicar. No reemplazar una carpeta `.git`.
