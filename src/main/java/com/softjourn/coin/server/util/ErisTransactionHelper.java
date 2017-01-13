package com.softjourn.coin.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.blockchain.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigInteger;

/**
 * ErisTransactionHelper
 * Created by vromanchuk on 12.01.17.
 */
@Component
public class ErisTransactionHelper {

    private ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;
    private String host;

    @Autowired
    public ErisTransactionHelper(RestTemplate restTemplate, @Value("${eris.chain.url}") String host) {
        this.restTemplate = restTemplate;
        this.host = host;
    }

    public Block getBlock(BigInteger blockNumber) throws IOException {
        String blockJSON = this.getBlockJSON(blockNumber);
        return objectMapper.readValue(blockJSON, Block.class);
    }

    public String getBlockJSON(BigInteger blockNumber) {
        String endpoint = "/blockchain/block";
        String url = host + endpoint + "/" + blockNumber.toString();
        return restTemplate.getForEntity(url, String.class).getBody();
    }

    public Block getLatestBlock() throws IOException {
        String endpoint = "/blockchain/latest_block";
        String url = host + endpoint;
        String blockJSON = restTemplate.getForEntity(url, String.class).getBody();
        return objectMapper.readValue(blockJSON, Block.class);
    }

    public BigInteger getLatestBlockNumber() throws IOException {
        Block latestBlock = this.getLatestBlock();
        return latestBlock.getHeader().getHeight();
    }
}
