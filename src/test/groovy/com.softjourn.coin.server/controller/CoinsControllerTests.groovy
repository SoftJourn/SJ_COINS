package com.softjourn.coin.server.controller

import com.softjourn.coin.server.service.ErisTransactionCollector
import org.hamcrest.Matchers
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
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
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
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.security.KeyPair
import java.util.stream.Collectors

import static org.hamcrest.Matchers.notNullValue
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@RunWith(SpringJUnit4ClassRunner)
@SpringBootTest(classes = ControllerTestConfig.class)
@AutoConfigureTestDatabase
@WebAppConfiguration
class CoinsControllerTests {

    @MockBean
    private ErisTransactionCollector erisTransactionCollector

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation('target/generated-snippets')

    @Autowired
    private WebApplicationContext context

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
    void 'test of GET request to /v1/amount endpoint'() {
        mockMvc.perform(get('/v1/amount')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andDo(document('amount',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Amount of coins.')
                )
        ))
    }

    @Test
    void 'test of GET request to /v1/amount endpoint without auth'() {
        mockMvc.perform(get('/v1/amount'))
                .andExpect(status().isUnauthorized())
    }

    @Test
    void 'test of GET request to /v1/amount/treasury endpoint'() {
        mockMvc.perform(get('/v1/amount/treasury')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andDo(document('treasuryAmount',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Amount of coins.')
                )
        ))
    }

