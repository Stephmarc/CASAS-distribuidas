# CASA O NADA · Preparación de la sustentación

Jannys Graciela Navarro Acrota · Olger Meza Rupa · Miguel Angel Alanguia Japura. Grupo 9.

## Qué pide el docente

Revisión del 30/10/2026: [Evaluación S12](https://262dist.github.io/pagatu/sesiones/S12_Evaluacion_Unidad_2/) y [producto / rúbrica de Unidad II](https://262dist.github.io/pagatu/proyecto-sello/u2-producto/).

**Se necesitan diapositivas y aplicación funcionando en vivo.** Las capturas respaldan la entrega, pero no sustituyen provocar y explicar los casos en vivo.

| Parte | Tiempo indicado |
|---|---|
| Alcance del proyecto, con diapositiva | 2–3 minutos grupales |
| Arquitectura, con diapositiva | 2–3 minutos grupales |
| Asignación real de tareas, con diapositiva | 2–3 minutos grupales |
| Demo y sustentación | 3 minutos por integrante |
| Preguntas | 3 minutos por integrante |

La suma de esas partes para tres personas es 24–27 minutos, sin contar un pitch separado. Confirmar con el docente si aplica otro límite al horario del grupo.

También exige un video pitch, defensa individual, `u2-producto.md` adaptado, repositorio reproducible con topics y documentación publicada. **No se especifica un informe Word/PDF independiente**: el producto escrito está en MkDocs.

## Material listo

- [PowerPoint de sustentación](assets/CASAS-Sustentacion-Unidad2-Final.pptx): doce diapositivas editables con notas de exposición.
- [Producto de Unidad II](https://stephmarc.github.io/CASAS-distribuidas/u2-producto/).
- [Evidencias y resultados reales](https://stephmarc.github.io/CASAS-distribuidas/evidencias/).
- [Código](https://github.com/Stephmarc/CASAS-distribuidas), rama `main`.
- Capturas de aplicación, formulario con foto, backend y observabilidad; reportes reales y log de Maven.
- ZIP completo de respaldo `CASAS-Entrega-Unidad2.zip`.

## Antes de exponer

1. Mantener Docker Desktop abierto. Si la app ya funciona, no reiniciar todo innecesariamente.
2. Si está apagada, abrir PowerShell y ejecutar **solo el comando**, sin copiar `PS C:\...>`:

```powershell
& "C:\Users\grace\Distribuidasss\unidad1\CASAS-distribuidas\scripts\start-complete.ps1"
```

3. Abrir las pestañas que se usarán:

| Pantalla | Dirección | Acceso de laboratorio |
|---|---|---|
| Aplicación | http://localhost:4300 | cliente / Cliente123! |
| Gestión | misma aplicación, otra sesión de navegador | admin / Admin123! |
| Eureka | http://localhost:28761 | sin login |
| Kafka UI | http://localhost:28085 | sin login |
| Grafana | http://localhost:23000 | admin / casas-demo-local |

Estas credenciales son únicamente de demostración local. Para mantener cliente y administrador separados, usar ventana normal e incógnita. No hace falta exponer el token JWT en una captura.

## Demo por requisito

Desde la raíz del proyecto, ejecutar los scripts siguientes. La prueba HTTP completa crea datos ficticios adicionales; no es necesario repetirla durante cada ensayo.

```powershell
Set-Location "C:\Users\grace\Distribuidasss\unidad1\CASAS-distribuidas"
```

| Requisito | Acción en vivo | Qué debe verse |
|---|---|---|
| Resiliencia | `& .\scripts\test-resilience.ps1` | Caída real de propiedad-ms, respuesta degradada, OPEN, HALF_OPEN y CLOSED tras recuperar el proceso |
| Seguridad en varios servicios | `& .\scripts\test-distributed-security.ps1` | Sin token: 401. Cliente intentando ruta ADMIN: 403. Se comprueban los nueve servicios |
| Evento publicado y consumido | Crear reserva, orden y adelanto desde la app; aprobar pago como ADMIN | Mensajes en `casa.operaciones.v1` y avance a contrato / notificación |
| Reprocesamiento idempotente | `& .\scripts\test-replay.ps1` | Mismo evento CREAR_CONTRATO, `duplicate=true`, un contrato antes y después |
| Compensación de fallo parcial | En una operación nueva, registrar adelanto y rechazar simulación como ADMIN | Orden CANCELADA y propiedad liberada después del procesamiento asíncrono |
| Observabilidad | Grafana: métricas y Explore / Loki | Mismo trace en dos o más servicios, health y tráfico visibles |
| Frontend por Gateway y rol | Flujo cliente, luego acceso Gestión como ADMIN | Sesión real y funciones distintas por rol; toda API de negocio usa 28080 |

Prueba integral disponible:

```powershell
& .\scripts\test-stability.ps1
```

La ejecución registrada aprobó 50/50 verificaciones HTTP. Maven aprobó 78 pruebas Java. Los archivos de evidencia incluyen los resultados reales; no se deben sustituir por textos que simplemente digan “correcto”.

### Flujo de la aplicación

1. Cliente: elegir vivienda DISPONIBLE, abrir detalle y reservar. Ir a Mi espacio → Mis operaciones.
2. Esperar a `ESPERANDO_PAGO` y registrar el adelanto del simulador.
3. Administrador, otra sesión: Gestión → Pagos de prueba → aprobar la simulación de esa orden.
4. Cliente: actualizar o esperar el refresco automático. Mostrar orden CONFIRMADA, contrato y notificación.
5. Para compensación, repetir con otra propiedad disponible y **rechazar** ese pago. Esperar CANCELADA y comprobar que la propiedad vuelve a estar disponible.
6. Para mostrar publicación, ADMIN → Gestión → Catálogo. Completar datos, pegar una URL pública HTTPS de imagen y comprobar la vista previa antes de publicar.

Los pagos son `SANDBOX_LOCAL`: no mueven dinero. Los contratos son registros académicos, no escrituras públicas ni firma digital. Las viviendas de demostración son ficticias y sus fotos ilustrativas.

### Grafana y trazabilidad

En Explore elegir Loki. Para una operación nueva, usar su `traceId` registrado en los logs. Como respaldo histórico comprobado el 30/10/2026, existe el trace:

```text
07da5641-e729-402d-a640-14a64108827e
```

Consulta:

```logql
{job="casas"} |= "07da5641-e729-402d-a640-14a64108827e"
```

Usar rango 30/10/2026 05:30–07:00, zona Lima, para ese ejemplo histórico. Su reporte comprobó correlación en siete servicios. Para una demo nueva, seleccionar el rango reciente y el trace de esa operación, no reutilizar una captura como si fuera en vivo.

## Guion del pitch, aproximadamente 60 segundos

“Somos CASA O NADA, una plataforma académica para consultar viviendas, coordinar visitas y seguir una operación de compra o alquiler. La Unidad I aportó configuración, descubrimiento y acceso mediante Gateway. En la Unidad II incorporamos Keycloak, comunicación resiliente y coordinación con Kafka. Una reserva inicia una saga persistida: valida disponibilidad, espera el adelanto y genera el contrato. Si un pago se rechaza, el sistema compensa y libera el inmueble. Si un evento se repite, el consumidor evita duplicar su efecto. Angular permite operar por rol y Grafana relaciona métricas y logs mediante el mismo identificador de traza. Mostraremos estos comportamientos funcionando en nuestro dominio inmobiliario.”

Este texto es un guion, **no un video ya grabado**. El equipo debe grabarlo y explicar sus aportes reales.

## Preguntas y respuestas para practicar

- **Timeout / reintento / Circuit Breaker:** el timeout limita cuánto espera una llamada; un reintento repite un fallo transitorio; el circuito deja de enviar llamadas mientras el destino está fallando y prueba su recuperación en HALF_OPEN.
- **¿Por qué seguridad dentro de cada servicio?** Alguien podría llamar directamente a su puerto. El servicio verifica firma, emisor, roles y pertenencia del cliente aunque no pase por Gateway.
- **¿Para qué Kafka?** Desacopla el paso que solicita una acción del que la ejecuta. Se pierde la respuesta inmediata de toda la operación y se debe administrar consistencia eventual.
- **¿Cómo se evita el duplicado?** Inbox registra el eventId procesado y las restricciones de negocio impiden duplicar el efecto. No se afirma que Kafka entregue exactamente una vez.
- **¿Qué hace la compensación?** Ejecuta acciones de negocio que deshacen pasos confirmados, como liberar la propiedad y cancelar la reserva. No es un rollback ACID global.
- **¿Decisión propia frente al docente?** CASAS usa saga orquestada por orden-ms, con estado persistido. El ejemplo del docente explica coreografía. El equipo debe defender la diferencia, no llamarlas iguales.
- **¿Por qué PKCE?** Una SPA no puede guardar un secreto de cliente. Code + PKCE protege el intercambio de código; el token se conserva en memoria y se envía al Gateway.
- **¿Qué prueba el trace?** Relaciona una misma operación en distintos servicios y permite localizar el punto de fallo en el panel.
- **¿JWT propio y migración?** La versión entregada usa Keycloak. No se presenta un auth-ms propio como una fase ejecutada si no existe en el código. Estudiar la diferencia entre el `sub` de identidad (String) y el `cliente_id` de negocio firmado.

## Qué falta del equipo

- Grabar el video pitch y tenerlo descargado para reproducirlo.
- Ajustar la diapositiva de reparto a la contribución real de cada persona. La propuesta de ensayo no acredita autoría individual.
- Ensayar una demo de tres minutos por persona y responder las preguntas sin depender del guion.
- Confirmar el topic académico de sección: los demás topics conocidos ya están configurados. No se inventó una sección.

La entrega técnica ya tiene pruebas registradas. La defensa individual, la presentación personal y el video son parte de la evaluación y no quedan resueltos únicamente por código o capturas.
