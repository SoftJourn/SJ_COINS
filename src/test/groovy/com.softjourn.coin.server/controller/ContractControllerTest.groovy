package com.softjourn.coin.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.softjourn.coin.server.dto.NewContractDTO
import com.softjourn.coin.server.dto.NewContractInstanceDTO
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringJUnit4ClassRunner)
@SpringBootTest(classes = ControllerTestConfig.class)
@AutoConfigureTestDatabase
@WebAppConfiguration
class ContractControllerTest {

    @MockBean
    private ErisTransactionCollector erisTransactionCollector;

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
    void 'test of POST request to /api/v1/contracts endpoint'() {
        mockMvc.perform(post('/api/v1/contracts')
                .contentType(APPLICATION_JSON)
                .content(json(new NewContractDTO("newContract", "type", "some code", "some interface", new ArrayList<>().toArray() as List<Object>))))
                .andExpect(status().isOk())
                .andDo(document("createContract-request", preprocessRequest(prettyPrint()),
                requestFields(
                        fieldWithPath("name").description("Contract name(Required field)").type(JsonFieldType.STRING),
                        fieldWithPath("type").description("Contract type(Required field)").type(JsonFieldType.STRING),
                        fieldWithPath("code").description("Contract's byte code(Required field)").type(JsonFieldType.STRING),
                        fieldWithPath("abi").description("Contract's interface (Required field)").type(JsonFieldType.STRING),
                        fieldWithPath("parameters").description("Input parameters of contract constructor," +
                                "if constructor does not have any parameters field should empty, " +
                                "otherwise if constructor has parameters, than put these parameters" +
                                " in the same order that they are in contract constructor (Required field)").type(JsonFieldType.ARRAY)
                )))
                .andDo(document('createContract-response',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('contractId')
                                .type(JsonFieldType.NUMBER)
                                .description("Contract's id."),
                        fieldWithPath('name')
                                .type(JsonFieldType.STRING)
                                .description("Contract's name."),
                        fieldWithPath('type')
                                .type(JsonFieldType.STRING)
                                .description("Contract's type."),
                        fieldWithPath('address')
                                .type(JsonFieldType.STRING)
                                .description("Contract's instance address.")
                )
        ))
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of POST request to /api/v1/contracts/instances endpoint'() {
        mockMvc.perform(post('/api/v1/contracts/instances')
                .contentType(APPLICATION_JSON)
                .content(json(new NewContractInstanceDTO(1, new ArrayList<>().toArray() as List<Object>))))
                .andExpect(status().isOk())
                .andDo(document("createContractInstance-request", preprocessRequest(prettyPrint()),
                requestFields(
                        fieldWithPath("contractId").description("Contract's id(Required field)").type(JsonFieldType.NUMBER),
                        fieldWithPath("parameters").description("Input parameters of contract constructor," +
                                "if constructor does not have any parameters field should empty, " +
                                "otherwise if constructor has parameters, than put these parameters" +
                                " in the same order that they are in contract constructor (Required field)").type(JsonFieldType.ARRAY)
                )))
                .andDo(document('createContractInstance-response',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('contractId')
                                .type(JsonFieldType.NUMBER)
                                .description("Contract's id."),
                        fieldWithPath('name')
                                .type(JsonFieldType.STRING)
                                .description("Contract's name."),
                        fieldWithPath('type')
                                .type(JsonFieldType.STRING)
                                .description("Contract's type."),
                        fieldWithPath('address')
                                .type(JsonFieldType.STRING)
                                .description("Contract's instance address.")
                )
        ))
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of GET request to /api/v1/contracts endpoint'() {
        mockMvc.perform(get('/api/v1/contracts'))
                .andExpect(status().isOk())
                .andDo(document('getContract-response',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('[0].id')
                                .type(JsonFieldType.NUMBER)
                                .description("Contract's id."),
                        fieldWithPath('[0].name')
                                .type(JsonFieldType.STRING)
                                .description("Contract's name."),
                        fieldWithPath('[0].type.type')
                                .type(JsonFieldType.STRING)
                                .description("Contract's type."),
                        fieldWithPath('[0].instances')
                                .type(JsonFieldType.ARRAY)
                                .description("Contract's instances."),
                        fieldWithPath('[0].instances.[0].id')
                                .type(JsonFieldType.NUMBER)
                                .description("Instance's id."),
                        fieldWithPath('[0].instances.[0].address')
                                .type(JsonFieldType.STRING)
                                .description("Instance's address.")
                )
        ))
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    void 'test of GET request to /api/v1/contracts/instances endpoint'() {
        mockMvc.perform(get('/api/v1/contracts/instances/{contractId}', 1))
                .andExpect(status().isOk())
                .andDo(document('getInstancesByContractId-response',
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath('[0].contractId')
                                .type(JsonFieldType.NUMBER)
                                .description("Contract's id."),
                        fieldWithPath('[0].name')
                                .type(JsonFieldType.STRING)
                                .description("Contract's name."),
                        fieldWithPath('[0].type')
                                .type(JsonFieldType.STRING)
                                .description("Contract's type."),
                        fieldWithPath('[0].address')
                                .type(JsonFieldType.STRING)
                                .description("Contract's instance address.")
                )
        ))
    }

    private static String json(Object o) throws IOException {
        return new ObjectMapper().writeValueAsString(o)
    }

}
