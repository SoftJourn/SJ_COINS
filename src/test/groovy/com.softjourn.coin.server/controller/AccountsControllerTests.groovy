package com.softjourn.coin.server.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

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
    @WithMockUser(roles = ["SUPER_USER"])
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
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of POST request to /api/v1/account/merchant endpoint'() {
        mockMvc.perform(post('/api/v1/account/merchant')
                .content('{\n  "name": "VM1",\n  "uniqueId": "123456-123456-123456"\n}')
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
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of GET request to /api/v1/accounts endpoint'() {
        mockMvc.perform(get('/api/v1/accounts'))
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
                        fieldWithPath('[1].ldapId')
                                .type(JsonFieldType.STRING)
                                .description('LDAP ID'),
                        fieldWithPath('[1].amount')
                                .type(JsonFieldType.NUMBER)
                                .description('Amount of coins'),
                        fieldWithPath('[1].fullName')
                                .type(JsonFieldType.STRING)
                                .description('Account full name'),
                )
        ))
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of GET request to /api/v1/accounts/{accountType} endpoint'() {
        mockMvc.perform(get('/api/v1/accounts/{accountType}', 'merchant'))
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
                                .description('Account full name')
                )
        ))
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of DELETE request to /api/v1/account/{accountName} endpoint'() {
        mockMvc.perform(delete('/api/v1/account/{ldapId}', 'VM1'))
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
}
