package com.softjourn.coin.server.exceptions;

public class AccountEnrollException extends RuntimeException {

    public AccountEnrollException(String message) {
        super(message);
    }

    public AccountEnrollException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountEnrollException(Throwable cause) {
        super(cause);
    }

}
