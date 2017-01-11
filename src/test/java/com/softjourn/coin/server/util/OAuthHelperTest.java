package com.softjourn.coin.server.util;

import com.softjourn.coin.server.dto.AccessTokenDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * OAuthHelperTest
 * Created by vromanchuk on 10.01.17.
 */
@RunWith(SpringRunner.class)
@AutoConfigureRestDocs("target/generated-snippets")
public class OAuthHelperTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OAuthHelper oAuthHelper;
    private String clientId = "server_client";
    private String clientSecret = "server_secret";
    private AccessTokenDTO tokenDTO;
    private String authServerUrl = "http://localhost:8111";
    private String url = authServerUrl + "/oauth/token";

    @Before
    public void setUp() throws Exception {
        this.oAuthHelper = new OAuthHelper(this.clientId, this.clientSecret, authServerUrl, restTemplate);
        this.tokenDTO = new AccessTokenDTO("eyJhbGciOiJSUzI1NiJ9.eyJzY29wZSI6WyJyZWFkIl0sImV4cCI6MTQ4NDE3NzQ4NSwiYXV0aG9yaXRpZXMiOlsiUk9MRV9BUFBMSUNBVElPTiJdLCJqdGkiOiJjZWFlY2UxMC04NjBiLTQyYmEtOTkwYS03YmM4MWIwNjc2MzUiLCJjbGllbnRfaWQiOiJzZXJ2ZXJfY2xpZW50In0.DYlELyP-yHlxtwiT0T9Zfge5e-S-Ej0qjbRA4mirdrSqB0MA64a3jUU1sJKdp-UTNfLR-ta240fxEE6Cqs_vJoizls25M_8WdoJjlpwyqQtbzaQPA6pB_QxC6bb3KuBo2ATCtBpRqO2H1JNyPyXQ2EifqCwd1GmCUp7ml7ZvrX7RrobI6SzFt6g35fJleCTdAlsks61-57Qs9lLIM37HZXIBM4x6qCd_d8W65bVVjsLadmGPA2wGgPUv0FfhyaxTczB2qARNQ35JAIv6QAGkbn8QWmfsHpqzbJurYWJIXqi2nuOqAGntVLMeIIiQ8T8y41EJmoeSY6EEH-eb79seuA"
                , "bearer", 500L, "read", "ceaece10-860b-42ba-990a-7bc81b067635");
        ResponseEntity<AccessTokenDTO> response = new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(AccessTokenDTO.class))).thenReturn(response);
        when(restTemplate.exchange(Matchers.anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("response", HttpStatus.OK));
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

    @Test
    public void receiveTokenFromAuth_tokenDTO() throws Exception {
        Method method = OAuthHelper.class.getDeclaredMethod("receiveTokenFromAuth");
        method.setAccessible(true);
        assertEquals(method.invoke(oAuthHelper), tokenDTO);
    }

    /**
     * Get token
     * Positive test
     */
    @Test
    public void getToken_tokenDTOGetAccessToken() throws Exception {
        assertEquals(tokenDTO.getAccessToken(), oAuthHelper.getToken());
        ;
    }

    /**
     * Test after timeout
     *
     * @throws Exception
     */
    @Test
    public void getToken_newDTOGetAccessToken() throws Exception {
        AccessTokenDTO newTokenDTO = new AccessTokenDTO("newToken"
                , "bearer", 500L, "read"
                , "ceaece10-860b-42ba-990a-7bc81b067635");
        ResponseEntity<AccessTokenDTO> response = new ResponseEntity<>(newTokenDTO, HttpStatus.OK);
        assertEquals(tokenDTO.getAccessToken(), oAuthHelper.getToken());
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(AccessTokenDTO.class))).thenReturn(response);
        assertEquals(tokenDTO.getAccessToken(), oAuthHelper.getToken());
        Thread.sleep(700L);
        assertEquals(oAuthHelper.getToken(), newTokenDTO.getAccessToken());

    }

}
