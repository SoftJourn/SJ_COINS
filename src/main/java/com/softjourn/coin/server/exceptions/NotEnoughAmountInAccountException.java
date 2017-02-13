package com.softjourn.coin.server.exceptions;


public class NotEnoughAmountInAccountException extends RuntimeException {

    public NotEnoughAmountInAccountException() {
        super("Not enough amount of coins in account to make this body.");
    }

}
