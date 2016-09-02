package com.softjourn.coin.server.eris.contract.event;


import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.softjourn.coin.server.eris.rpc.ErisRPCRequestEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class EventHandler {

    private static final String SUBSCRIBE_METHOD = "erisdb.eventSubscribe";

    WebSocketFactory factory;

    public EventHandler() {
        factory = new WebSocketFactory();
    }

    public EventHandler(WebSocketFactory factory) {
        this.factory = factory;
    }

    public void subscribe(String url, String eventId, Consumer<String> callBack) {
        try {
            WebSocket socket = factory.createSocket(url);
            socket.connect();
            socket.addListener(new Listener(callBack));
            socket.sendText(createSubscribeRequest(eventId));
        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }
    }

    private String createSubscribeRequest(String eventId) {
        Map<String, Object> param = Collections.singletonMap("event_id", eventId);
        return new ErisRPCRequestEntity(param, SUBSCRIBE_METHOD).toString();
    }

    private static class Listener extends WebSocketAdapter {

        private Consumer<String> callBack;

        public Listener(Consumer<String> callBack) {
            this.callBack = callBack;
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            callBack.accept(text);
        }
    }
}
