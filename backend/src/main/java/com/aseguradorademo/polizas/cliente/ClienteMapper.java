package com.aseguradorademo.polizas.cliente;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getFechaNacimiento());
    }
}
