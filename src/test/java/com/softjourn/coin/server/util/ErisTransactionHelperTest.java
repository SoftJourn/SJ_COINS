package com.softjourn.coin.server.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.blockchain.Block;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * ErisTransactionHelperTest
 * Created by vromanchuk on 12.01.17.
 */
@RunWith(SpringRunner.class)
public class ErisTransactionHelperTest {

    String chainUrl = "http://172.17.0.1:1337";

    RestTemplate restTemplate = new RestTemplate();

    ErisTransactionHelper transactionHelper = new ErisTransactionHelper();

    @Test
    public void getBlock() throws Exception {
        String blockJSON = transactionHelper.getBlock(new BigInteger("10"));
        assertNotNull(blockJSON);
        assertFalse(blockJSON.isEmpty());
        ObjectMapper objectMapper = new ObjectMapper();
        Block block = objectMapper.readValue(blockJSON, Block.class);
        assertNotNull(block.getHeader());
        assertNotNull(block.getLastCommit());
        assertNotNull(block.getData());
        assertNotNull(block.getData().getTransactions());
    }
}
