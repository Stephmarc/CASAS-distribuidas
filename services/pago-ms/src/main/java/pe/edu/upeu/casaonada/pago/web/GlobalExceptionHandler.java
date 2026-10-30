package pe.edu.upeu.casaonada.pago.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.upeu.casaonada.pago.exception.BusinessRuleException;
import pe.edu.upeu.casaonada.pago.exception.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private ApiError error(HttpStatus status, String msg, HttpServletRequest req) {
    return new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        msg,
        req.getRequestURI(),
        MDC.get("traceId"));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(error(HttpStatus.NOT_FOUND, ex.getMessage(), req));
  }

  @ExceptionHandler(BusinessRuleException.class)
  ResponseEntity<ApiError> business(BusinessRuleException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(error(HttpStatus.CONFLICT, ex.getMessage(), req));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Object> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError f : ex.getBindingResult().getFieldErrors())
      fields.put(f.getField(), f.getDefaultMessage());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", 400);
    body.put("error", "Bad Request");
    body.put("message", "Validación fallida");
    body.put("fields", fields);
    body.put("path", req.getRequestURI());
    body.put("traceId", MDC.get("traceId"));
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
  ResponseEntity<Object> status(
      org.springframework.web.server.ResponseStatusException ex, HttpServletRequest req) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(
            java.util.Map.of(
                "status",
                ex.getStatusCode().value(),
                "message",
                java.util.Objects.toString(ex.getReason(), "Solicitud rechazada"),
                "path",
                req.getRequestURI()));
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  ResponseEntity<Object> duplicate(
      org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest req) {
    return ResponseEntity.status(409)
        .body(
            java.util.Map.of(
                "status",
                409,
                "message",
                "Conflicto de datos o solicitud concurrente. Consulte la operacion existente."));
  }

  @ExceptionHandler({
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingRequestHeaderException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class,
    jakarta.validation.ConstraintViolationException.class
  })
  ResponseEntity<Object> malformed(Exception ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "status",
                400,
                "message",
                "Solicitud incompleta o con formato invalido",
                "path",
                req.getRequestURI()));
  }

  @ExceptionHandler({
    org.springframework.dao.OptimisticLockingFailureException.class,
    org.springframework.dao.PessimisticLockingFailureException.class
  })
  ResponseEntity<Object> concurrent(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(409)
        .body(
            Map.of(
                "status",
                409,
                "message",
                "La operacion cambio concurrentemente; consulte su estado y reintente"));
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  ResponseEntity<Object> method(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(405).body(Map.of("status", 405, "message", "Metodo no permitido"));
  }

  @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
  ResponseEntity<Object> media(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(415)
        .body(Map.of("status", 415, "message", "Utilice Content-Type application/json"));
  }

  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  ResponseEntity<Object> missing(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(404)
        .body(Map.of("status", 404, "message", "Recurso no encontrado"));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<Object> invalid(IllegalArgumentException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "status",
                400,
                "message",
                java.util.Objects.toString(ex.getMessage(), "Datos invalidos")));
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  ResponseEntity<Object> denied(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(403)
        .body(Map.of("status", 403, "message", "No tiene permiso para esta operacion"));
  }

  @ExceptionHandler({
    org.springframework.transaction.CannotCreateTransactionException.class,
    org.springframework.dao.DataAccessResourceFailureException.class,
    org.springframework.dao.QueryTimeoutException.class
  })
  ResponseEntity<Object> unavailable(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(503)
        .header("Retry-After", "3")
        .body(
            Map.of(
                "status",
                503,
                "message",
                "Dependencia temporalmente no disponible; consulte el estado antes de reintentar"));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
    org.slf4j.LoggerFactory.getLogger(getClass())
        .error(
            "Error no controlado traceId={} path={}", MDC.get("traceId"), req.getRequestURI(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", req));
  }
}
