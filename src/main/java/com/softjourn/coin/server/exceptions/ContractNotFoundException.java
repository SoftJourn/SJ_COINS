package com.softjourn.coin.server.exceptions;

public class ContractNotFoundException extends RuntimeException {

    public ContractNotFoundException(String message) {
        super(message);
    }

    public ContractNotFoundException(Throwable cause) {
        super(cause);
    }
}
