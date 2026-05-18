package com.walrex.module_laboratorio.domain.exceptions;

import lombok.Getter;

@Getter
public class CurvaDisenoException extends RuntimeException {
    private final String code;

    public CurvaDisenoException(String message, String code) {
        super(message);
        this.code = code;
    }
}
