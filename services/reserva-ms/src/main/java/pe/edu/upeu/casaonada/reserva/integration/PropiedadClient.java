package pe.edu.upeu.casaonada.reserva.integration;

import pe.edu.upeu.casaonada.reserva.dto.PropiedadDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Comunicación síncrona por nombre lógico registrado en Eureka. */
@FeignClient(name = "casa-propiedad-ms", configuration = FeignPropagationConfig.class)
public interface PropiedadClient {
    @GetMapping("/api/v1/propiedades/{id}")
    PropiedadDto buscarPorId(@PathVariable("id") Long id);
}
