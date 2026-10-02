package pe.edu.upeu.casaonada.reserva.dto;

public record PropiedadConsultaResult(
        PropiedadDto propiedad,
        boolean degradado,
        String mensaje
) {}
