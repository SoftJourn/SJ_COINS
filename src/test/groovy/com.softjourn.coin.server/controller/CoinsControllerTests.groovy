package com.softjourn.coin.server.controller

import com.softjourn.coin.server.util.ErisTransactionCollector
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

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
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of GET request to /api/v1/amount endpoint'() {
        mockMvc.perform(get('/api/v1/amount'))
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
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of GET request to /api/v1/amount/treasury endpoint'() {
        mockMvc.perform(get('/api/v1/amount/treasury'))
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
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of GET request to /api/v1/amount/{accountType} endpoint'() {
        mockMvc.perform(get('/api/v1/amount/{accountType}', 'merchant'))
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
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of POST request to /api/v1/buy/{vendingMachineName} endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/api/v1/buy/{vendingMachineName}', "VM1")
                .content('{\n  "amount": 10,\n  "comment": "Buying Pepsi"\n}')
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
                                    .description('Remaining amount after transaction'),
                            fieldWithPath('error')
                                    .type(JsonFieldType.STRING)
                                    .description('Error description')
                    )
        ))
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of POST request to /api/v1/distribute/ endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/api/v1/distribute')
                .content('{\n  "amount": 10,\n  "comment": "Loading goods into machine VM1"\n}')
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andDo(document('distribute',
                preprocessResponse(prettyPrint())
        ))
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of POST request to /api/v1/move/{account} endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/api/v1/move/{account}', "account2")
                .content('{\n  "amount": 10,\n  "comment": "Bonus for hard work"\n}')
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
                                    .description('Remaining amount after transaction'),
                            fieldWithPath('error')
                                    .type(JsonFieldType.STRING)
                                    .description('Error description')
                    )
        ))
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of POST request to /api/v1/move/{account}/treasury endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/api/v1/move/{account}/treasury', "account2")
                .content('{\n  "amount": 10,\n  "comment": "Withdraw from machine 1"\n}')
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
                                .description('Remaining amount after transaction'),
                        fieldWithPath('error')
                                .type(JsonFieldType.STRING)
                                .description('Error description')
                )
        ))
    }

    @Test
    @WithMockUser(roles = ['SUPER_ADMIN'])
    void 'test of POST request to /api/v1/add/{account} endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/api/v1/add/{account}', "account1")
                .content('{\n  "amount": 10,\n  "comment": "Bonus for hard work"\n}')
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
                                    .description('Remaining amount after transaction'),
                            fieldWithPath('error')
                                    .type(JsonFieldType.STRING)
                                    .description('Error description')
                    )
        ))
    }
}
