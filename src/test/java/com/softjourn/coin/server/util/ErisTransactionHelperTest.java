package com.softjourn.coin.server.util;


import com.softjourn.coin.server.blockchain.Block;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * ErisTransactionHelperTest
 * Created by vromanchuk on 12.01.17.
 */
@RunWith(SpringRunner.class)
public class ErisTransactionHelperTest {

    String chainUrl = "http://172.17.0.1:1337";

    RestTemplate restTemplate = new RestTemplate();

    private ErisTransactionHelper transactionHelper = new ErisTransactionHelper();
    private BigInteger blockNumber = new BigInteger("10");

    @Test
    public void getBlockJSON() throws Exception {
        String blockJSON = transactionHelper.getBlockJSON(blockNumber);

        assertNotNull(blockJSON);
        assertFalse(blockJSON.isEmpty());
//        System.out.println(blockJSON);


//        //Check transactions
//        assertNotNull(block.getData().getTransactionsBites());
//        assertTrue(block.getData().getTransactionsBites().size() > 0);
//        String transactionString = block.getData().getTransactionsBites().get(0);
//        Transaction transaction = new Transaction(transactionString);
////        System.out.println(transaction);
//
//        //Check header
//        System.out.println(block.getHeader());
//        String stringTime = block.getHeader().getTime();
//        //ISO_OFFSET_DATE_TIME or ISO_ZONED_DATE_TIME or ISO_DATE_TIME
//        LocalDateTime localDateTime = LocalDateTime.parse(stringTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//
//        System.out.println(localDateTime);

//        ContractUnit unit = contractUnits.get(contractUnitName);
    }

    @Test
    public void getBlock_BlockN10_BlockN10() throws Exception {
        assertThat(transactionHelper.getBlock(blockNumber), instanceOf(Block.class));
        Block block = transactionHelper.getBlock(blockNumber);
        assertNotNull(block);
        assertNotNull(block.getHeader());
        assertNotNull(block.getLastCommit());
        assertNotNull(block.getData());
        assertEquals(blockNumber, block.getHeader().getHeight());
    }

    @Test
    public void getBlock_BlockN3847_BlockN3847() throws Exception {
        BigInteger blockN3847 = new BigInteger("3847");
        assertThat(transactionHelper.getBlock(blockN3847), instanceOf(Block.class));
        Block block = transactionHelper.getBlock(blockN3847);
        assertEquals(blockN3847, block.getHeader().getHeight());
    }

    @Test
    public void getLatestBlock_Block() throws Exception {
        assertThat(transactionHelper.getLatestBlock(), instanceOf(Block.class));
        Block block = transactionHelper.getLatestBlock();
        assertNotNull(block);
    }

    @Test
    public void getLatestBlockNumber_BigInteger() throws Exception {
        assertThat(transactionHelper.getLatestBlockNumber(), instanceOf(BigInteger.class));
        assertNotNull(transactionHelper.getLatestBlockNumber());
    }
}
