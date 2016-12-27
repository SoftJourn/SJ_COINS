package com.softjourn.coin.server.exceptions;


public class ErisAccountNotFoundException extends RuntimeException {

    public ErisAccountNotFoundException() {
        super("Can't create new eris account.");
    }

    public ErisAccountNotFoundException(String message) {
        super(message);
    }
}
