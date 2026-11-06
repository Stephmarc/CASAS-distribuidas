# Guion y demo · Jannys y Alanguia

Presentar solo las diapositivas 1–6. Las 7–13 son apoyo para la demo o preguntas. Olger permanece como integrante del grupo; exponen Jannys y Miguel Angel Alanguia. Este reparto organiza la exposición y no atribuye contribuciones personales no verificadas.

## Pantallas que deben abrir

| Pantalla | URL | Acceso |
|---|---|---|
| App cliente | http://localhost:4300 | cliente / Cliente123! |
| App administrador, incógnito | http://localhost:4300 | admin / Admin123! |
| Grafana | http://localhost:23000 | admin / casas-demo-local |
| Tablero de CASAS | http://localhost:23000/d/casas-u2/casas-c2b7-operacion-distribuida?orgId=1&from=now-30m&to=now | mismo acceso Grafana |
| Eureka | http://localhost:28761 | sin contraseña |
| Kafka UI | http://localhost:28085 | sin contraseña |
| Catálogo JSON por Gateway | http://localhost:28080/api/v1/propiedades | público |
| MkDocs | https://stephmarc.github.io/CASAS-distribuidas/ | público |

Si Grafana abre la portada, entrar a Paneles de control y buscar CASAS, o usar el enlace directo de la tabla. La lista de recientes vacía no significa que falte el tablero. Para mostrar métricas de una operación nueva usar últimos 15–30 minutos, actualizar y realizar acciones en la app.

## Preparación

Mantener Docker Desktop abierto. Si el proyecto ya funciona, no reiniciarlo. Si está apagado:

```powershell
& "C:\Users\grace\Distribuidasss\unidad1\CASAS-distribuidas\scripts\start-complete.ps1"
```

Preparar una PowerShell en la raíz:

```powershell
Set-Location "C:\Users\grace\Distribuidasss\unidad1\CASAS-distribuidas"
```

Las contraseñas anteriores son de laboratorio. No proyectar tokens JWT.

## PPT esencial

### 1. Portada · Jannys

“Buenos días. Somos el grupo CASA O NADA. Hoy Alanguia y yo mostraremos nuestra plataforma inmobiliaria y su evolución durante la Unidad II. Primero explicaremos brevemente el producto y luego demostraremos su funcionamiento.”

### 2. Qué resuelve · Jannys

“El producto integra una operación inmobiliaria que normalmente estaría separada: buscar una vivienda, agendar una visita, reservar, registrar el adelanto y consultar el contrato. El cliente sigue su operación; el agente publica y coordina visitas; el administrador gestiona el catálogo y los pagos de prueba.”

### 3. Evolución · Jannys

“La Unidad I dejó la base: CRUD, persistencia, configuración, descubrimiento y Gateway. Conservamos esa estructura y pasamos de seis a nueve dominios. La Unidad II añade Keycloak, comunicación resiliente, Kafka, consistencia mediante una saga y un frontend Angular. Ahora podemos comprobar qué sucede cuando falla un servicio, se rechaza un pago o se repite un evento.”

### 4. Composición · Alanguia

“Dividimos el negocio en nueve servicios. Propiedad, agente y visita atienden el catálogo y la agenda. Cliente, reserva y orden conducen la operación. Pago, contrato y notificación completan el cierre. Cada uno tiene su propia base de dominio. También entregamos interfaz, infraestructura, pruebas y documentación.”

### 5. Arquitectura · Alanguia

“Angular consume la API de negocio mediante Gateway. Keycloak autentica y los servicios también validan el token. Eureka permite localizar los servicios por nombre y Config centraliza sus ajustes. Las consultas entre servicios usan Feign con respuesta controlada ante fallos. Kafka coordina eventos de una saga orquestada por orden-ms. Prometheus recoge métricas y Alloy envía logs a Loki; Grafana permite consultarlos con el mismo trace.”

### 6. Reparto y transición · Jannys

“Yo mostraré el CRUD y el recorrido del cliente. Alanguia mostrará infraestructura, caída controlada, mensajería y diagnóstico. Pasamos a la aplicación para verificarlo con datos de demostración.”

## Demo de Jannys

### A. Crear y consultar una vivienda

Entrar como ADMIN, Gestión → Catálogo. Crear una vivienda ficticia con título reconocible, ciudad, dirección de prueba, precio, características e imagen HTTPS. Comprobar la vista previa y publicar. Abrir el catálogo y su detalle.

“La imagen se guarda como URL pública HTTPS. La creación pasa por Gateway y se almacena en propiedad-ms. Ahora consultaremos la API para comprobar que el registro realmente existe.”

Abrir http://localhost:28080/api/v1/propiedades, actualizar y buscar el título. Mostrar id, precio y URL de imagen. El JSON es una lectura real del backend.

### B. CRUD completo por API

```powershell
& .\scripts\demo-crud.ps1 -PasoAPaso
```

El script espera Enter en cada paso. Crea un registro exclusivo nuevo, consulta su ID, cambia precio de 350000 a 365000, consulta de nuevo, elimina solo ese registro y verifica 404. No elimina la vivienda recién publicada en la interfaz ni propiedades de otras operaciones.

“POST devuelve 201 al crear. GET devuelve 200 al consultar. PUT modifica el precio y comprobamos que persiste. DELETE devuelve 204. Un GET posterior devuelve 404 porque el registro de prueba ya no existe.”

El frontend actual ofrece creación y consulta; actualización y eliminación se demuestran por API. No buscar botones de editar/eliminar que no existen en esta interfaz.

### C. Reserva y operación

