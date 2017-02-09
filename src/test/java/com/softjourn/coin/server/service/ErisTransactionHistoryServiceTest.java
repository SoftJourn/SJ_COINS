package com.softjourn.coin.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.repository.ErisTransactionRepository;
import com.softjourn.eris.contract.ContractUnit;
import com.softjourn.eris.contract.ContractUnitType;
import com.softjourn.eris.contract.Variable;
import com.softjourn.eris.contract.types.Uint;
import com.softjourn.eris.transaction.pojo.Block;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ErisTransactionHistoryServiceTest {

    @Mock
    private ContractServiceImpl contractServiceImpl;
    @Mock
    private ErisTransactionCollector erisTransactionCollector;
    @Mock
    private ErisTransactionRepository erisTransactionRepository;
    @InjectMocks
    private ErisTransactionHistoryServiceV11 erisTransactionHistoryService;

    private ObjectMapper mapper = new ObjectMapper();
    private Block block33;
    private Block block1030101;
    private ContractUnit contractUnit;
    private HashMap<String, String> inputArgsBlock33Tx;
    private TransactionStoring transaction33;

    @Before
    public void setUp() throws Exception {


        File file;
        String json;
        file = new File("./src/test/resources/json/block15.json");
        json = FileUtils.readFileToString(file);
        Block block15 = mapper.readValue(json, Block.class);

        file = new File("./src/test/resources/json/block33.json");
        json = FileUtils.readFileToString(file);
        block33 = mapper.readValue(json, Block.class);

        file = new File("./src/test/resources/json/block1030101.json");
        json = FileUtils.readFileToString(file);
        block1030101 = mapper.readValue(json, Block.class);

        file = new File("./src/test/resources/json/abi_coins.json");
        String abi = FileUtils.readFileToString(file);

        Contract contract = new Contract();
        contract.setAbi(abi);

        when(contractServiceImpl.getContractsByAddress(block33.getData().getErisTransactions().get(0).getContractAddress()))
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

        transaction33 = new TransactionStoring();
        transaction33.setBlockNumber(block33.getHeader().getHeight());
        transaction33.setTime(block33.getHeader().getDateTime());
        transaction33.setFunctionName(contractUnit.getName());
        transaction33.setTransaction(block33.getData().getErisTransactions().get(0));
        transaction33.setChainId("test");
        transaction33.setTxId("4090245DC57B93EB8DEC3EA40FC75685595F6AAB");

        Map<String, String> callingValue = new HashMap<>();
        callingValue.put(contractUnit.getInputs()[0].getName(), "1");
        transaction33.setCallingValue(callingValue);

        Set<TransactionStoring> tss = new HashSet<>();
        when(erisTransactionRepository.save(any(TransactionStoring.class))).thenAnswer(i -> {
            TransactionStoring ts = (TransactionStoring) i.getArguments()[0];
            tss.add(ts);
            return ts;
        });
        when(erisTransactionRepository.findFirstByOrderByBlockNumberDesc())
                .thenAnswer(i -> tss.stream()
                        .sorted(Comparator.comparing(TransactionStoring::getBlockNumber))
                        .limit(1)
                        .findFirst()
                        .orElse(null));
        when(erisTransactionRepository.count())
                .thenAnswer(i -> {
                    return (long)tss.size();
                });
    }

    @Test
    public void getHeightLastStored() throws Exception {
        System.out.println("ErisTransactionServiceTest.getHeightLastStored");
        System.out.println(erisTransactionHistoryService);
        long lastSavedBlockHeightWithTx = erisTransactionHistoryService.getHeightLastStored();
        assertEquals(0L, lastSavedBlockHeightWithTx);
        erisTransactionHistoryService.storeTransaction(transaction33);
        assertEquals(transaction33.getBlockNumber(), erisTransactionHistoryService.getHeightLastStored());
    }

    @Test
    public void getTransactionStoring_Block_ListTransactionStoringObj() throws Exception {
        List<TransactionStoring> transactions = erisTransactionHistoryService.getTransactionStoring(block33).collect(Collectors.toList());
        assertNotNull(transactions);
        assertEquals(transactions.size(), 1);
        TransactionStoring transaction = transactions.get(0);
        assertEquals(transaction33, transaction);

    }

    @Test
    public void storeTransaction_TransactionStoring_TransactionStoring() throws Exception {
        TransactionStoring transactionStoring = erisTransactionHistoryService.getTransactionStoring(block33).collect(Collectors.toList()).get(0);
        assertEquals(transactionStoring, erisTransactionHistoryService.storeTransaction(transactionStoring));
    }

    @Test
    public void getContractUnit() throws Exception {
        assertEquals(contractUnit, erisTransactionHistoryService.getContractUnit(block33.getData().getErisTransactions().get(0)));

    }

    @Test
    public void getCallingData() throws Exception {
        assertEquals(inputArgsBlock33Tx, erisTransactionHistoryService.getCallingData(block33.getData().getErisTransactions().get(0), contractUnit));
    }

    @Test
    public void getTransactionStoring_Blocks_ListTransactionStoringObj() throws Exception {
        List<Block> blocks = new ArrayList<>();
        blocks.add(block33);
        blocks.add(block1030101);
        assertEquals(2, erisTransactionHistoryService.getTransactionStoring(blocks).collect(Collectors.toList()).size());
    }

}