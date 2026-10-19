package com.aseguradorademo.polizas.common;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Formato estándar de error de la API. Todos los errores se devuelven con esta forma.
 */
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String codigo,
        String mensaje,
        List<String> detalles) {

    public static ErrorResponse of(int status, String codigo, String mensaje) {
        return new ErrorResponse(OffsetDateTime.now(), status, codigo, mensaje, List.of());
    }

    public static ErrorResponse of(int status, String codigo, String mensaje, List<String> detalles) {
        return new ErrorResponse(OffsetDateTime.now(), status, codigo, mensaje, detalles);
    }
}
