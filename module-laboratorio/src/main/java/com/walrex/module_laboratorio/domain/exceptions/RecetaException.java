package com.walrex.module_laboratorio.domain.exceptions;

import lombok.Getter;

@Getter
public class RecetaException extends RuntimeException {
    private final String code;

    public RecetaException(String message, String code) {
        super(message);
        this.code = code;
    }
}
