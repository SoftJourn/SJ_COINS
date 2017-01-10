package com.softjourn.coin.server.util;

import com.softjourn.coin.server.dto.TokenDTO;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Component
public class OAuthHelper {

    private String clientId;

    private String clientSecret;

    private String authServerUrl;

    private RestTemplate restTemplate;

    private String base64;

    @Autowired
    public OAuthHelper(@Value("${auth.client.client-id}") String clientId
            , @Value("${auth.client.client-secret}") String clientSecret
            , @Value("${auth.server.url}") String authServerUrl
            , RestTemplate restTemplate) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authServerUrl = authServerUrl;
        this.restTemplate = restTemplate;
        this.base64 = OAuthHelper.generateBase64Code(clientId, clientSecret);
    }

    public static String generateBase64Code(String clientId, String clientSecret) {
        String base = clientId + ":" + clientSecret;
        byte[] encodedBytes = Base64.getEncoder().encode(base.getBytes());
        return new String(encodedBytes);
    }

    public HttpHeaders getHeaders() {
        String authorization = "Basic " + this.base64;
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        return headers;
    }

    public TokenDTO getToken() {
        String url = authServerUrl + "/oauth/token";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, this.getHeaders());
        ResponseEntity<TokenDTO> response = restTemplate.postForEntity(url, request, TokenDTO.class);

        return response.getBody();
    }

}