    @Test
    void 'test of GET request to /v1/amount/treasury endpoint with wrong role'() {
        mockMvc.perform(get('/v1/amount/treasury')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_USER")))
                .andExpect(status().isForbidden())
    }

    @Test
    void 'test of GET request to /v1/amount/{accountType} endpoint'() {
        mockMvc.perform(get('/v1/amount/{accountType}', 'merchant')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN")))
                .andExpect(status().isOk())
                .andDo(document('merchantAmount',
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName('accountType').description('Type of account')),
                responseFields(
                        fieldWithPath('amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Amount of coins.')
                )
        ))
    }

    @Test
    void 'test of GET request to /v1/amount/{accountType} endpoint with wrong role'() {
        mockMvc.perform(get('/v1/amount/{accountType}', 'merchant')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_INVENTORY_ADMIN")))
                .andExpect(status().isForbidden())
    }

    @Test
    void 'test of POST request to /v1/buy/{vendingMachineName} endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/buy/{vendingMachineName}', "VM1")
                .content('{\n  "amount": 10,\n  "comment": "Buying Pepsi"\n}')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath('$.id', Matchers.is(notNullValue())))
                .andDo(document('spent',
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("vendingMachineName")
                                .description("The name of vending machine where you want to buy eris account address")
                ),
                responseFields(
                        fieldWithPath('id')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction ID'),
                        fieldWithPath('account')
                                .type(JsonFieldType.STRING)
                                .description('Current Account'),
                        fieldWithPath('destination')
                                .type(JsonFieldType.STRING)
                                .description('Destination Account'),
                        fieldWithPath('amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction amount'),
                        fieldWithPath('comment')
                                .type(JsonFieldType.STRING)
                                .description('Transaction comment'),
                        fieldWithPath('created')
                                .type(JsonFieldType.OBJECT)
                                .description('Create date'),
                        fieldWithPath('status')
                                .type(JsonFieldType.STRING)
                                .description('Transaction status'),
                        fieldWithPath('remain')
                                .type(JsonFieldType.NUMBER)
                                .description('Remaining amount after body'),
                        fieldWithPath('error')
                                .type(JsonFieldType.STRING)
                                .description('Error description'),
                        fieldWithPath('transactionStoring')
                                .type(JsonFieldType.OBJECT)
                                .description('Error description')
                )
        ))
    }

    @Test
    void 'test of POST request to /v1/buy/{vendingMachineName} endpoint without token'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/buy/{vendingMachineName}', "VM1")
                .content('{\n  "amount": 10,\n  "comment": "Buying Pepsi"\n}'))
                .andExpect(status().isUnauthorized())
    }

    @Test
    void 'test of POST request to /v1/rollback/{txId} endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/rollback/{txId}', "12")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.singleton("rollback"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath('$.id', Matchers.is(notNullValue())))
                .andDo(document('rollback',
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("txId")
                                .description("Id of body that needs to be rolled back")
                ),
                responseFields(
                        fieldWithPath('id')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction ID'),
                        fieldWithPath('account')
                                .type(JsonFieldType.STRING)
                                .description('Current Account'),
                        fieldWithPath('destination')
                                .type(JsonFieldType.STRING)
                                .description('Destination Account'),
                        fieldWithPath('amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction amount'),
                        fieldWithPath('comment')
                                .type(JsonFieldType.STRING)
                                .description('Transaction comment'),
                        fieldWithPath('created')
                                .type(JsonFieldType.OBJECT)
                                .description('Create date'),
                        fieldWithPath('status')
                                .type(JsonFieldType.STRING)
                                .description('Transaction status'),
                        fieldWithPath('remain')
                                .type(JsonFieldType.NUMBER)
                                .description('Remaining amount after body'),
                        fieldWithPath('error')
                                .type(JsonFieldType.STRING)
                                .description('Error description'),
                        fieldWithPath('transactionStoring')
                                .type(JsonFieldType.OBJECT)
                                .description('Error description')
                )
        ))
    }

    @Test
    void 'test of POST request to /v1/rollback/{txId} endpoint with wrong scope'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/rollback/{txId}', "12")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.singleton("wrongScope"))))
                .andExpect(status().isForbidden())
    }

    @Test
    void 'test of POST request to /v1/rollback/{txId} endpoint without token'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/rollback/{txId}', "12"))
                .andExpect(status().isUnauthorized())
    }

    @Test
    void 'test of POST request to /v1/distribute/ endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/distribute')
                .content('{\n  "amount": 10,\n  "comment": "Loading goods into machine VM1"\n}')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andDo(document('distribute',
                preprocessResponse(prettyPrint())
        ))
    }

    @Test
    void 'test of POST request to /v1/move/{account} endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/move/{account}', "account2")
                .content('{\n  "amount": 10,\n  "comment": "Bonus for hard work"\n}')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id', Matchers.is(notNullValue())))
                .andDo(document('move',
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("account")
                                .description("Account to send coins")
                ),
                responseFields(
                        fieldWithPath('id')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction ID'),
                        fieldWithPath('account')
                                .type(JsonFieldType.STRING)
                                .description('Current Account'),
                        fieldWithPath('destination')
                                .type(JsonFieldType.STRING)
                                .description('Destination Account'),
                        fieldWithPath('amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction amount'),
                        fieldWithPath('comment')
                                .type(JsonFieldType.STRING)
                                .description('Transaction comment'),
                        fieldWithPath('created')
                                .type(JsonFieldType.OBJECT)
                                .description('Create date'),
                        fieldWithPath('status')
                                .type(JsonFieldType.STRING)
                                .description('Transaction status'),
                        fieldWithPath('remain')
                                .type(JsonFieldType.NUMBER)
                                .description('Remaining amount after body'),
                        fieldWithPath('error')
                                .type(JsonFieldType.STRING)
                                .description('Error description'),
                        fieldWithPath('transactionStoring')
                                .type(JsonFieldType.OBJECT)
                                .description('Error description')
                )
        ))
    }

    @Test
    void 'test of POST request to /v1/move/{account}/treasury endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/move/{account}/treasury', "account2")
                .content('{\n  "amount": 10,\n  "comment": "Withdraw from machine 1"\n}')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id', Matchers.is(notNullValue())))
                .andDo(document('moveToTreasury',
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("account")
                                .description("Account to send coins")
                ),
                responseFields(
                        fieldWithPath('id')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction ID'),
                        fieldWithPath('account')
                                .type(JsonFieldType.STRING)
                                .description('Current Account'),
                        fieldWithPath('destination')
                                .type(JsonFieldType.STRING)
                                .description('Destination Account'),
                        fieldWithPath('amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction amount'),
                        fieldWithPath('comment')
                                .type(JsonFieldType.STRING)
                                .description('Transaction comment'),
                        fieldWithPath('created')
                                .type(JsonFieldType.OBJECT)
                                .description('Create date'),
                        fieldWithPath('status')
                                .type(JsonFieldType.STRING)
                                .description('Transaction status'),
                        fieldWithPath('remain')
                                .type(JsonFieldType.NUMBER)
                                .description('Remaining amount after body'),
                        fieldWithPath('error')
                                .type(JsonFieldType.STRING)
                                .description('Error description'),
                        fieldWithPath('transactionStoring')
                                .type(JsonFieldType.OBJECT)
                                .description('Error description')
                )
        ))
    }

    @Test
    void 'test of POST request to /v1/move/{account}/treasury endpoint with wrong role'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/move/{account}/treasury', "account2")
                .content('{\n  "amount": 10,\n  "comment": "Withdraw from machine 1"\n}')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isForbidden())
    }

    @Test
    void 'test of POST request to /v1/add/{account} endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/add/{account}', "account1")
                .content('{\n  "amount": 10,\n  "comment": "Bonus for hard work"\n}')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id', Matchers.is(notNullValue())))
                .andDo(document('add',
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("account")
                                .description("Account to add coins")
                ),
                responseFields(
                        fieldWithPath('id')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction ID'),
                        fieldWithPath('account')
                                .type(JsonFieldType.STRING)
                                .description('Current Account'),
                        fieldWithPath('destination')
                                .type(JsonFieldType.STRING)
                                .description('Destination Account'),
                        fieldWithPath('amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction amount'),
                        fieldWithPath('comment')
                                .type(JsonFieldType.STRING)
                                .description('Transaction comment'),
                        fieldWithPath('created')
                                .type(JsonFieldType.OBJECT)
                                .description('Create date'),
                        fieldWithPath('status')
                                .type(JsonFieldType.STRING)
                                .description('Transaction status'),
                        fieldWithPath('remain')
                                .type(JsonFieldType.NUMBER)
                                .description('Remaining amount after body'),
                        fieldWithPath('error')
                                .type(JsonFieldType.STRING)
                                .description('Error description'),
                        fieldWithPath('transactionStoring')
                                .type(JsonFieldType.OBJECT)
                                .description('Error description'),
                        fieldWithPath('transactionStoring')
                                .type(JsonFieldType.OBJECT)
                                .description('Error description')
                )
        ))
    }

    @Test
    void 'test of POST request to /v1/add/{account} endpoint wrong role'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/v1/add/{account}', "account1")
                .content('{\n  "amount": 10,\n  "comment": "Bonus for hard work"\n}')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_INVENTORY_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isForbidden())
    }

    @Test
    void 'test of GET request to /v1/add/template endpoint to download template'() {
        mockMvc.perform(RestDocumentationRequestBuilders.get('/v1/template')
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_BILLING")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"template.csv\""))
                .andDo(document("template-response",
                preprocessResponse(prettyPrint())))
    }

    @Test
    void 'test of POST request to /v1/add/ endpoint to fill accounts'() {
        def csv = "Account,\"Full Name\",Coins\n" +
                "ovovchuk,\"Oleksandr Vovchuk\",0\n" +
                "vkraietskyi,\"Volodymyr Kraietskyi\",0\n" +
                "vkuryga,\"Vita Kuryga\",0\n" +
                "vmoroz,\"Volodymyr Moroz\",0\n" +
                "vromanchuk,\"Volodymyr Romanchuk\",0"
        MockMultipartFile file = new MockMultipartFile("file", "file", "multipart/form-data", csv.getBytes())
        mockMvc.perform(RestDocumentationRequestBuilders.fileUpload('/v1/add/')
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_BILLING")))
                .andExpect(status().isOk())
                .andDo(document('fillAccounts-response',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('checkHash')
                                .type(JsonFieldType.STRING)
                                .description('Hash of running process')
                )))
    }

    @Test
    void 'test of GET request to /v1/add/check endpoint to check progress'() {
        mockMvc.perform(RestDocumentationRequestBuilders.get('/v1/check/{checkHash}', "some hash")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + prepareToken(Collections.emptySet(), "ROLE_BILLING")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andDo(document('check-response',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('total')
                                .type(JsonFieldType.NUMBER)
                                .description('Total amount of records to process'),
                        fieldWithPath('isDone')
                                .type(JsonFieldType.STRING)
                                .description('Amount of records that have already processed'),
                        fieldWithPath('transactions')
                                .type(JsonFieldType.ARRAY)
                                .description('Array of processed transactions(This array will be full only in case where all transactions are processed)'),
                        fieldWithPath('transactions[0].amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Transaction amount'),
                        fieldWithPath('transactions[0].comment')
                                .type(JsonFieldType.STRING)
                                .description('Transaction comment'),
                        fieldWithPath('transactions[0].created')
                                .type(JsonFieldType.OBJECT)
                                .description('Create date'),
                        fieldWithPath('transactions[0].status')
                                .type(JsonFieldType.STRING)
                                .description('Transaction status'),
                        fieldWithPath('transactions[0].remain')
                                .type(JsonFieldType.NUMBER)
                                .description('Remaining amount after body'),
                        fieldWithPath('transactions[0].error')
                                .type(JsonFieldType.STRING)
                                .description('Error description'),
                        fieldWithPath('transactions[0].transactionStoring')
                                .type(JsonFieldType.OBJECT)
                                .description('Error description'),
                        fieldWithPath('transactions[0].transactionStoring')
                                .type(JsonFieldType.OBJECT)
                                .description('Error description')
                )
        ))
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
