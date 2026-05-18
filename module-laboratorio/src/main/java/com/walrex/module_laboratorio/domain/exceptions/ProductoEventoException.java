package com.walrex.module_laboratorio.domain.exceptions;

import lombok.Getter;

@Getter
public class ProductoEventoException extends RuntimeException {
    private final String code;

    public ProductoEventoException(String message, String code) {
        super(message);
        this.code = code;
    }
}
