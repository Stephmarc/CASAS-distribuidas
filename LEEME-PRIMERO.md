# CASAS — Lee esto primero

El proyecto grupal está actualizado en:

`C:\Users\grace\Distribuidasss\unidad1\CASAS-distribuidas`

El ZIP contiene una copia completa de fuentes, frontend construido, documentación construida y evidencias. No contiene Git, contraseñas locales, bases de datos ni carpetas de dependencias. Tu proyecto personal Pagatu permanece separado.

## 1. Encender el proyecto

Abre Docker Desktop y espera a que el motor esté listo. Abre **PowerShell** y copia solamente estas líneas (no copies el texto `PS C:\...>` del terminal):

```powershell
Set-ExecutionPolicy -Scope Process Bypass
Set-Location -LiteralPath "C:\Users\grace\Distribuidasss\unidad1\CASAS-distribuidas"
& ".\scripts\start-complete.ps1"
```

El arranque compila Java, ejecuta sus pruebas, inicia infraestructura y aplicaciones y construye Angular. Si el Node instalado es antiguo, prepara una copia portable oficial sin cambiar el Node del sistema. El primer arranque tarda por las descargas. No borra volúmenes y carga nueve inmuebles ficticios con fotos y dos agentes; `-SkipDemoData` omite esa carga.

Abre `http://localhost:4300`. Usuarios de laboratorio:

| Rol | Usuario | Clave |
|---|---|---|
| Cliente | cliente | Cliente123! |
| Segundo cliente | cliente2 | Cliente2123! |
| Agente | agente | Agente123! |
| Administrador de aplicación | admin | Admin123! |

El inicio completo carga nueve inmuebles con fotos automáticamente. Para repetir la carga sin duplicarlos:

```powershell
& ".\scripts\seed-demo.ps1"
```

Para aprobar o rechazar un pago simulado, entra como administrador y abre Gestión → Pagos de prueba. Los pagos no mueven dinero. Grafana está en `http://localhost:23000`: usuario `admin`, clave `casas-demo-local`.

## 2. Repetir las comprobaciones

```powershell
& ".\scripts\test-stability.ps1"
& ".\scripts\test-distributed-security.ps1"
& ".\scripts\test-resilience.ps1"
& ".\scripts\test-replay.ps1"
```

La prueba de resiliencia detiene únicamente el proceso de propiedad registrado por este proyecto y lo recupera. Las pruebas crean casos de laboratorio. Los resultados se guardan en `.run`.

Ya se verificaron 78 pruebas unitarias, 50 comprobaciones HTTP, seguridad en nueve servicios, 80 comprobaciones SQL, caída real y recuperación, duplicados Kafka y un flujo Angular confirmado. Consulta `Mkdocs/docs/evidencias.md` para ver pruebas y límites: el Compose completo ya fue verificado en el contexto normal de Windows, y los pagos reales de Mercado Pago no fueron validados.

## 3. Subir el proyecto a su Git

Desde la misma PowerShell y carpeta:

```powershell
& ".\scripts\publish.ps1"
```

El script muestra los cambios, crea un commit si falta y hace push a la rama actual (`main` en tu copia). Si GitHub solicita ingresar, usa **Stephmarc**. La entrega fue subida a `main` y MkDocs ya se publicó. Este script sirve para subir los próximos cambios.

Pages ya está configurado con **GitHub Actions**. En una copia o repositorio nuevo, se configura en **Settings → Pages → Build and deployment → Source**. En **Actions**, espera que “Publicar MkDocs” termine en verde. La dirección prevista es:

https://stephmarc.github.io/CASAS-distribuidas/

No la consideres publicada hasta que el workflow termine correctamente. Si falla, conserva el mensaje de error; no borres el proyecto ni las bases.

## 4. Si utilizas el ZIP en otra PC

Extrae el ZIP en una carpeta nueva. Clona `https://github.com/Stephmarc/CASAS-distribuidas.git` y copia el contenido de la carpeta `CASAS-distribuidas` del ZIP sobre el clon. Conserva la carpeta `.git` del clon. Después ejecuta los mismos scripts. Java 21 y Docker Desktop deben estar instalados.

El ZIP también incluye `frontend/dist` y `Mkdocs/site` como versiones construidas para revisión; el código fuente y los lockfiles permiten reconstruirlas.

## 5. Lo que debe preparar el equipo

- Grabar el pitch y practicar la defensa individual; el guion está en MkDocs.
- Confirmar la distribución real de aportes. La tabla de sustentación es una propuesta para practicar, no una autoría inventada.
- Los topics base ya están guardados en GitHub → About. Falta confirmar la sección real. Base del docente: `campus-juliaca`, `semestre-2026-2`, `linea-software`, `tipo-ps`, `dist`, `grupo-9-casa-o-nada` y la **sección real** del curso (`seccion-...`). No se adivinó la sección.
- Si el docente exige capturas de escritorio con reloj y URL, tomarlas durante su demo. Las incluidas son capturas reales del navegador integrado.

Para detener los procesos del proyecto:

```powershell
& ".\scripts\stop-complete.ps1"
```

Para detener además sus contenedores: `& ".\scripts\stop-complete.ps1" -StopInfrastructure`. Se conservan los datos.

## Material para presentar S12

La entrega incluye ntrega/CASAS-Sustentacion-Unidad2-Final.pptx y ntrega/CASAS-Guia-Sustentacion.md. El docente pide diapositivas, demo en vivo, video pitch y defensa individual. El producto escrito está publicado en MkDocs. El equipo debe grabar el pitch y confirmar el reparto según sus aportes reales.

