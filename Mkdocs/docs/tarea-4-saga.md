# S09 — SAGA inmobiliaria

Orden-ms orquesta una SAGA persistente: asociación de reserva, bloqueo de propiedad, pago, contrato, confirmación de propiedad y conversión de reserva. La operación termina en CONFIRMADA solamente después de todas las respuestas.

Ante fallo se cancela el contrato, se cancela o reembolsa el pago, se libera la propiedad y se cancela la reserva. Cada participante trabaja con su base y publica el resultado en una transacción local. Los plazos, cercos de cancelación y reintentos permiten recuperar la operación después de reinicios.

Consulte `docs/UNIDAD_2.md` para la tabla completa de estados, y `docs/VERIFICACION.md` para distinguir las pruebas ejecutadas de las pendientes. La evidencia reproducible se obtiene con `scripts/test-u2.ps1`.
