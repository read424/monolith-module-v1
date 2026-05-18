package com.walrex.module_machines.domain.exceptions;

import lombok.Getter;

@Getter
public class MaquinaException extends RuntimeException {
    private final String code;

    public MaquinaException(String message, String code) {
        super(message);
        this.code = code;
    }
}
