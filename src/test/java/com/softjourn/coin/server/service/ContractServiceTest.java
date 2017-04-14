package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.ContractCreateResponseDTO;
import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Instance;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.Type;
import com.softjourn.coin.server.exceptions.ContractNotFoundException;
import com.softjourn.coin.server.exceptions.TypeNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ContractRepository;
import com.softjourn.coin.server.repository.InstanceRepository;
import com.softjourn.coin.server.repository.TypeRepository;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.response.DeployResponse;
import com.softjourn.eris.contract.response.DeployResult;
import com.softjourn.eris.contract.response.Error;
import com.softjourn.eris.contract.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private TypeRepository typeRepository;

    @Mock
    private ErisContractService erisContractService;

    @Mock
    private AccountRepository accountRepository;

    private ContractService contractService;

    @Before
    public void setUp() {

        when(typeRepository.findOne("project")).thenReturn(new Type("project"));
        DeployResponse deployResponse = new DeployResponse("some id", new DeployResult(), new Error(0, null), "jsonrpc2.0");
        deployResponse.setContract(prepareErisContract("some address"));
        com.softjourn.coin.server.entity.Contract contract = new com.softjourn.coin.server.entity.Contract(1L, "some contract", true, "", "", new Type("project"), null);

        when(erisContractService.deploy(anyString(), anyString(), anyList())).thenReturn(deployResponse);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(instanceRepository.save(any(Instance.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(contractRepository.save(any(com.softjourn.coin.server.entity.Contract.class))).thenAnswer(invocation -> {
            com.softjourn.coin.server.entity.Contract c = (com.softjourn.coin.server.entity.Contract) invocation.getArguments()[0];
            c.setId(1L);
            return c;
        });
        when(contractRepository.findOne(1L)).thenReturn(contract);
        when(instanceRepository.findByContractId(anyLong())).thenReturn(new ArrayList<Instance>() {{
            add(new Instance(1L, "some name", "some address", null, contract));
        }});
        contractService = new ContractServiceImpl(contractRepository, instanceRepository, typeRepository, erisContractService, accountRepository);
    }

    @Test
    public void newContractTest() {
        NewContractDTO newContractDTO = new NewContractDTO("new", "project", "some code", "some abi", new ArrayList() {{
            add("some parameters");
        }});
        Transaction transaction = this.contractService.newContract(newContractDTO);
        assertEquals(true, transaction.getValue() instanceof ContractCreateResponseDTO);
        assertEquals("some address", ((ContractCreateResponseDTO) transaction.getValue()).getAddress());
        assertEquals("new", ((ContractCreateResponseDTO) transaction.getValue()).getName());
        assertEquals("project", ((ContractCreateResponseDTO) transaction.getValue()).getType());
        assertEquals(Long.valueOf(1), ((ContractCreateResponseDTO) transaction.getValue()).getContractId());
    }

    @Test(expected = TypeNotFoundException.class)
    public void newContractTestUnknownType() {
        NewContractDTO newContractDTO = new NewContractDTO("new", "unknown", "some code", "some abi", new ArrayList() {{
            add("some parameters");
        }});
        this.contractService.newContract(newContractDTO);
    }

    @Test
    public void changeActiveTest() {
        com.softjourn.coin.server.entity.Contract contract = this.contractService.changeActive(1L);
        assertEquals(false, contract.getActive());
    }

    @Test
    public void getInstancesByContractId() {
        List<ContractCreateResponseDTO> instancesByContractId = this.contractService.getInstancesByContractId(1L);
        assertEquals(Long.valueOf(1), instancesByContractId.get(0).getContractId());
        assertEquals("some address", instancesByContractId.get(0).getAddress());
        assertEquals("project", instancesByContractId.get(0).getType());
        assertEquals("some name", instancesByContractId.get(0).getName());
    }

    @Test
    public void newInstanceTest() {
        NewContractInstanceDTO instanceDTO = new NewContractInstanceDTO(1L, "new", new ArrayList() {{
            add("some parameters");
        }});
        Transaction transaction = this.contractService.newInstance(instanceDTO);
        assertEquals(true, transaction.getValue() instanceof ContractCreateResponseDTO);
        assertEquals("some address", ((ContractCreateResponseDTO) transaction.getValue()).getAddress());
        assertEquals("new", ((ContractCreateResponseDTO) transaction.getValue()).getName());
        assertEquals("project", ((ContractCreateResponseDTO) transaction.getValue()).getType());
        assertEquals(Long.valueOf(1), ((ContractCreateResponseDTO) transaction.getValue()).getContractId());
    }
    @Test(expected = ContractNotFoundException.class)
    public void newInstanceTestUnknownContract() {
        NewContractInstanceDTO instanceDTO = new NewContractInstanceDTO(2L, "new", new ArrayList() {{
            add("some parameters");
        }});
        Transaction transaction = this.contractService.newInstance(instanceDTO);
    }

    private Contract prepareErisContract(String address) {
        return new Contract() {
            @Override
            public void close() throws IOException {

            }

            @Override
            public Response call(String s, Object... objects) throws IOException {
                return null;
            }

            @Override
            public String subscribeToUserIn(String s, Consumer<Response> consumer) {
                return null;
            }

            @Override
            public String subscribeToUserOut(String s, Consumer<Response> consumer) {
                return null;
            }

            @Override
            public String subscribeToUserCall(String s, Consumer<Response> consumer) {
                return null;
            }

            @Override
            public void unsubscribe(String s) {

            }

            @Override
            public String getAddress() {
                return address;
            }
        };
    }

}
