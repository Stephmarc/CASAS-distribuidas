package pe.edu.upeu.casaonada.cliente.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.cliente.domain.*;
public record ClienteResponse(Long id, String keycloakUserId, String nombres, String apellidos, String email, String telefono, BigDecimal presupuestoMin, BigDecimal presupuestoMax, String ciudadPreferida, String tipoOperacionPreferida, EstadoCliente estado, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
