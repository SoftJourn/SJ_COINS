package com.softjourn.coin.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.service.ErisTransactionService;
import com.softjourn.eris.transaction.TransactionHelper;
import com.softjourn.eris.transaction.type.Block;
import com.softjourn.eris.transaction.type.BlockMeta;
import com.softjourn.eris.transaction.type.Blocks;
import com.softjourn.eris.transaction.type.Header;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class ErisTransactionCollectorTest {

    private BigInteger lastBlock = BigInteger.valueOf(12);
    private ErisTransactionService transactionService = mock(ErisTransactionService.class);
    private ErisTransactionCollector testCollector;
    private ObjectMapper mapper = new ObjectMapper();
    private TransactionHelper transactionHelperMock = mock(TransactionHelper.class);
    private ScheduledExecutorService scheduledExecutorService;

    @Test
    public void getBlockNumbersWithTransaction_1_10_EmptyList() throws Exception {
        List<BigInteger> list = testCollector
                .getBlockNumbersWithTransaction(BigInteger.ONE, BigInteger.TEN)
                .collect(Collectors.toList());
        assertTrue(list.isEmpty());
    }

    @Test
    public void getBlockNumbersWithTransaction_1_11_EmptyList() throws Exception {
        scheduledExecutorService.shutdown();
        List<BigInteger> list = testCollector
                .getBlockNumbersWithTransaction(BigInteger.ONE, BigInteger.TEN.add(BigInteger.ONE))
                .collect(Collectors.toList());
        assertTrue(list.isEmpty());
    }

    @Test
    public void getBlockNumbersWithTransaction_1_12_BlockHeight11() throws Exception {
        System.out.println("ErisTransactionCollectorTest.getBlockNumbersWithTransaction_1_12_BlockHeight11");
        scheduledExecutorService.shutdown();
        List<BigInteger> list = testCollector.getBlockNumbersWithTransaction(BigInteger.ONE, BigInteger.valueOf(13))
                .collect(Collectors.toList());
        System.out.println(list);
        assertFalse(list.isEmpty());
        assertTrue(list.contains(BigInteger.valueOf(12)));
    }

    @Test
    public void getTransactionsFromBlock_BlockNumber_ListOfTransactions() throws Exception {
        List<TransactionStoring> transactions;
        transactions = testCollector.getTransactionsFromBlock(BigInteger.valueOf(27));
        assertEquals(1, transactions.size());
        transactions = testCollector.getTransactionsFromBlock(BigInteger.valueOf(33));
        assertEquals(1, transactions.size());
    }

    @Test
    public void getTransactionsFromBlocks_ListBlockNumbers_ListOfTransactions() throws Exception {
        System.out.println("ErisTransactionCollectorTest.getTransactionsFromBlocks_ListBlockNumbers_ListOfTransactions");
        List<BigInteger> blockNumbers = new ArrayList<>();
        blockNumbers.add(BigInteger.valueOf(27));
        blockNumbers.add(BigInteger.valueOf(33));
        List<TransactionStoring> transactions;
        transactions = testCollector.getTransactionsFromBlocks(blockNumbers);
        System.out.println(transactions);
        assertEquals(2, transactions.size());
    }

    @Test
    public void run() throws Exception {
        testCollector.run();
        verify(transactionHelperMock, atLeastOnce()).getLatestBlockNumber();
        verify(transactionService, atLeastOnce()).getHeightLastStored();

    }

    @Before
    public void setUp() throws Exception {
        when(transactionService.getHeightLastStored()).thenReturn(BigInteger.ZERO);
        when(transactionHelperMock.getLatestBlockNumber()).thenReturn(lastBlock);

        int[] blocksInitArray = new int[9];
        List<BlockMeta> blockMetas = Arrays.stream(blocksInitArray)
                .mapToObj((i) -> new BlockMeta())
                .peek(block -> block.setHeader(new Header()))
                .peek(blockMeta -> blockMeta.getHeader().setNumTxs(0))
                .collect(Collectors.toList());

        when(transactionHelperMock.getBlockStream(BigInteger.ONE, BigInteger.TEN)).thenReturn(blockMetas.stream());

        blockMetas = new ArrayList<>(blockMetas);
        blockMetas.add(null);
        when(transactionHelperMock.getBlockStream(BigInteger.ONE, BigInteger.TEN.add(BigInteger.ONE)))
                .thenReturn(blockMetas.stream());

        blockMetas = new ArrayList<>(blockMetas);
        BlockMeta blockMetaWithTx = new BlockMeta();
        Header header;
        header = new Header();
        header.setNumTxs(1);
        header.setHeight(BigInteger.valueOf(12));
        blockMetaWithTx.setHeader(header);
        blockMetas.add(blockMetaWithTx);
        when(transactionHelperMock.getBlockStream(BigInteger.ONE, BigInteger.valueOf(13)))
                .thenReturn(blockMetas.stream());

        File file;
        String json;
        Block block;

        List<TransactionStoring> transactionStorings = new ArrayList<>();
        transactionStorings.add(new TransactionStoring());

        file = new File("src/test/resources/json/block27.json");
        json = new Scanner(file).useDelimiter("\\Z").next();
        block = mapper.readValue(json, Block.class);
        when(transactionHelperMock.getBlock(BigInteger.valueOf(27))).thenReturn(block);

        when(transactionService.getTransactionStoring(block)).thenReturn(transactionStorings);

        file = new File("src/test/resources/json/block33.json");
        json = new Scanner(file).useDelimiter("\\Z").next();
        block = mapper.readValue(json, Block.class);
        when(transactionHelperMock.getBlock(BigInteger.valueOf(33))).thenReturn(block);

        when(transactionService.getTransactionStoring(block)).thenReturn(transactionStorings);

        file = new File("src/test/resources/json/block15.json");
        json = new Scanner(file).useDelimiter("\\Z").next();
        block = mapper.readValue(json, Block.class);
        when(transactionHelperMock.getBlock(BigInteger.valueOf(15))).thenReturn(block);

        file = new File("src/test/resources/json/blockRange1-75.json");
        json = new Scanner(file).useDelimiter("\\Z").next();
        Blocks blocks;
        blocks = mapper.readValue(json, Blocks.class);
        when(transactionHelperMock.getBlockStream(BigInteger.ONE, BigInteger.valueOf(75)))
                .thenReturn(blocks.getBlockMetas().stream());

        when(transactionService.storeTransaction(any(TransactionStoring.class)))
                .thenAnswer(invocation -> invocation.getArgumentAt(0, TransactionStoring.class));

        doCallRealMethod().when(transactionService).storeTransaction(any(List.class));


        long runningExecutionTimeSeconds = Long.MAX_VALUE;
        String host = "http://172.17.0.2:1337";
        testCollector = new ErisTransactionCollector(host, runningExecutionTimeSeconds, transactionService);

        Field scheduledExecutorServiceField = testCollector.getClass().getDeclaredField("scheduledExecutorService");
        scheduledExecutorServiceField.setAccessible(true);
        scheduledExecutorService = (ScheduledExecutorService)scheduledExecutorServiceField.get(testCollector);
        scheduledExecutorService.shutdownNow();

        //get transaction helper
        Field transactionHelperField = testCollector.getClass().getDeclaredField("transactionHelper");
        transactionHelperField.setAccessible(true);

        transactionHelperField.set(testCollector, transactionHelperMock);
    }



}