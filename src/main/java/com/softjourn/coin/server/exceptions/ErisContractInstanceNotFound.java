package com.softjourn.coin.server.exceptions;

public class ErisContractInstanceNotFound extends RuntimeException {

    public ErisContractInstanceNotFound(String message) {
        super(message);
    }

    public ErisContractInstanceNotFound(Throwable cause) {
        super(cause);
    }
}
