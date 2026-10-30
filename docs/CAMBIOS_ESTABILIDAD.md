# Qué se pulió en CASAS

La actualización conserva el flujo inmobiliario y los nueve servicios. No incorpora código de KIDO ni comienza el frontend.

## Pagos con un resultado recuperable

Ahora se registra una intención antes de contactar al proveedor. Esa intención guarda una referencia y una huella de la solicitud, sin almacenar el token de tarjeta. Si se pierde la respuesta, el sistema consulta la referencia para encontrar el pago. Un reintento no vuelve a enviar el cobro.

La intención se confirma en una transacción independiente antes de la llamada HTTP. Después se guarda la respuesta en otra transacción. De esa manera, una caída del proceso deja una operación local identificable que puede conciliarse.

Si todavía no puede conocerse el resultado externo, la compensación permanece pendiente. No se declara reembolsado un cobro que no pudo verificarse. El endpoint GET /api/v1/pagos/{id}/seguimiento permite al propietario consultar ese estado. La referencia no encontrada no se interpreta automáticamente como prueba de que nunca hubo cobro.

La simulación local sigue disponible y separada del proveedor externo. El arranque exige elegir exactamente un modo de pago.

## Cambios simultáneos y mensajes atrasados

Pagos, vencimientos de reservas y recuperación de órdenes vuelven a leer los registros después del bloqueo. Esto evita que una transacción utilice un estado que otra acaba de cambiar.

La cancelación de un pago deja un cierre persistente incluso si el intento de reembolso tiene que repetirse. Las respuestas atrasadas no vuelven a activar un pago cancelado o reembolsado. La SAGA tampoco aplica rechazos antiguos a etapas que ya terminaron.

Las solicitudes con la misma clave de idempotencia se serializan. Outbox publica primero el evento pendiente más antiguo de cada operación, aunque existan varias instancias. Los fallos usan espera progresiva y no permiten que un mensaje posterior de la misma operación adelante al pendiente.

## Agenda y catálogo

Las visitas validan cliente activo, agente activo, inmueble disponible y fecha futura. La creación siempre comienza en SOLICITADA. Las modificaciones repiten las validaciones y existen restricciones de horario en la base. Una visita cancelada o terminada no se puede reabrir mediante una actualización.

El cliente puede cancelar su visita. El agente dispone de las rutas de actualización y cambio de estado permitidas. La API no deja marcar como realizada una visita futura.

Las propiedades comprueban dimensiones e importes. Los estados de reserva, venta y alquiler se producen mediante la SAGA. Al bloquear un inmueble se comprueba también que su precio coincida con el total capturado por la orden. Los borradores e inmuebles inactivos quedan fuera de las consultas del cliente. La baja de propiedades y agentes conserva su historial.

## Contratos y acceso

La aceptación de contrato tiene una acción propia para el cliente. Requiere una orden confirmada, un contrato pendiente de aceptación y que el contrato sea suyo. Se bloquea el registro antes de modificarlo. Esta aceptación es un registro de negocio y no una firma digital.

Se corrigieron respuestas que podían aparecer como error interno cuando correspondían a datos inválidos o falta de permisos. Los accesos a pagos, contratos y visitas de otro cliente devuelven 403. Las solicitudes mal formadas devuelven 400 y las dependencias temporalmente indisponibles devuelven 503.

Swagger incluye autorización Bearer JWT. Gateway permite CORS para los orígenes locales configurados, sin iniciar ninguna interfaz web.

## Arranque y mantenimiento

Los scripts verifican puertos y Java antes del inicio. Si una aplicación falla al arrancar, muestran sus logs y detienen únicamente los procesos de ese intento. El límite de memoria por aplicación es configurable. Se añadieron reinicio por servicio, diagnóstico y respaldo de las nueve bases.

Las credenciales de PostgreSQL se usan de forma consistente en Compose y los servicios. Las migraciones ya entregadas no se modifican. La compilación y pruebas pueden ejecutarse también en GitHub Actions cuando el grupo suba el código.

## Qué quedó comprobado

La actualización pasó clean verify con Java 21 y sus dependencias originales: 78 pruebas exitosas. También pasó 80 comprobaciones de migraciones y consultas en PostgreSQL embebido. El detalle y los límites están en VERIFICACION.md.

Las 32 comprobaciones HTTP anteriores ya pasaron en el equipo del grupo. La batería ampliada de esta versión se ejecuta con scripts/test-stability.ps1 sobre sus contenedores. Mercado Pago real sigue requiriendo validación con credenciales del proveedor; el servidor HTTP de las pruebas representa respuestas controladas.
