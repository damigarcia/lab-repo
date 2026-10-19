package com.aseguradorademo.polizas.siniestro;

public final class SiniestroMapper {

    private SiniestroMapper() {
    }

    public static SiniestroResponse toResponse(Siniestro siniestro) {
        return new SiniestroResponse(
                siniestro.getId(),
                siniestro.getPoliza().getId(),
                siniestro.getFechaOcurrencia(),
                siniestro.getFechaReporte(),
                siniestro.getDescripcion(),
                siniestro.getMontoReclamado(),
                siniestro.getEstado());
    }
}
