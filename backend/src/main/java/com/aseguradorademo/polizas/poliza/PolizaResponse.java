package com.aseguradorademo.polizas.poliza;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaResponse(
        Long id,
        String numero,
        Long clienteId,
        String clienteNombre,
        TipoPoliza tipo,
        EstadoPoliza estado,
        BigDecimal sumaAsegurada,
        BigDecimal primaAnual,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String motivoCancelacion,
        boolean enPeriodoDeGracia) {
}
