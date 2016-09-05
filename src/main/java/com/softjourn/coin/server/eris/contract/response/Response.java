package com.softjourn.coin.server.eris.contract.response;


import lombok.Data;

@Data
public class Response<T> {

    private final String id;

    private final ReturnValue<T> returnValue;

    private final Error error;

    private final TxParams txParams;

}
