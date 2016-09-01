package com.softjourn.coin.server.eris.contract;

import com.softjourn.coin.server.eris.contract.response.Error;
import com.softjourn.coin.server.eris.contract.response.Response;
import com.softjourn.coin.server.eris.contract.response.ReturnValue;
import com.softjourn.coin.server.eris.contract.response.TxParams;
import com.softjourn.coin.server.eris.contract.types.Uint;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class ResponseParserTest {

    private Response<BigDecimal> txResponse;

    private Response txResponseWithRetValue;

    private Response<BigDecimal> valResponse;

    private Response errorResponse;

    private ResponseParser parser;

    private final String valResponseString = "{\n" +
            "    \"result\": {\n" +
            "      \"return\": \"0000000000000000000000000000000000000000000000000000000000000064\",\n" +
            "      \"gas_used\": 0\n" +
            "    },\n" +
            "    \"error\": null,\n" +
            "    \"id\": \"\",\n" +
            "    \"jsonrpc\": \"2.0\"\n" +
            "  }";

    private final String txResponseSring = "{\n" +
            "    \"result\": [\n" +
            "      4,\n" +
            "      {\n" +
            "        \"call_data\": {\n" +
            "          \"caller\": \"1ADA404B3EEDD5CC971475489A17BAACB9BA5D68\",\n" +
            "          \"callee\": \"628FF6F4BB9C68A8BF9C2BBE04242C4B7FAE6A19\",\n" +
            "          \"data\": \"D0679D340000000000000000000000007EA0B39261842504023A40858099700E061563CE0000000000000000000000000000000000000000000000000000000000000001\",\n" +
            "          \"value\": 1,\n" +
            "          \"gas\": 9701\n" +
            "        },\n" +
            "        \"origin\": \"1ADA404B3EEDD5CC971475489A17BAACB9BA5D68\",\n" +
            "        \"tx_id\": \"8993486AB880DB2144A58989B4E3D72F9656246D\",\n" +
            "        \"return\": \"\",\n" +
            "        \"exception\": \"\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"error\": null,\n" +
            "    \"id\": \"\",\n" +
            "    \"jsonrpc\": \"2.0\"\n" +
            "  }";

    private final String txResponseWithRetValueSring = "{\n" +
            "    \"result\": [\n" +
            "      4,\n" +
            "      {\n" +
            "        \"call_data\": {\n" +
            "          \"caller\": \"1ADA404B3EEDD5CC971475489A17BAACB9BA5D68\",\n" +
            "          \"callee\": \"628FF6F4BB9C68A8BF9C2BBE04242C4B7FAE6A19\",\n" +
            "          \"data\": \"D0679D340000000000000000000000007EA0B39261842504023A40858099700E061563CE0000000000000000000000000000000000000000000000000000000000000001\",\n" +
            "          \"value\": 1,\n" +
            "          \"gas\": 9701\n" +
            "        },\n" +
            "        \"origin\": \"1ADA404B3EEDD5CC971475489A17BAACB9BA5D68\",\n" +
            "        \"tx_id\": \"8993486AB880DB2144A58989B4E3D72F9656246D\",\n" +
            "        \"return\": \"0000000000000000000000000000000000000000000000000000000000000064\",\n" +
            "        \"exception\": \"\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"error\": null,\n" +
            "    \"id\": \"\",\n" +
            "    \"jsonrpc\": \"2.0\"\n" +
            "  }";

    private final String errorResponseString = "{\n" +
            "    \"result\": null,\n" +
            "    \"error\": {\n" +
            "      \"code\": -32603,\n" +
            "      \"message\": \"Error when transacting: Insuffient gas\"\n" +
            "    },\n" +
            "    \"id\": \"\",\n" +
            "    \"jsonrpc\": \"2.0\"\n" +
            "  }";

    @Before
    public void setUp() throws Exception {
        txResponse = new Response<>("", null, null, new TxParams("1ADA404B3EEDD5CC971475489A17BAACB9BA5D68", "8993486AB880DB2144A58989B4E3D72F9656246D"));
        txResponseWithRetValue = new Response<>("", new ReturnValue<>(BigDecimal.class, new BigDecimal(100)), null, new TxParams("1ADA404B3EEDD5CC971475489A17BAACB9BA5D68", "8993486AB880DB2144A58989B4E3D72F9656246D"));
        valResponse = new Response<>("", new ReturnValue<>(BigDecimal.class, new BigDecimal(100)), null, null);
        errorResponse = new Response<>("", null, new Error(-32603, "Error when transacting: Insuffient gas"), null);
    }

    @Test
    public void testParseVal() throws Exception {
        ResponseParser<BigDecimal> parser = new ResponseParser<>(new Variable<>("x", new Uint()));
        assertEquals(valResponse, parser.parse(valResponseString));
    }

    @Test
    public void testParseError() throws IOException {
        ResponseParser parser = new ResponseParser<>(null);
        assertEquals(errorResponse, parser.parse(errorResponseString));
    }

    @Test
    public void testParseTx() throws IOException {
        ResponseParser parser = new ResponseParser<>(null);
        assertEquals(txResponse, parser.parse(txResponseSring));
    }

    @Test
    public void testParseTxWithRetVal() throws IOException {
        ResponseParser parser = new ResponseParser<>(new Variable<>("x", new Uint()));
        assertEquals(txResponseWithRetValue, parser.parse(txResponseWithRetValueSring));
    }

}