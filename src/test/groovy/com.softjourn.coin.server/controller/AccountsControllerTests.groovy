package com.softjourn.coin.server.controller

import com.softjourn.coin.server.service.ErisTransactionCollector
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
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
class AccountsControllerTests {

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
    void 'test of GET request to /api/v1/account endpoint'() {
        mockMvc.perform(get('/api/v1/account')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andDo(document('account',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Count of coins in account.'),
                        fieldWithPath('name')
                                .type(JsonFieldType.STRING)
                                .description('Account\'s name.'),
                        fieldWithPath('surname')
                                .type(JsonFieldType.STRING)
                                .description('Account\'s surname.'),
                        fieldWithPath('image')
                                .type(JsonFieldType.STRING)
                                .description('Account\'s image.')
                )
        ))
    }

    @Test
    void 'test of GET request to /api/v1/account endpoint without auth header'() {
        mockMvc.perform(get('/api/v1/account'))
                .andExpect(status().isUnauthorized())
    }

    @Test
    void 'test of POST request to /api/v1/account/merchant endpoint'() {
        mockMvc.perform(post('/api/v1/account/merchant')
                .content('{\n  "name": "VM1",\n  "uniqueId": "123456-123456-123456"\n}')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document('addSeller',
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                        fieldWithPath('name')
                                .type(JsonFieldType.STRING)
                                .description('Vending machine name'),
                        fieldWithPath('uniqueId')
                                .type(JsonFieldType.STRING)
                                .description('Unique Id of the machine')
                ),
                responseFields(
                        fieldWithPath('name')
                                .type(JsonFieldType.STRING)
                                .description('Account\'s name.')
                )
        ))
    }

    @Test
    void 'test of POST request to /api/v1/account/merchant endpoint with wrong ROLE'() {
        mockMvc.perform(post('/api/v1/account/merchant')
                .content('{\n  "name": "VM1",\n  "uniqueId": "123456-123456-123456"\n}')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_ADM"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
    }

    @Test
    void 'test of GET request to /api/v1/accounts endpoint'() {
        mockMvc.perform(get('/api/v1/accounts')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andDo(document('accounts',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('[0].ldapId')
                                .type(JsonFieldType.STRING)
                                .description('LDAP ID'),
                        fieldWithPath('[0].amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Amount of coins'),
                        fieldWithPath('[0].fullName')
                                .type(JsonFieldType.STRING)
                                .description('Account full name'),
                        fieldWithPath('[0].isNew')
                                .type(JsonFieldType.BOOLEAN)
                                .description('Is new ?'),
                        fieldWithPath('[1].ldapId')
                                .type(JsonFieldType.STRING)
                                .description('LDAP ID'),
                        fieldWithPath('[1].amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Amount of coins'),
                        fieldWithPath('[1].fullName')
                                .type(JsonFieldType.STRING)
                                .description('Account full name'),
                        fieldWithPath('[0].isNew')
                                .type(JsonFieldType.BOOLEAN)
                                .description('Is new ?'),
                        fieldWithPath('[0].address')
                                .type(JsonFieldType.STRING)
                                .description('Eris account address')
                )
        ))
    }

    @Test
    void 'test of GET request to /api/v1/accounts endpoint wrong ROLE'() {
        mockMvc.perform(get('/api/v1/accounts')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_ADMIN")))
                .andExpect(status().isForbidden())
    }

    @Test
    void 'test of GET request to /api/v1/accounts/{accountType} endpoint'() {
        mockMvc.perform(get('/api/v1/accounts/{accountType}', 'merchant')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andDo(document('merchantAccounts',
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("accountType").description('Account type')),
                responseFields(
                        fieldWithPath('[0].ldapId')
                                .type(JsonFieldType.STRING)
                                .description('LDAP ID'),
                        fieldWithPath('[0].amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Amount of coins'),
                        fieldWithPath('[0].fullName')
                                .type(JsonFieldType.STRING)
                                .description('Account full name'),
                        fieldWithPath('[0].isNew')
                                .type(JsonFieldType.BOOLEAN)
                                .description('Is new ?'),
                        fieldWithPath('[0].address')
                                .type(JsonFieldType.STRING)
                                .description('Eris account address')
                )
        ))
    }

    @Test
    void 'test of GET request to /api/v1/accounts/{accountType} endpoint without auth header'() {
        mockMvc.perform(get('/api/v1/accounts/{accountType}', 'merchant'))
                .andExpect(status().isUnauthorized())
    }

    @Test
    void 'test of DELETE request to /api/v1/account/{accountName} endpoint'() {
        mockMvc.perform(delete('/api/v1/account/{ldapId}', 'VM1')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andDo(document('deleteAccount',
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("ldapId").description('Account ldapId')),
                responseFields(
                        fieldWithPath('deleted')
                                .type(JsonFieldType.BOOLEAN)
                                .description('Status of delete operation')
                )
        ))
    }

    @Test
    void 'test of DELETE request to /api/v1/account/{accountName} endpoint wrong role'() {
        mockMvc.perform(delete('/api/v1/account/{ldapId}', 'VM1')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_ADM")))
                .andExpect(status().isForbidden())
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
