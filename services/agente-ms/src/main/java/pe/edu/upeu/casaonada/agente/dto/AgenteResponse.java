package pe.edu.upeu.casaonada.agente.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.agente.domain.*;
public record AgenteResponse(Long id, String keycloakUserId, String nombres, String apellidos, String email, String telefono, String matricula, EstadoAgente estado, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
