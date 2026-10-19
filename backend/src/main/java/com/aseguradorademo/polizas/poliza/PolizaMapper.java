package com.aseguradorademo.polizas.poliza;

public final class PolizaMapper {

    private PolizaMapper() {
    }

    public static PolizaResponse toResponse(Poliza poliza, boolean enPeriodoDeGracia) {
        return new PolizaResponse(
                poliza.getId(),
                poliza.getNumero(),
                poliza.getCliente().getId(),
                poliza.getCliente().getNombreCompleto(),
                poliza.getTipo(),
                poliza.getEstado(),
                poliza.getSumaAsegurada(),
                poliza.getPrimaAnual(),
                poliza.getFechaInicio(),
                poliza.getFechaFin(),
                poliza.getMotivoCancelacion(),
                enPeriodoDeGracia);
    }
}
