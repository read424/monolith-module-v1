package com.walrex.module_partidas.domain.exceptions;


public class WebSocketNotificationException extends RuntimeException {

    public WebSocketNotificationException(String message) {
        super(message);
    }

    public WebSocketNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
