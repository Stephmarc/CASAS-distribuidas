package pe.edu.upeu.casaonada.notificacion.web;
import java.time.Instant;
public record ApiError(Instant timestamp, int status, String error, String message, String path, String traceId) {}
