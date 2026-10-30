package pe.edu.upeu.casaonada.shared;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConsultasRemotas {
  private final CasaClients.Agentes agentes;
  private final CasaClients.Reservas reservas;
  private final CasaClients.Propiedades propiedades;
  private final CasaClients.Clientes clientes;
  private final CasaClients.Ordenes ordenes;

  public ConsultasRemotas(
      CasaClients.Reservas r,
      CasaClients.Propiedades p,
      CasaClients.Clientes c,
      CasaClients.Ordenes o,
      CasaClients.Agentes a) {
    agentes = a;
    reservas = r;
    propiedades = p;
    clientes = c;
    ordenes = o;
  }

  @CircuitBreaker(name = "reserva", fallbackMethod = "reservaFallback")
  public CasaClients.Reserva reserva(Long id) {
    return reservas.buscar(id);
  }

  @CircuitBreaker(name = "propiedad", fallbackMethod = "propiedadFallback")
  public CasaClients.Propiedad propiedad(Long id) {
    return propiedades.buscar(id);
  }

  @CircuitBreaker(name = "cliente", fallbackMethod = "clienteFallback")
  public CasaClients.Cliente cliente(Long id) {
    return clientes.buscar(id);
  }

  @CircuitBreaker(name = "orden", fallbackMethod = "ordenFallback")
  public CasaClients.Orden orden(Long id) {
    return ordenes.buscar(id);
  }

  @CircuitBreaker(name = "agente", fallbackMethod = "agenteFallback")
  public CasaClients.Agente agente(Long id) {
    return agentes.buscar(id);
  }

  public CasaClients.Agente agenteFallback(Long id, Throwable e) {
    throw error(e);
  }

  public CasaClients.Reserva reservaFallback(Long id, Throwable e) {
    throw error(e);
  }

  public CasaClients.Propiedad propiedadFallback(Long id, Throwable e) {
    throw error(e);
  }

  public CasaClients.Cliente clienteFallback(Long id, Throwable e) {
    throw error(e);
  }

  public CasaClients.Orden ordenFallback(Long id, Throwable e) {
    throw error(e);
  }

  private RuntimeException error(Throwable e) {
    if (e instanceof FeignException f && f.status() >= 400 && f.status() < 500)
      return new ResponseStatusException(
          HttpStatus.valueOf(f.status()), "Solicitud remota rechazada");
    return new ResponseStatusException(
        HttpStatus.SERVICE_UNAVAILABLE, "Dependencia temporalmente no disponible");
  }
}
