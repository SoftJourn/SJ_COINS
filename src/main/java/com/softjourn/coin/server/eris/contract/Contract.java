package com.softjourn.coin.server.eris.contract;


import com.softjourn.coin.server.eris.contract.response.Response;

import java.io.IOException;
import java.util.function.Consumer;

public interface Contract {

    /**
     * Call contract function with passed arguments
     * @param function name of function to be called
     * @param args arguments to pass to called functions(can be zero arguments)
     * @return JSON representation of result of call
     */
    <T> Response<T> call(String function, Object... args) throws IOException;


    void subscribeToUserIn(String address, Consumer<Response> callBack);


}
