package com.softjourn.coin.server.eris.rpc;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;

/**
 * Interface to make simple Http call with Json payload
 */
public interface RPCClient {

    default String makeRequestBody(Object entity) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer();
            return writer.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            //should newer happened
            throw new RuntimeException("Can't write request entity as JSON.", e);
        }
    }

    /**
     * Make call to specified URL with Json payload
     * @param URL Url endpoint to call
     * @param entity entity that will be passed as Json payload
     * @return String representation of response body
     * @throws IOException
     */
    String call(String URL, Object entity) throws IOException;
}
