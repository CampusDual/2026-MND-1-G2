package com.campusdual.cd2026mnd1g1.model.exception;

public class WorkerNotFoundException extends RuntimeException {

    /**
     * Crea una excepcion cuando el usuario autenticado no tiene perfil de trabajador.
     *
     * @param username nombre de usuario autenticado
     */
    public WorkerNotFoundException(String username) {
        super("El usuario " + username + " no tiene un perfil de trabajador");
    }
}
