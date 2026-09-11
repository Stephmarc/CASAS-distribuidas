CREATE INDEX idx_visitas_agenda ON visitas(agente_id, fecha_hora, estado);
CREATE INDEX idx_visitas_propiedad ON visitas(propiedad_id);
