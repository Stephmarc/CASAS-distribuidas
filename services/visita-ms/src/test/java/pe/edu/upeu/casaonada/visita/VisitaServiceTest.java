package pe.edu.upeu.casaonada.visita;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import pe.edu.upeu.casaonada.shared.*;
import pe.edu.upeu.casaonada.visita.domain.*;
import pe.edu.upeu.casaonada.visita.dto.VisitaRequest;
import pe.edu.upeu.casaonada.visita.mapper.VisitaMapper;
import pe.edu.upeu.casaonada.visita.repository.VisitaRepository;
import pe.edu.upeu.casaonada.visita.service.VisitaService;

class VisitaServiceTest {
  VisitaRepository repo;
  ConsultasRemotas c;
  VisitaService service;
  OffsetDateTime future;

  @BeforeEach
  void setup() {
    repo = mock(VisitaRepository.class);
    c = mock(ConsultasRemotas.class);
    service = new VisitaService(repo, new VisitaMapper(), c);
    future = OffsetDateTime.now().plusDays(1);
    when(c.cliente(1L)).thenReturn(new CasaClients.Cliente(1L, "Cliente", "c@c.pe", true));
    when(c.agente(1L)).thenReturn(new CasaClients.Agente(1L, "ACTIVO"));
    when(c.propiedad(1L))
        .thenReturn(
            new CasaClients.Propiedad(1L, new BigDecimal("100000"), "COMPRA", "DISPONIBLE"));
    when(repo.findByFechaHoraAndEstadoIn(any(), any())).thenReturn(List.of());
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  VisitaRequest request(OffsetDateTime date) {
    return new VisitaRequest(1L, 1L, 1L, date, null, EstadoVisita.CONFIRMADA);
  }

  @Test
  void clienteNoConfirmaLaVisitaAlCrearla() {
    assertEquals(EstadoVisita.SOLICITADA, service.crear(request(future)).estado());
  }

  @Test
  void noProgramaEnPasado() {
    assertThrows(
        RuntimeException.class, () -> service.crear(request(OffsetDateTime.now().minusHours(1))));
    verify(repo, never()).save(any());
  }

  @Test
  void noAceptaAgenteInactivo() {
    when(c.agente(1L)).thenReturn(new CasaClients.Agente(1L, "INACTIVO"));
    assertThrows(RuntimeException.class, () -> service.crear(request(future)));
  }

  @Test
  void noAceptaInmuebleVendido() {
    when(c.propiedad(1L))
        .thenReturn(new CasaClients.Propiedad(1L, new BigDecimal("100000"), "COMPRA", "VENDIDA"));
    assertThrows(RuntimeException.class, () -> service.crear(request(future)));
  }

  @Test
  void noDuplicaHorarioDeAgente() {
    var v = new Visita();
    v.setId(5L);
    v.setAgenteId(1L);
    v.setClienteId(2L);
    when(repo.findByFechaHoraAndEstadoIn(any(), any())).thenReturn(List.of(v));
    assertThrows(RuntimeException.class, () -> service.crear(request(future)));
  }

  @Test
  void noMarcaUnaVisitaFuturaComoRealizada() {
    var v = new Visita();
    v.setEstado(EstadoVisita.CONFIRMADA);
    v.setFechaHora(future);
    when(repo.locked(1L)).thenReturn(Optional.of(v));
    assertThrows(RuntimeException.class, () -> service.cambiarEstado(1L, EstadoVisita.REALIZADA));
  }
}
