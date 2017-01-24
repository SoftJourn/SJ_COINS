package com.softjourn.coin.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.repository.ErisTransactionRepository;
import com.softjourn.eris.contract.ContractUnit;
import com.softjourn.eris.contract.ContractUnitType;
import com.softjourn.eris.contract.Variable;
import com.softjourn.eris.contract.types.Uint;
import com.softjourn.eris.transaction.type.Block;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class ErisTransactionServiceTest {

    @Mock
    private ErisTransactionRepository transactionRepository;

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ErisTransactionService erisTransactionService;

    private ObjectMapper mapper = new ObjectMapper();
    // Block with deploy contract tx
    private Block block15;
    private Block block33;
    private Contract contract;
    private ContractUnit contractUnit;
    private HashMap<String, String> inputArgsBlock33Tx;



    @Before
    public void setUp() throws Exception {
        File file;
        String json;
        file = new File("./src/test/resources/json/block15.json");
        json = FileUtils.readFileToString(file);
        block15 = mapper.readValue(json, Block.class);

        when(transactionRepository.save(any(TransactionStoring.class)))
                .then(invocation -> invocation.getArgumentAt(0, TransactionStoring.class));

        file = new File("./src/test/resources/json/block33.json");
        json = FileUtils.readFileToString(file);
        block33 = mapper.readValue(json, Block.class);


        file = new File("./src/test/resources/json/abi_coins.json");
        String abi = FileUtils.readFileToString(file);

        contract = new Contract();
        contract.setAbi(abi);

        when(contractService.getContractsByAddress(block33.getData().getErisTransactions().get(0).getContractAddress()))
                .thenReturn(contract);

        contractUnit = new ContractUnit();
        contractUnit.setName("setColor");
        contractUnit.setAnonymous(false);
        contractUnit.setConstant(false);
        contractUnit.setType(ContractUnitType.function);
        contractUnit.setInputs(new Variable[]{new Variable("_tokenColor", new Uint(8))});
        contractUnit.setOutputs(new Variable[0]);

        inputArgsBlock33Tx = new HashMap<>();
        inputArgsBlock33Tx.put("_tokenColor", "1");

    }


    @Test
    public void getTransactionStoring_Block_ListTransactionStoringObj() throws Exception {
        List<TransactionStoring> transactions = erisTransactionService.getTransactionStoring(block33);
        assertNotNull(transactions);
        assertEquals(transactions.size(), 1);

    }

    @Test
    public void storeTransaction_TransactionStoring_TransactionStoring() throws Exception {
        TransactionStoring transactionStoring = erisTransactionService.getTransactionStoring(block33).get(0);
        assertEquals(transactionStoring, erisTransactionService.storeTransaction(transactionStoring));
        verify(transactionRepository, times(1)).save(any(TransactionStoring.class));
    }

    @Test
    public void getContractUnit() throws Exception {
        assertEquals(contractUnit, erisTransactionService.getContractUnit(block33.getData().getErisTransactions().get(0)));

    }

    @Test
    public void getCallingData() throws Exception {
        assertEquals(inputArgsBlock33Tx, erisTransactionService.getCallingData(block33.getData().getErisTransactions().get(0), contractUnit));
    }
}