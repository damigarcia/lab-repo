package com.aseguradorademo.polizas.siniestro;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SiniestroResponse(
        Long id,
        Long polizaId,
        LocalDate fechaOcurrencia,
        LocalDate fechaReporte,
        String descripcion,
        BigDecimal montoReclamado,
        EstadoSiniestro estado) {
}
