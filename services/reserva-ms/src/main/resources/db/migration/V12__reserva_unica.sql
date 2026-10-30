CREATE UNIQUE INDEX ux_reserva_propiedad_vigente ON reservas(propiedad_id) WHERE estado IN ('PENDIENTE','ACTIVA');
