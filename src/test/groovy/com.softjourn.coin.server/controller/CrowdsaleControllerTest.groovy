package com.softjourn.coin.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.softjourn.coin.server.dto.DonateDTO
import com.softjourn.coin.server.service.ErisTransactionCollector
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.security.KeyPair
import java.util.stream.Collectors

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*
import static org.springframework.restdocs.payload.PayloadDocumentation.*
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringJUnit4ClassRunner)
@SpringBootTest(classes = ControllerTestConfig.class)
@AutoConfigureTestDatabase
@WebAppConfiguration
class CrowdsaleControllerTest {

    @MockBean
    private ErisTransactionCollector erisTransactionCollector

    @Autowired
    private DefaultTokenServices tokenService;
    @Value('${authKeyFileName}')
    private String authKeyFileName
    @Value('${authKeyStorePass}')
    private String authKeyStorePass
    @Value('${authKeyMasterPass}')
    private String authKeyMasterPass
    @Value('${authKeyAlias}')
    private String authKeyAlias

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation('target/generated-snippets')

    @Autowired
    private WebApplicationContext context

    private MockMvc mockMvc

    @Before
    synchronized void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentation))
                .build()
    }

    @Test
    void 'test of POST request to /api/v1/crowdsale/donate endpoint'() {
        mockMvc.perform(post('/api/v1/crowdsale/donate')
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "USER"))
                .content(json(new DonateDTO("some address", "some address", BigInteger.valueOf(1)))))
                .andExpect(status().isOk())
                .andDo(document("crowdsale-donate-request", preprocessRequest(prettyPrint()),
                requestFields(
                        fieldWithPath("contractAddress")
                                .description("Contract address(currency)(Required field)")
                                .type(JsonFieldType.STRING),
                        fieldWithPath("spenderAddress")
                                .description("Contract address(project)(Required field)")
                                .type(JsonFieldType.STRING),
                        fieldWithPath("amount")
                                .description("Amount to spend(Required field)")
                                .type(JsonFieldType.NUMBER),
                )))
                .andDo(document('crowdsale-donate-response',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('transactionResult')
                                .type(JsonFieldType.BOOLEAN)
                                .description("Transaction result.")
                )
        ))
    }

    @Test
    void 'test of POST request to /api/v1/crowdsale/donate endpoint without auth header'() {
        mockMvc.perform(post('/api/v1/crowdsale/donate')
                .contentType(APPLICATION_JSON)
                .content(json(new DonateDTO("some address", "some address", BigInteger.valueOf(1)))))
                .andExpect(status().isUnauthorized())
    }

    @Test
    void 'test of POST request to /api/v1/crowdsale/withdraw/{address} endpoint'() {
        mockMvc.perform(post('/api/v1/crowdsale/withdraw/{address}', "some address(project)")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "USER"))
                .contentType(APPLICATION_JSON))
                .andDo(document("crowdsale-withdraw-request",
                pathParameters(parameterWithName("address").description("Contract address(project)"))
        ))
                .andExpect(status().isOk())
                .andDo(document('crowdsale-withdraw-response',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('transactionResult')
                                .type(JsonFieldType.BOOLEAN)
                                .description("Transaction result.")
                )
        ))
    }

    @Test
    void 'test of POST request to /api/v1/crowdsale/withdraw/{address} endpoint without auth header'() {
        mockMvc.perform(post('/api/v1/crowdsale/withdraw/{address}', "some address(project)")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
    }

    @Test
    void 'test of GET request to /api/v1/crowdsale/{address} endpoint'() {
        mockMvc.perform(get('/api/v1/crowdsale/{address}', "some address(project)")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "USER")))
                .andExpect(status().isOk())
                .andDo(document('crowdsale-info-response',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('info.[0].name')
                                .type(JsonFieldType.STRING)
                                .description("Contract's field name."),
                        fieldWithPath('info.[0].value')
                                .type(JsonFieldType.OBJECT)
                                .description("Contract's field value."),
                        fieldWithPath('tokens.[0].address')
                                .type(JsonFieldType.STRING)
                                .description("Token(currency) address."),
                        fieldWithPath('tokens.[0].amount')
                                .type(JsonFieldType.OBJECT)
                                .description("Token(currency) amount.")
                )
        ))
    }

    @Test
    void 'test of GET request to /api/v1/crowdsale/{address} endpoint without auth header'() {
        mockMvc.perform(get('/api/v1/crowdsale/{address}', "some address(project)"))
                .andExpect(status().isUnauthorized())
    }

    private static String json(Object o) throws IOException {
        return new ObjectMapper().writeValueAsString(o)
    }

    private String prepareToken(Set<String> scopes, String... authorities) {

        def authoritiesCollection = Arrays.asList(authorities).stream().map({ s ->
            (GrantedAuthority) { -> s
            }
        }).collect(Collectors.toList())
        OAuth2Request oauth2Request = new OAuth2Request(null, "test", authoritiesCollection, true, scopes, null, null, null, null)
        Authentication userAuth = new TestingAuthenticationToken("test", null, authorities)
        OAuth2Authentication oauth2auth = new OAuth2Authentication(oauth2Request, userAuth)
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter()
        KeyPair keyPair = new KeyStoreKeyFactory(
                new ClassPathResource(authKeyFileName), authKeyStorePass.toCharArray())
                .getKeyPair(authKeyAlias, authKeyMasterPass.toCharArray())
        converter.setKeyPair(keyPair)

        tokenService.setTokenEnhancer(converter)
        OAuth2AccessToken token = tokenService.createAccessToken(oauth2auth)
        return token.getValue();
    }

}
