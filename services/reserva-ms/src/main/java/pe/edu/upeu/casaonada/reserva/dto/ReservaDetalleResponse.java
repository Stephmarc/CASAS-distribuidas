package pe.edu.upeu.casaonada.reserva.dto;

public record ReservaDetalleResponse(
        ReservaResponse reserva,
        PropiedadDto propiedad,
        boolean degradado,
        String mensaje
) {}
