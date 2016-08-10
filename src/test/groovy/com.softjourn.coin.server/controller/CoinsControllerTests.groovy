package com.softjourn.coin.server.controller

import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.MediaType
import org.springframework.restdocs.JUnitRestDocumentation
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
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = ControllerTestConfig.class)
@WebAppConfiguration
class CoinsControllerTests {

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
    @WithMockUser
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
    @WithMockUser
    void 'test of POST request to /api/v1/spent endpoint'() {
        mockMvc.perform(post('/api/v1/spent')
                .content('{\n  "amount": 10,\n  "comment": "Buying Pepsi"\n}')
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath('$.id', Matchers.is(notNullValue())))
                .andDo(document('spent',
                    preprocessResponse(prettyPrint()),
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
                            fieldWithPath('error')
                                    .type(JsonFieldType.STRING)
                                    .description('Error description')
                    )
        ))
    }

    @Test
    @WithMockUser
    void 'test of POST request to /api/v1/move/{account} endpoint'() {
        mockMvc.perform(post('/api/v1/move/account2')
                .content('{\n  "amount": 10,\n  "comment": "Bonus for hard work"\n}')
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id', Matchers.is(notNullValue())))
                .andDo(document('move',
                    preprocessResponse(prettyPrint()),
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
                            fieldWithPath('error')
                                    .type(JsonFieldType.STRING)
                                    .description('Error description')
                    )
        ))
    }

    @Test
    @WithMockUser(roles = ['COIN_ADMIN'])
    void 'test of POST request to /api/v1/add/{account} endpoint'() {
        mockMvc.perform(post('/api/v1/add/account1')
                .content('{\n  "amount": 10,\n  "comment": "Bonus for hard work"\n}')
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id', Matchers.is(notNullValue())))
                .andDo(document('add',
                    preprocessResponse(prettyPrint()),
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
                            fieldWithPath('error')
                                    .type(JsonFieldType.STRING)
                                    .description('Error description')
                    )
        ))
    }
}
