package com.softjourn.coin.server.exceptions;

public class NotEnoughAmountInTreasuryException extends RuntimeException {

    public NotEnoughAmountInTreasuryException(String message) {
        super(message);
    }
}
