package pe.edu.upeu.casaonada.pago;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.casaonada.pago.domain.*;
import pe.edu.upeu.casaonada.pago.dto.*;
import pe.edu.upeu.casaonada.pago.integration.MercadoPagoGateway;
import pe.edu.upeu.casaonada.pago.repository.PagoRepository;
import pe.edu.upeu.casaonada.pago.service.*;
import pe.edu.upeu.casaonada.shared.*;

class PagoSagaTest {
  jakarta.persistence.EntityManager em;
  org.springframework.beans.factory.ObjectProvider<MercadoPagoGateway> provider;
  PagoRepository repo;
  PagoSagaGuard guard;
  Outbox outbox;
  ConsultasRemotas consultas;
  PagoService service;
  OperacionEvent event;
  Pago pago;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setup() {
    repo = mock(PagoRepository.class);
    guard = mock(PagoSagaGuard.class);
    outbox = mock(Outbox.class);
    consultas = mock(ConsultasRemotas.class);
    provider = (ObjectProvider<MercadoPagoGateway>) mock(ObjectProvider.class);
    em = mock(jakarta.persistence.EntityManager.class);
    service =
        new PagoService(
            repo, provider, consultas, guard, outbox, true, em, mock(IdempotencyLocks.class));
    event =
        new OperacionEvent(
            "event",
            "saga",
            "REEMBOLSAR_PAGO",
            1L,
            1L,
            1L,
            1L,
            new BigDecimal("500"),
            "PEN",
            "COMPRA",
            "trace",
            null,
            new BigDecimal("100000"));
    pago = new Pago();
    pago.setOrdenId(1L);
    pago.setProveedor("SANDBOX_LOCAL");
    pago.setMonto(new BigDecimal("500"));
    pago.setMoneda("PEN");
    pago.setEstado(EstadoPago.PENDIENTE);
    when(repo.findById(1L)).thenReturn(Optional.of(pago));
    when(guard.context(1L)).thenReturn(event);
    when(repo.findByOrdenId(1L)).thenReturn(List.of(pago));
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "admin", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void reembolsaAprobado() {
    pago.setEstado(EstadoPago.APROBADO);
    service.compensar(event);
    assertEquals(EstadoPago.REEMBOLSADO, pago.getEstado());
    verify(guard).cancel(event);
  }

  @Test
  void cancelaPendiente() {
    service.compensar(event);
    assertEquals(EstadoPago.CANCELADO, pago.getEstado());
  }

  @Test
  void compensarDosVecesNoCobra() {
    pago.setEstado(EstadoPago.REEMBOLSADO);
    service.compensar(event);
    assertEquals(EstadoPago.REEMBOLSADO, pago.getEstado());
  }

  @Test
  void pagoTardioTrasCancelacionEsRechazado() {
    when(guard.lock(event)).thenReturn(true);
    assertThrows(ResponseStatusException.class, () -> service.simular(1L, EstadoPago.APROBADO));
    assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
  }

  @Test
  void simuladorNoAlteraProveedorExterno() {
    pago.setProveedor("MERCADO_PAGO");
    assertThrows(ResponseStatusException.class, () -> service.simular(1L, EstadoPago.APROBADO));
  }

  @Test
  void simulacionDuplicadaEsIdempotente() {
    pago.setEstado(EstadoPago.APROBADO);
    service.simular(1L, EstadoPago.APROBADO);
    verifyNoInteractions(outbox);
  }

  @Test
  void montoAlteradoNoSePaga() {
    when(repo.findByIdempotencyKey("key")).thenReturn(Optional.empty());
    when(consultas.orden(1L))
        .thenReturn(
            new CasaClients.Orden(
                1L,
                1L,
                1L,
                1L,
                new BigDecimal("500"),
                "PEN",
                "COMPRA",
                "ESPERANDO_PAGO",
                "saga",
                "trace",
                new BigDecimal("100000")));
    var req =
        new PagoRequest(
            1L, new BigDecimal("1"), "PEN", "Adelanto", "cliente@casa.local", "visa", null, 1);
    assertThrows(ResponseStatusException.class, () -> service.crear(req, "key"));
    verify(repo, never()).saveAndFlush(any());
  }

  @Test
  void recargaEstadoDespuesDeBloquear() {
    doAnswer(
            i -> {
              pago.setEstado(EstadoPago.CANCELADO);
              return null;
            })
        .when(em)
        .refresh(eq(pago), eq(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE));
    assertThrows(ResponseStatusException.class, () -> service.simular(1L, EstadoPago.APROBADO));
    verifyNoInteractions(outbox);
  }

  @Test
  void cobroInciertoNoSeDaPorReembolsado() {
    pago.setProveedor("MERCADO_PAGO");
    pago.setEstado(EstadoPago.CREADO);
    pago.setExternalReference("casa-ref");
    pago.setSubmissionStartedAt(java.time.OffsetDateTime.now());
    var gateway = mock(MercadoPagoGateway.class);
    when(provider.getObject()).thenReturn(gateway);
    when(gateway.buscarPorReferencia(anyString(), any())).thenReturn(Optional.empty());
    assertThrows(IllegalStateException.class, () -> service.compensar(event));
    verifyNoInteractions(outbox);
    assertEquals(EstadoPago.CREADO, pago.getEstado());
  }

  @Test
  void intencionNuncaEnviadaSeCancelaSinLlamarAlProveedor() {
    pago.setProveedor("MERCADO_PAGO");
    pago.setEstado(EstadoPago.CREADO);
    pago.setExternalReference("casa-ref");
    service.compensar(event);
    assertEquals(EstadoPago.CANCELADO, pago.getEstado());
    verifyNoInteractions(provider);
  }

  @Test
  void conciliacionNoResucitaPagoCancelado() {
    pago.setProveedor("MERCADO_PAGO");
    pago.setEstado(EstadoPago.CANCELADO);
    service.reconciliar(1L);
    verifyNoInteractions(provider);
    assertEquals(EstadoPago.CANCELADO, pago.getEstado());
  }
}
