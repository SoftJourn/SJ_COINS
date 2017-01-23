package com.softjourn.coin.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.dao.ErisTransactionDAO;
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
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class ErisTransactionCollectorTest {

    private BigInteger lastBlock = BigInteger.TEN;
    private String host = "http://172.17.0.1:1337";
    private ErisTransactionCollector testCollector = new ErisTransactionCollector(host, 30L);
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {

        //get transaction helper
        Field transactionHelperField = testCollector.getClass().getDeclaredField("transactionHelper");
        transactionHelperField.setAccessible(true);
        TransactionHelper transactionHelperMock = mock(TransactionHelper.class);
        transactionHelperField.set(testCollector, transactionHelperMock);
        when(transactionHelperMock.getLatestBlockNumber()).thenReturn(lastBlock);

        int[] blocksInitArray = new int[9];
        List<BlockMeta> blockMetas = Arrays.stream(blocksInitArray)
                .mapToObj((i) -> new BlockMeta())
                .peek(block -> block.setHeader(new Header()))
                .peek(blockMeta -> blockMeta.getHeader().setNumTxs(0))
                .collect(Collectors.toList());

        Blocks blocks = new Blocks();
        blocks.setBlockMetas(blockMetas);
        when(transactionHelperMock.getBlocks(BigInteger.ONE, BigInteger.TEN)).thenReturn(blocks);

        blockMetas = new ArrayList<>(blockMetas);
        blockMetas.add(null);
        blocks = new Blocks();
        blocks.setBlockMetas(blockMetas);
        when(transactionHelperMock.getBlocks(BigInteger.ONE, BigInteger.TEN.add(BigInteger.ONE))).thenReturn(blocks);

        blockMetas = new ArrayList<>(blockMetas);
        BlockMeta blockMetaWithTx = new BlockMeta();
        Header header = new Header();
        header.setNumTxs(1);
        header.setHeight(BigInteger.valueOf(12));
        blockMetaWithTx.setHeader(header);
        blockMetas.add(blockMetaWithTx);
        blocks = new Blocks();
        blocks.setBlockMetas(blockMetas);
        when(transactionHelperMock.getBlocks(BigInteger.ONE, BigInteger.valueOf(12))).thenReturn(blocks);

        File file;
        String json;
        Block block;

        file = new File("src/test/resources/json/block27.json");
        json = new Scanner(file).useDelimiter("\\Z").next();
        block = mapper.readValue(json, Block.class);
        when(transactionHelperMock.getBlock(BigInteger.valueOf(27))).thenReturn(block);

        file = new File("src/test/resources/json/block33.json");
        json = new Scanner(file).useDelimiter("\\Z").next();
        block = mapper.readValue(json, Block.class);
        when(transactionHelperMock.getBlock(BigInteger.valueOf(33))).thenReturn(block);

    }


    @Test
    public void run() throws Exception {
    }

    @Test
    public void getMissedTransactions() throws Exception {
        assertNotNull(testCollector.getMissedTransactions(BigInteger.ZERO, BigInteger.TEN));
        assertThat(testCollector.getMissedTransactions(BigInteger.ZERO, BigInteger.TEN), instanceOf(List.class));
    }

    @Test
    public void getBlockNumbersWithTransaction_1_10_EmptyList() throws Exception {
        List<BigInteger> list = testCollector.getBlockNumbersWithTransaction(BigInteger.ONE, BigInteger.TEN);
        assertTrue(list.isEmpty());
    }

    @Test
    public void getBlockNumbersWithTransaction_1_11_EmptyList() throws Exception {
        List<BigInteger> list = testCollector
                .getBlockNumbersWithTransaction(BigInteger.ONE, BigInteger.TEN.add(BigInteger.ONE));
        assertTrue(list.isEmpty());
    }

    @Test
    public void getBlockNumbersWithTransaction_1_12_BlockHeight11() throws Exception {
        List<BigInteger> list = testCollector.getBlockNumbersWithTransaction(BigInteger.ONE, BigInteger.valueOf(12));
        assertFalse(list.isEmpty());
        assertTrue(list.contains(BigInteger.valueOf(12)));
    }

    @Test
    public void getDifference() throws Exception {
        assertNotNull(testCollector.getDifference());
        assertEquals(lastBlock, testCollector.getDifference());
    }

    @Test
    public void getTransactionsFromBlock_BlockNumber_ListOfTransactions() throws Exception {
        List<ErisTransactionDAO> transactions;
        transactions = testCollector.getTransactionsFromBlock(BigInteger.valueOf(27));
        assertEquals(1, transactions.size());
        transactions = testCollector.getTransactionsFromBlock(BigInteger.valueOf(33));
        assertEquals(1, transactions.size());
    }

    @Test
    public void getTransactionsFromBlocks_ListBlockNumbers_ListOfTransactions() throws Exception {
        List<BigInteger> blockNumbers = new ArrayList<>();
        blockNumbers.add(BigInteger.valueOf(27));
        blockNumbers.add(BigInteger.valueOf(33));
        List<ErisTransactionDAO> transactions;
        transactions = testCollector.getTransactionsFromBlocks(blockNumbers);
        assertEquals(2, transactions.size());
        System.out.println(transactions);
    }
}