package com.softjourn.coin.server.exceptions;

public class ErisContractNotAllowedToCreate extends RuntimeException {

    public ErisContractNotAllowedToCreate(String message) {
        super(message);
    }

    public ErisContractNotAllowedToCreate(Throwable cause) {
        super(cause);
    }
}
