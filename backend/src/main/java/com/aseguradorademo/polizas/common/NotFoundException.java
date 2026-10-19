package com.aseguradorademo.polizas.common;

/**
 * Se lanza cuando un recurso solicitado no existe. Se traduce a HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String recurso, Object id) {
        super(recurso + " con id " + id + " no existe");
    }
}
