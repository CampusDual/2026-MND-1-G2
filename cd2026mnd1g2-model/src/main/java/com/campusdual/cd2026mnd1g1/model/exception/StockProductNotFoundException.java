package com.campusdual.cd2026mnd1g1.model.exception;

public class StockProductNotFoundException extends RuntimeException {
    public StockProductNotFoundException(Long productId) {
        super("No se ha encontrado el producto de stock " + productId);
    }
}
