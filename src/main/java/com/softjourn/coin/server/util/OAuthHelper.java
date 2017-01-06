package com.softjourn.coin.server.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class OAuthHelper {

    private String clientId;

    private String clientSecret;

    private String base64;

    public OAuthHelper(@Value("${auth.client.client-id}") String clientId
            , @Value("${auth.client.client-secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.base64 = this.generateBase64Code();
    }

    private String generateBase64Code() {
        String base = "{" + this.clientId + ":" + this.clientSecret + "}";
        byte[] encodedBytes = Base64.getEncoder().encode(base.getBytes());
        return new String(encodedBytes);
    }


}
