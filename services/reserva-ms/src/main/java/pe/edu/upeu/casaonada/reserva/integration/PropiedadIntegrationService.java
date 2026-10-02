package pe.edu.upeu.casaonada.reserva.integration;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import pe.edu.upeu.casaonada.reserva.dto.PropiedadConsultaResult;
import pe.edu.upeu.casaonada.reserva.dto.PropiedadDto;
import pe.edu.upeu.casaonada.reserva.exception.DependencyUnavailableException;
import pe.edu.upeu.casaonada.reserva.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PropiedadIntegrationService {
    private static final Logger log = LoggerFactory.getLogger(PropiedadIntegrationService.class);
    private final PropiedadClient client;

    public PropiedadIntegrationService(PropiedadClient client) {
        this.client = client;
    }

    /**
     * Operación usada al crear una reserva: si propiedad-ms no responde,
     * la operación no continúa y devuelve un error controlado 503.
     */
    @CircuitBreaker(name = "propiedad", fallbackMethod = "fallbackValidarReservable")
    public PropiedadDto validarReservable(Long propiedadId) {
        return client.buscarPorId(propiedadId);
    }

    public PropiedadDto fallbackValidarReservable(Long propiedadId, Throwable ex) {
        if (ex instanceof FeignException.NotFound) {
            throw new ResourceNotFoundException("Propiedad no encontrada: " + propiedadId);
        }
        log.warn("Fallback de propiedad-ms al validar la propiedad {}: {}", propiedadId, ex.toString());
        throw new DependencyUnavailableException(
                "No se pudo validar la propiedad porque propiedad-ms no está disponible temporalmente", ex);
    }

    /**
     * Consulta enriquecida: si la dependencia falla, mantiene disponible la reserva
     * y responde en modo degradado, haciendo visible el fallback para la evidencia S06.
     */
    @CircuitBreaker(name = "propiedad", fallbackMethod = "fallbackConsultar")
    public PropiedadConsultaResult consultar(Long propiedadId) {
        return new PropiedadConsultaResult(
                client.buscarPorId(propiedadId),
                false,
                "Propiedad consultada correctamente desde propiedad-ms"
        );
    }

    public PropiedadConsultaResult fallbackConsultar(Long propiedadId, Throwable ex) {
        if (ex instanceof FeignException.NotFound) {
            return new PropiedadConsultaResult(null, true,
                    "La reserva existe, pero la propiedad " + propiedadId + " no fue encontrada");
        }
        log.warn("Respuesta degradada para propiedad {}: {}", propiedadId, ex.toString());
        return new PropiedadConsultaResult(null, true,
                "Servicio de propiedades temporalmente no disponible; se devuelve la reserva sin datos de propiedad");
    }
}
