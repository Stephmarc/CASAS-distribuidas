package pe.edu.upeu.casaonada.notificacion.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.notificacion.domain.*;
public record NotificacionResponse(Long id, String destinatario, TipoNotificacion tipo, String asunto, String contenido, EstadoNotificacion estado, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
