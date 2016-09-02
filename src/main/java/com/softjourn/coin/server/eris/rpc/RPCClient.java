package com.softjourn.coin.server.eris.rpc;


import java.io.IOException;

/**
 * Interface to make simple Http call with Json payload
 */
public interface RPCClient {

    /**
     * Make call to specified URL with Json payload
     * @param URL Url endpoint to call
     * @param entity entity that will be passed as Json payload
     * @return String representation of response body
     * @throws IOException
     */
    String call(String URL, Object entity) throws IOException;
}