Entrar como CLIENTE (o registrarse con DNI) en otra sesión. Elegir vivienda DISPONIBLE, responder si desea una visita previa y confirmar la reserva (dura 24 horas; llega un correo a Mailpit, http://localhost:28025). En Mi espacio pulsar «Pagar adelanto», elegir Yape, Plin o transferencia, escribir el número de operación y subir la captura. Entrar como AGENTE → Pagos → Ver captura → aprobar (también puede pedir otra captura o rechazar con motivo). Volver al cliente: la operación avanza a CONFIRMADA; el agente envía el contrato desde Contratos, el cliente lo acepta y el agente lo activa.

“La reserva no confirma inmediatamente toda la operación. Cada servicio realiza su paso y la saga avanza hasta completar el cierre. El pago es manual y lo valida un agente con la captura; en este entorno académico no se mueve dinero real.”

Para mostrar en backend los registros protegidos, usar lecturas autenticadas que no imprimen el token:

```powershell
& .\scripts\ver-backend.ps1 -Entidad reservas
& .\scripts\ver-backend.ps1 -Entidad ordenes
& .\scripts\ver-backend.ps1 -Entidad pagos
& .\scripts\ver-backend.ps1 -Entidad contratos
```

Cada ejecución hace una nueva consulta por Gateway. Identificar la reserva y orden propias por ID. Abrir la URL protegida sin cabecera Authorization puede dar 401 aunque otra pestaña tenga sesión de la SPA.

### D. Roles

“El cliente no tiene las funciones de Gestión del administrador. Ocultar botones ayuda a la experiencia, pero la protección real está en Gateway y en los microservicios.”

No confundir iniciar sesión en la aplicación con iniciar sesión en Grafana o Keycloak: son interfaces diferentes.

## Demo de Alanguia

### A. Eureka

Abrir http://localhost:28761. Mostrar nueve servicios de negocio y Gateway en UP.

“Eureka confirma qué instancias están registradas. No es una pantalla de viviendas o reservas. Los datos de negocio se consultan en la API o en la aplicación.”

### B. Resiliencia real

Abrir Grafana con el tablero de CASAS. Ejecutar:

```powershell
& .\scripts\test-resilience.ps1
```

“Provocamos la caída real de propiedad-ms. La consulta responde de forma degradada y el circuito pasa a OPEN. HALF_OPEN permite comprobar recuperación y CLOSED restablece la comunicación. El script recupera el servicio al finalizar; no borra datos.”

Durante el arranque Java puede tardar. Esperar el resultado final; no detener PowerShell a mitad del script. La pantalla de respaldo del PPT muestra propiedad en 0 durante la caída, pero la evaluación pide comprobar el caso en vivo.

### C. Kafka e idempotencia

Kafka UI → Topics → casa.operaciones.v1 → Messages. Buscar los mensajes de la orden recién creada. Mostrar type, eventId, sagaId y traceId.

```powershell
& .\scripts\test-replay.ps1
```

“Kafka puede entregar un evento más de una vez. Reenviamos el mismo CREAR_CONTRATO. El consumidor lo reconoce como duplicado y sigue existiendo un solo contrato. Outbox registra la intención de publicar junto al cambio local; inbox evita repetir el efecto.”

Publicar una vivienda no necesariamente produce eventos de esta saga. Se muestran los eventos del proceso reserva → orden → adelanto → contrato.

### D. Compensación

Para otra vivienda DISPONIBLE, crear una nueva operación y registrar adelanto. Como ADMIN rechazar ese pago de prueba. Esperar CANCELADA y comprobar que el inmueble vuelve a estar disponible.

“Al fallar un paso, la saga ejecuta compensaciones. No hay una transacción ACID global entre bases. Orden-ms coordina los pasos y persiste su estado: nuestra saga es orquestada, distinta de la coreografía del ejemplo del docente.”

### E. Grafana y seguridad

En el tablero, mostrar disponibilidad, peticiones, errores y latencia. En los logs, filtrar con el traceId de la misma orden y señalar dos o más servicios. Para un caso antiguo ajustar el rango horario; para uno nuevo usar el rango reciente.

“Este identificador conecta la petición con sus eventos y permite localizar en qué servicio ocurrió un problema.”

Prueba directa de seguridad:

```powershell
& .\scripts\test-distributed-security.ps1
```

“Sin token se recibe 401. Un cliente intentando una función de administrador recibe 403. Se comprueba directamente en los nueve servicios.”

## Cierre compartido

“El sistema conserva la base de Unidad I y añade seguridad, tolerancia a fallos, eventos, consistencia y observabilidad. Las pruebas registradas aprobaron 78 casos Java y 50 comprobaciones HTTP. El CRUD completo también quedó ejecutado. La documentación y evidencias están publicadas en MkDocs.”

Las diapositivas 7–13 se abren solo para apoyar el caso que estén mostrando o responder una pregunta. No leerlas todas. Para reducir duración, preparar la operación y las pestañas antes de iniciar y coordinar qué demostración pide ver el docente.

## Tiempos y pendientes reales

S12 asigna 2–3 minutos grupales al alcance, 2–3 a arquitectura y 2–3 a tareas; después 3 minutos de demo y 3 de preguntas por integrante. Con dos expositores, esa secuencia suma 18–21 minutos, pero la guía pide defensa individual por integrante: el cambio a dos personas debe estar acordado con el docente. No se afirma que haya dispensado al tercer integrante.

Grabar el pitch, ensayar sin leer el guion y ajustar declaraciones de aporte a lo realmente realizado por cada persona. Los scripts de prueba no sustituyen el dominio personal del proyecto.

Imágenes decorativas BT21: recursos oficiales de https://www.bt21.com/character, usados como detalles visuales solicitados, sin afiliación al proyecto.
