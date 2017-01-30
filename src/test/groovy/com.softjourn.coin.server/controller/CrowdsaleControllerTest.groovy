package com.softjourn.coin.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.softjourn.coin.server.dto.DonateDTO
import com.softjourn.coin.server.util.ErisTransactionCollector
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

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
    @WithMockUser(roles = ["USER"])
    void 'test of POST request to /api/v1/crowdsale/donate endpoint'() {
        mockMvc.perform(post('/api/v1/crowdsale/donate')
                .contentType(APPLICATION_JSON)
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
    @WithMockUser(roles = ["USER"])
    void 'test of POST request to /api/v1/crowdsale/withdraw/{address} endpoint'() {
        mockMvc.perform(post('/api/v1/crowdsale/withdraw/{address}', "some address(project)")
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
    @WithMockUser(roles = ["USER"])
    void 'test of GET request to /api/v1/crowdsale/{address} endpoint'() {
        mockMvc.perform(get('/api/v1/crowdsale/{address}', "some address(project)"))
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

    private static String json(Object o) throws IOException {
        return new ObjectMapper().writeValueAsString(o)
    }

}
