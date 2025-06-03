package com.walrex.module_almacen.domain.model.exceptions;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String message) {
        super(message);
    }

    public StockInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}
