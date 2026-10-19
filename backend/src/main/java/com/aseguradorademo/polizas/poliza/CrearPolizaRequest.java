package com.aseguradorademo.polizas.poliza;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record CrearPolizaRequest(
        @NotNull Long clienteId,
        @NotNull TipoPoliza tipo,
        @NotNull @DecimalMin(value = "1000.00") @Digits(integer = 12, fraction = 2) BigDecimal sumaAsegurada,
        @NotNull LocalDate fechaInicio) {
}
