package com.walrex.user.module_users.domain.exception;

public class EmailNotFoundException extends RuntimeException {
    /**
     * Constructor por defecto
     */
    public EmailNotFoundException() {
        super("El email proporcionado no existe en el sistema");
    }

    /**
     * Constructor con mensaje personalizado
     *
     * @param message Mensaje de error detallado
     */
    public EmailNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa
     *
     * @param message Mensaje de error detallado
     * @param cause Causa raíz de la excepción
     */
    public EmailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
