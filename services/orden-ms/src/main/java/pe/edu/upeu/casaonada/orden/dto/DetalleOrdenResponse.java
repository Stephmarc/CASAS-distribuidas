package pe.edu.upeu.casaonada.orden.dto;
import java.math.BigDecimal;
public record DetalleOrdenResponse(Long id,Long propiedadId,String concepto,BigDecimal precioAcordado) {}
