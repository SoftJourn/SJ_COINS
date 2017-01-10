package com.softjourn.coin.server.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * OAuthHelperTest
 * Created by vromanchuk on 10.01.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureRestDocs("target/generated-snippets")
@AutoConfigureMockMvc(secure = false)
public class OAuthHelperTest {

    @Autowired
    MockMvc mvc;
    RestTemplate restTemplate = new RestTemplate();
    @Mock
    private OAuthHelper oAuthHelper;
    private String clientId = "server_client";
    private String clientSecret = "server_secret";
    private String authServerUrl = "http://localhost:8111";

    @Before
    public void setUp() throws Exception {
        oAuthHelper = new OAuthHelper(this.clientId, this.clientSecret, authServerUrl, restTemplate);
    }

    @Test
    public void generateBase64Code_testBase64encode() throws Exception {
        String testBase64encode = "c2VydmVyX2NsaWVudDpzZXJ2ZXJfc2VjcmV0";
        assertEquals(testBase64encode, OAuthHelper.generateBase64Code(clientId, clientSecret));
    }

    @Test
    public void getHeaders_AuthorizationAndContentTypeHeaders() throws Exception {
        HttpHeaders headers = oAuthHelper.getHeaders();
        assertNotNull(headers);
        assertThat(headers.size(), is(2));
        assertThat(headers.get(HttpHeaders.CONTENT_TYPE).get(0), is("application/x-www-form-urlencoded"));
        System.out.println(headers);

    }


    /**
     * Get token
     * Simulation of receiving tokens. It depends on auth server starts. May throw exceptions
     */
    @Test
    public void getToken() throws Exception {
        try {
            String url = authServerUrl + "/oauth/token";
            HttpHeaders headers = oAuthHelper.getHeaders();

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            System.out.println(response.getBody());
        } finally {
            System.out.println("Simulation of receiving tokens. It depends on auth server starts");
        }

    }

    @Test
    public void getTokenTest() throws Exception {
        System.out.println(oAuthHelper.getToken());

    }
}
