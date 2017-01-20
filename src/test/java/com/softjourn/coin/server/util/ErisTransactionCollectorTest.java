package com.softjourn.coin.server.util;

import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class ErisTransactionCollectorTest {

    private BigInteger lastBlock = BigInteger.TEN;
    private String host = "http://172.17.0.1:1337";
    private ErisTransactionCollector testCollector = new ErisTransactionCollector(host, 30L);

//    @Before
//    public void setUp() throws Exception {
//        //get transaction helper
//        Field transactionHelperField = testCollector.getClass().getDeclaredField("transactionHelper");
//        transactionHelperField.setAccessible(true);
//        TransactionHelper transactionHelperMock = mock(TransactionHelper.class);
//        transactionHelperField.set(testCollector,transactionHelperMock);
//        when(transactionHelperMock.getLatestBlockNumber()).thenReturn(lastBlock);
//    }

    @Test
    public void run() throws Exception {
//
//        Thread.sleep(2000L);
    }

    @Test
    public void getMissedTransactions() throws Exception {
        assertNotNull(testCollector.getMissedTransactions(BigInteger.ZERO, BigInteger.TEN));
        assertThat(testCollector.getMissedTransactions(BigInteger.ZERO, BigInteger.TEN), instanceOf(List.class));
    }

    @Test
    public void getBlockNumbersWithTransaction() throws Exception {
        //TODO getBlockNumbersWithTransaction returns Null pointer on collect function
        testCollector.getBlockNumbersWithTransaction(BigInteger.ZERO, BigInteger.TEN);
    }

    @Test
    public void getDifference() throws Exception {
        assertNotNull(testCollector.getDifference());
        assertEquals(lastBlock, testCollector.getDifference());
    }
}