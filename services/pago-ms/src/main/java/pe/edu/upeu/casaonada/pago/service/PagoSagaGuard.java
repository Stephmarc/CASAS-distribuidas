package pe.edu.upeu.casaonada.pago.service;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.casaonada.shared.*;
import tools.jackson.databind.ObjectMapper;

@Component
public class PagoSagaGuard {
  private final JdbcTemplate db;
  private final ObjectMapper json;

  public PagoSagaGuard(JdbcTemplate d, ObjectMapper j) {
    db = d;
    json = j;
  }

  public boolean lock(OperacionEvent e) {
    e.validate();
    db.update(
        "insert into pago_saga_guard(saga_id,orden_id,cancelada,payload) values (?,?,false,?) on"
            + " conflict(saga_id) do nothing",
        e.sagaId(),
        e.ordenId(),
        json.writeValueAsString(e));
    var row =
        db.queryForMap(
            "select cancelada,payload from pago_saga_guard where saga_id=? for update", e.sagaId());
    var original = json.readValue(row.get("payload").toString(), OperacionEvent.class);
    if (!original.sameOperation(e))
      throw new IllegalArgumentException("La operacion no coincide con el pago registrado");
    return Boolean.TRUE.equals(row.get("cancelada"));
  }

  public void cancel(OperacionEvent e) {
    db.update("update pago_saga_guard set cancelada=true where saga_id=?", e.sagaId());
  }

  public OperacionEvent context(Long ordenId) {
    var rows = db.queryForList("select payload from pago_saga_guard where orden_id=?", ordenId);
    if (rows.isEmpty())
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Pago antiguo sin orden asociada requiere conciliacion");
    return json.readValue(rows.get(0).get("payload").toString(), OperacionEvent.class);
  }

  public static OperacionEvent context(CasaClients.Orden o) {
    return new OperacionEvent(
        UUID.randomUUID().toString(),
        o.sagaId(),
        "PAGO_SOLICITADO",
        o.id(),
        o.reservaId(),
        o.propiedadId(),
        o.clienteId(),
        o.monto(),
        o.moneda(),
        o.tipo(),
        o.traceId(),
        null,
        o.total());
  }
}
