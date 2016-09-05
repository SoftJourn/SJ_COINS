package com.softjourn.coin.server.exceptions;

/**
 * Created by volodymyr on 9/5/16.
 */
public class ErisRootAccountOverFlow extends RuntimeException {
    public ErisRootAccountOverFlow() {
        super("Property file have more root accounts that in JSON file");
    }
}
