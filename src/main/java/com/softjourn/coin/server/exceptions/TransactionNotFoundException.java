package com.softjourn.coin.server.exceptions;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(Long transaction) {
        super("Transaction\"" + transaction + "\" not found.");
    }

    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
