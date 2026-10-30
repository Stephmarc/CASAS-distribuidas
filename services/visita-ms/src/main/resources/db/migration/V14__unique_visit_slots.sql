CREATE UNIQUE INDEX ux_visita_agente_horario ON visitas(agente_id,fecha_hora) WHERE estado IN ('SOLICITADA','CONFIRMADA','REPROGRAMADA');
CREATE UNIQUE INDEX ux_visita_cliente_horario ON visitas(cliente_id,fecha_hora) WHERE estado IN ('SOLICITADA','CONFIRMADA','REPROGRAMADA');
