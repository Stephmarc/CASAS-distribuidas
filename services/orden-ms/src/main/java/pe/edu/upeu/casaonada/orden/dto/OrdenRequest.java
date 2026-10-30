package pe.edu.upeu.casaonada.orden.dto;

import jakarta.validation.constraints.*;
public record OrdenRequest(@NotNull @Positive Long reservaId,@NotBlank @Pattern(regexp="PEN") String moneda){}
