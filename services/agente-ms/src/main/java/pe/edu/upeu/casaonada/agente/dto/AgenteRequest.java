package pe.edu.upeu.casaonada.agente.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.agente.domain.*;
public record AgenteRequest(String keycloakUserId, @jakarta.validation.constraints.NotBlank String nombres, @jakarta.validation.constraints.NotBlank String apellidos, @jakarta.validation.constraints.NotBlank String email, String telefono, @jakarta.validation.constraints.NotBlank String matricula, @jakarta.validation.constraints.NotNull EstadoAgente estado) {}
