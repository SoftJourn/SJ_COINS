package com.softjourn.coin.server.eris.rpc;

import junit.framework.TestCase;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.skyscreamer.jsonassert.JSONParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientBuilder.class)
public class HTTPRPCClientTest extends TestCase {

    HTTPRPCClient client = new HTTPRPCClient();

    private String expectedResponse = "{\"test\": \"response\"}";

    @Override
    public void setUp() throws Exception {
        mockStatic(HttpClientBuilder.class);

        HttpClientBuilder builder = mock(HttpClientBuilder.class);
        CloseableHttpClient cl = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        Mockito.when(response.getStatusLine()).thenReturn(new StatusLine() {
            @Override
            public ProtocolVersion getProtocolVersion() {
                return new ProtocolVersion("http", 1, 1);
            }

            @Override
            public int getStatusCode() {
                return 200;
            }

            @Override
            public String getReasonPhrase() {
                return "OK";
            }
        });
        HttpEntity entity = mock(HttpEntity.class);

        when(HttpClientBuilder.create()).thenReturn(builder);

        Mockito.when(builder.build()).thenReturn(cl);

        Mockito.when(cl.execute(any())).then(invocationOnMock -> {
            HttpRequest request = (HttpRequest) invocationOnMock.getArguments()[0];
            Header[] contentTypeHeader = request.getHeaders(HttpHeaders.CONTENT_TYPE);
            if (! (request instanceof HttpPost)) throw new RuntimeException("Request type should be POST");
            if(contentTypeHeader.length == 0 || ! contentTypeHeader[0].getValue().equals("application/json"))
                throw new RuntimeException("Request content type should be JSON");

            String reqBody = new BufferedReader(new InputStreamReader(((HttpPost)request).getEntity().getContent())).lines().collect(Collectors.joining("\n"));

            JSONObject obj = (JSONObject) JSONParser.parseJSON(reqBody);

            assertEquals("2.0", obj.get("jsonrpc").toString());
            assertTrue(obj.has("params"));
            assertTrue(obj.has("method"));

            return response;
        });

        Mockito.when(response.getEntity()).thenReturn(entity);

        byte[] testString = expectedResponse.getBytes();
        Mockito.when(entity.getContent()).thenReturn(new ByteArrayInputStream(testString));
    }

    @Test
    public void testCall() throws Exception {
        assertEquals(expectedResponse, client.call("url", ErisRPCRequestEntity.constantCallEntity(new HashMap<>())));
    }

}