package pe.edu.upeu.casaonada.contrato.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.contrato.domain.*;
public record ContratoResponse(Long id, Long ordenId, Long clienteId, Long propiedadId, TipoContrato tipo, OffsetDateTime fechaInicio, OffsetDateTime fechaFin, BigDecimal monto, Integer version, EstadoContrato estado, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
