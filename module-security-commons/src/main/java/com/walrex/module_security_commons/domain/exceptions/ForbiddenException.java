package com.walrex.module_security_commons.domain.exceptions;

public class ForbiddenException extends RuntimeException {
    private final String requiredPermission;

    public ForbiddenException(String message, String requiredPermission){
        super(message);
        this.requiredPermission = requiredPermission;
    }

    public String getRequiredPermission(){
        return requiredPermission;
    }
}
