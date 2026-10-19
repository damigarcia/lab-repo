package com.aseguradorademo.polizas.poliza;

import java.math.BigDecimal;

/**
 * Tipos de póliza comercializados. La tasa base se aplica sobre la suma asegurada
 * para obtener la prima anual (ver docs/REGLAS_NEGOCIO.md).
 */
public enum TipoPoliza {
    AUTO(new BigDecimal("0.0400")),
    HOGAR(new BigDecimal("0.0050")),
    VIDA(new BigDecimal("0.0120"));

    private final BigDecimal tasaBase;

    TipoPoliza(BigDecimal tasaBase) {
        this.tasaBase = tasaBase;
    }

    public BigDecimal getTasaBase() {
        return tasaBase;
    }
}
