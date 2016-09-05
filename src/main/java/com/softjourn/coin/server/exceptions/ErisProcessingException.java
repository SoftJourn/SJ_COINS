package com.softjourn.coin.server.exceptions;

public class ErisProcessingException extends RuntimeException {

    public ErisProcessingException(String message) {
        super(message);
    }

    public ErisProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
