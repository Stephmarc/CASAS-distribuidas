CREATE INDEX idx_reservas_propiedad_estado ON reservas(propiedad_id, estado);
CREATE INDEX idx_reservas_cliente ON reservas(cliente_id);
