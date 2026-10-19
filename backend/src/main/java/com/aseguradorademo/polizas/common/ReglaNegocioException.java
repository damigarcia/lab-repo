package com.aseguradorademo.polizas.common;

/**
 * Se lanza cuando una operación viola una regla de negocio. Se traduce a HTTP 422.
 */
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
