package com.aseguradorademo.polizas.siniestro;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record CrearSiniestroRequest(
        @NotNull Long polizaId,
        @NotNull @PastOrPresent LocalDate fechaOcurrencia,
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 12, fraction = 2) BigDecimal montoReclamado) {
}
