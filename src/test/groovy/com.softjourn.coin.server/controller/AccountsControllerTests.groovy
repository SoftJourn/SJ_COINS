package com.softjourn.coin.server.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = ControllerTestConfig.class)
@WebAppConfiguration
class AccountsControllerTests {

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
    void 'test of GET request to /api/v1/account endpoint'() {
        mockMvc.perform(get('/api/v1/account'))
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
    @WithMockUser
    void 'test of POST request to /api/v1/account/{sellerName} endpoint'() {
        mockMvc.perform(RestDocumentationRequestBuilders.post('/api/v1/account/{sellerName}', "VM1"))
                .andExpect(status().isOk())
                .andDo(document('addSeller',
                preprocessResponse(prettyPrint()), pathParameters(
                parameterWithName("sellerName")
                        .description("The name of vending machine that you want to register")
        ),
                responseFields(
                        fieldWithPath('name')
                                .type(JsonFieldType.STRING)
                                .description('Account\'s name.')
                )
        ))
    }

}
