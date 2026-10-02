package pe.edu.upeu.casaonada.reserva.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Contrato mínimo que reserva-ms necesita de propiedad-ms. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PropiedadDto(
        Long id,
        String titulo,
        String ciudad,
        BigDecimal precio,
        String tipoOperacion,
        String estado,
        Long agenteId
) {}
