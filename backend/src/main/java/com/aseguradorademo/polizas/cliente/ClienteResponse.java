package com.aseguradorademo.polizas.cliente;

import java.time.LocalDate;

public record ClienteResponse(
        Long id,
        String nombreCompleto,
        String documentoIdentidad,
        String email,
        String telefono,
        LocalDate fechaNacimiento) {
}
