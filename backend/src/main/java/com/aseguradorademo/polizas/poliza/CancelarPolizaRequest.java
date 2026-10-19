package com.aseguradorademo.polizas.poliza;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarPolizaRequest(
        @NotBlank @Size(max = 255) String motivo) {
}
