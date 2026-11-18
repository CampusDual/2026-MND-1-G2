package com.campusdual.cd2026mnd1g1.model.exception;

public class RequestConflictException extends RuntimeException {

    /**
     * Crea una excepcion de conflicto para cambios concurrentes o estados invalidos.
     *
     * @param message descripcion del conflicto
     */
    public RequestConflictException(String message) {
        super(message);
    }
}
