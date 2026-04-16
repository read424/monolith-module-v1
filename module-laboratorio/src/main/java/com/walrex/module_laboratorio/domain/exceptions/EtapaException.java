package com.walrex.module_laboratorio.domain.exceptions;

import lombok.Getter;

@Getter
public class EtapaException extends RuntimeException {
    private final String code;

    public EtapaException(String message, String code) {
        super(message);
        this.code = code;
    }
}
