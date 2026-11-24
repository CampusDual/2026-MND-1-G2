package com.campusdual.cd2026mnd1g1.model.exception;

public class StockInsufficientException extends RuntimeException {

    /**
     * Crea una excepcion cuando no hay unidades suficientes para asignar una solicitud.
     *
     * @param productName nombre del producto
     * @param available unidades disponibles
     * @param requested unidades solicitadas
     */
    public StockInsufficientException(String productName, int available, int requested) {
        super("Stock insuficiente para el producto " + productName
                + ": disponibles " + available + ", solicitadas " + requested);
    }
}
