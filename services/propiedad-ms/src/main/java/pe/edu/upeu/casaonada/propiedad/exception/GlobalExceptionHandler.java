package pe.edu.upeu.casaonada.propiedad.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PropiedadNotFoundException.class)
    ProblemDetail notFound(PropiedadNotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST,
                "Uno o más campos no cumplen las reglas de validación", req.getRequestURI());
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail optimisticLock(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT,
                "La propiedad fue modificada por otra operación. Vuelve a consultar su versión.",
                req.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail dataConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT,
                "La operación viola una restricción de integridad de datos", req.getRequestURI());
    }

    private ProblemDetail problem(HttpStatus status, String message, String path) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(status.getReasonPhrase());
        detail.setInstance(URI.create(path));
        return detail;
    }
}
