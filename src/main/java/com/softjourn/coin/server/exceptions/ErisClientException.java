package com.softjourn.coin.server.exceptions;

/**
 * Thrown when Eris Client can't get correct information
 * Created by vromanchuk on 19.01.17.
 */
public class ErisClientException extends Exception {
    public ErisClientException() {
    }

    public ErisClientException(String s) {
        super(s);
    }

    public ErisClientException(Throwable throwable) {
        super(throwable);
    }
}
