package com.campusdual.cd2026mnd1g1.model.exception;
public class ServiceRequestNotFoundException extends RuntimeException {
    public ServiceRequestNotFoundException(Long id) { super("No existe la solicitud con id " + id); }
}
