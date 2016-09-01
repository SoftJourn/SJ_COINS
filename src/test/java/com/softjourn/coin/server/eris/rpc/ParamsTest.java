package com.softjourn.coin.server.eris.rpc;

import junit.framework.TestCase;

import java.util.Map;

public class ParamsTest extends TestCase {
    public void testConstantCallParams() throws Exception {
        Map<String, Object> params = Params.constantCallParams("1111", "adr", "data");

        assertTrue(params.containsKey("from"));
        assertTrue(params.containsKey("address"));
        assertTrue(params.containsKey("data"));
    }

    public void testTransactionalCallParams() throws Exception {
        Map<String, Object> params = Params.transactionalCallParams("1111", "adr", "data");

        assertTrue(params.containsKey("priv_key"));
        assertTrue(params.containsKey("address"));
        assertTrue(params.containsKey("data"));
        assertTrue(params.containsKey("gas_limit"));

    }

}