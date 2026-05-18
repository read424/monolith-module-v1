package com.walrex.module_laboratorio.domain.exceptions;

import lombok.Getter;

@Getter
public class GamaException extends RuntimeException {
    private final String code;

    public GamaException(String message, String code) {
        super(message);
        this.code = code;
    }
}
