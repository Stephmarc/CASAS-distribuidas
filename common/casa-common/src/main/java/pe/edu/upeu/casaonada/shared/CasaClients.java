package pe.edu.upeu.casaonada.shared;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

public final class CasaClients {
  private CasaClients() {}

  public record Reserva(
      Long id,
      Long clienteId,
      Long propiedadId,
      OffsetDateTime fechaExpiracion,
      BigDecimal montoReserva,
      String estado) {}

  public record Propiedad(Long id, BigDecimal precio, String tipoOperacion, String estado) {}

  public record Agente(Long id, String estado) {}

  public record Cliente(Long id, String nombres, String email, boolean activo) {}

  public record Orden(
      Long id,
      Long clienteId,
      Long reservaId,
      Long propiedadId,
      BigDecimal monto,
      String moneda,
      String tipo,
      String estado,
      String sagaId,
      String traceId,
      BigDecimal total) {}

  @FeignClient(name = "casa-agente-ms", contextId = "visitaAgente")
  public interface Agentes {
    @GetMapping("/api/v1/agentes/{id}")
    Agente buscar(@PathVariable("id") Long id);
  }

  @FeignClient(name = "casa-reserva-ms", contextId = "sagaReserva")
  public interface Reservas {
    @GetMapping("/api/v1/reservas/{id}")
    Reserva buscar(@PathVariable("id") Long id);
  }

  @FeignClient(name = "casa-propiedad-ms", contextId = "sagaPropiedad")
  public interface Propiedades {
    @GetMapping("/api/v1/propiedades/{id}")
    Propiedad buscar(@PathVariable("id") Long id);
  }

  @FeignClient(name = "casa-cliente-ms", contextId = "sagaCliente")
  public interface Clientes {
    @GetMapping("/api/v1/clientes/{id}")
    Cliente buscar(@PathVariable("id") Long id);
  }

  @FeignClient(name = "casa-orden-ms", contextId = "sagaOrden")
  public interface Ordenes {
    @GetMapping("/api/v1/ordenes/{id}")
    Orden buscar(@PathVariable("id") Long id);
  }
}
