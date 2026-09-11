package pe.edu.upeu.casaonada.notificacion.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.notificacion.domain.*;
public record NotificacionRequest(@jakarta.validation.constraints.NotBlank String destinatario, @jakarta.validation.constraints.NotNull TipoNotificacion tipo, @jakarta.validation.constraints.NotBlank String asunto, @jakarta.validation.constraints.NotBlank String contenido, @jakarta.validation.constraints.NotNull EstadoNotificacion estado) {}
