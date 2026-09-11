package pe.edu.upeu.casaonada.notificacion.web;
import pe.edu.upeu.casaonada.notificacion.exception.BusinessRuleException; import pe.edu.upeu.casaonada.notificacion.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest; import org.slf4j.MDC; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.ExceptionHandler; import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant; import java.util.LinkedHashMap; import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
 private ApiError error(HttpStatus status, String msg, HttpServletRequest req) { return new ApiError(Instant.now(),status.value(),status.getReasonPhrase(),msg,req.getRequestURI(),MDC.get("traceId")); }
 @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ApiError> notFound(ResourceNotFoundException ex,HttpServletRequest req) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(HttpStatus.NOT_FOUND,ex.getMessage(),req)); }
 @ExceptionHandler(BusinessRuleException.class) ResponseEntity<ApiError> business(BusinessRuleException ex,HttpServletRequest req) { return ResponseEntity.status(HttpStatus.CONFLICT).body(error(HttpStatus.CONFLICT,ex.getMessage(),req)); }
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Object> validation(MethodArgumentNotValidException ex,HttpServletRequest req) {
  Map<String,String> fields=new LinkedHashMap<>(); for(FieldError f:ex.getBindingResult().getFieldErrors()) fields.put(f.getField(),f.getDefaultMessage());
  Map<String,Object> body=new LinkedHashMap<>(); body.put("timestamp",Instant.now()); body.put("status",400); body.put("error","Bad Request"); body.put("message","Validación fallida"); body.put("fields",fields); body.put("path",req.getRequestURI()); body.put("traceId",MDC.get("traceId"));
  return ResponseEntity.badRequest().body(body);
 }
 @ExceptionHandler(Exception.class) ResponseEntity<ApiError> generic(Exception ex,HttpServletRequest req) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(HttpStatus.INTERNAL_SERVER_ERROR,"Error interno",req)); }
}
