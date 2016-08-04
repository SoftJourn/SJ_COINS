package com.softjourn.coin.server.exceptions;


public class AccountNotFoundException extends RuntimeException{

    public AccountNotFoundException(String account) {
        super("Account\"" + account + "\" not found.");
    }
}
