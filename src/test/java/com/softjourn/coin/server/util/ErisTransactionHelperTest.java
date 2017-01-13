package com.softjourn.coin.server.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.blockchain.Block;
import com.softjourn.coin.server.blockchain.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void getBlock() throws Exception {
        String blockJSON = transactionHelper.getBlock(new BigInteger("10"));

        assertNotNull(blockJSON);
        assertFalse(blockJSON.isEmpty());
//        System.out.println(blockJSON);

        Block block = objectMapper.readValue(blockJSON, Block.class);
        assertNotNull(block.getHeader());
        assertNotNull(block.getLastCommit());
        assertNotNull(block.getData());

        //Check transactions
        assertNotNull(block.getData().getTransactionsBites());
        assertTrue(block.getData().getTransactionsBites().size() > 0);
        String transactionString = block.getData().getTransactionsBites().get(0);
        Transaction transaction = new Transaction(transactionString);
//        System.out.println(transaction);

        //Check header
        System.out.println(block.getHeader());
        String stringTime = block.getHeader().getTime();
        //ISO_OFFSET_DATE_TIME or ISO_ZONED_DATE_TIME or ISO_DATE_TIME
        LocalDateTime localDateTime = LocalDateTime.parse(stringTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        System.out.println(localDateTime);

//        ContractUnit unit = contractUnits.get(contractUnitName);
    }
}
