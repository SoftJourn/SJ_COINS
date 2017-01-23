package com.softjourn.coin.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.ErisCallingData;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.repository.ErisTransactionRepository;
import com.softjourn.eris.transaction.type.Block;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
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
    private Block block15;
    private Block block33;

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

        Contract contract = new Contract();
        contract.setAbi(abi);

        when(contractService.getContractsByAddress(block33.getData().getErisTransactions().get(0).getContractAddress()))
                .thenReturn(contract);

    }


    @Test
    public void getTransactionStoringFromBlock_Block_ListTransactionStoringObj() throws Exception {
        List<TransactionStoring> transactions = ErisTransactionService.getTransactionStoringFromBlock(block15);
        assertNotNull(transactions);
        assertEquals(transactions.size(), 1);

    }

    @Test
    public void storeTransaction_TransactionStoring_TransactionStoring() throws Exception {
        TransactionStoring transactionStoring = ErisTransactionService.getTransactionStoringFromBlock(block15).get(0);
        assertEquals(transactionStoring, erisTransactionService.storeTransaction(transactionStoring));
        verify(transactionRepository,times(1)).save(any(TransactionStoring.class));
    }

    @Test
    public void getCallingData() throws Exception {
        TransactionStoring transactionStoring = ErisTransactionService.getTransactionStoringFromBlock(block33).get(0);
        ErisCallingData callingData = erisTransactionService.getCallingData(transactionStoring);
    }
}