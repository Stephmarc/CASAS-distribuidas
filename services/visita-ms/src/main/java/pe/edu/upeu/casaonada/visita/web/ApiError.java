package pe.edu.upeu.casaonada.visita.web;
import java.time.Instant;
public record ApiError(Instant timestamp, int status, String error, String message, String path, String traceId) {}
