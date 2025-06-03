package com.walrex.module_almacen.common.Exception;

public class OrdenIngresoException extends RuntimeException {
    public OrdenIngresoException(String message) {
        super(message);
    }

    public OrdenIngresoException(String message, Throwable cause) {
        super(message, cause);
    }
}
