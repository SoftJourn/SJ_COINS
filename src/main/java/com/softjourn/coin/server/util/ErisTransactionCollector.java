package com.softjourn.coin.server.util;

import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.exceptions.ErisClientException;
import com.softjourn.coin.server.service.ErisTransactionService;
import com.softjourn.eris.transaction.TransactionHelper;
import com.softjourn.eris.transaction.type.Block;
import com.softjourn.eris.transaction.type.Blocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ErisTransactionHelper
 * Created by vromanchuk on 12.01.17.
 */
@Component
public class ErisTransactionCollector implements Runnable {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private TransactionHelper transactionHelper;
    private ErisTransactionService transactionService;
    private BigInteger latestBlockHeight = BigInteger.ONE;


    @Autowired
    public ErisTransactionCollector(@Value("${eris.chain.url}") String host
            , @Value("${eris.transaction.collector.interval}") Long interval
            , ErisTransactionService transactionService) {
        this.transactionHelper = new TransactionHelper(host);
        this.transactionService = transactionService;
        scheduledExecutorService.scheduleAtFixedRate(this, interval, interval, TimeUnit.SECONDS);
        scheduledExecutorService.submit(this);
    }

    @Override
    public void run() {
        try {
            BigInteger lastProduced = transactionHelper.getLatestBlockNumber();
            List<BigInteger> blocksWithTx = this.getBlockNumbersWithTransaction(this.latestBlockHeight, lastProduced);
            List<TransactionStoring> transactions = this.getTransactionsFromBlocks(blocksWithTx);
            this.transactionService.storeTransaction(transactions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public BigInteger getDifference() throws ErisClientException {
        try {
            return this.transactionHelper.getLatestBlockNumber().subtract(latestBlockHeight);
        } catch (IOException e) {
            throw new ErisClientException(e.getMessage());
        }
    }

    public List<BigInteger> getBlockNumbersWithTransaction(BigInteger from, BigInteger to) throws ErisClientException {
        return Blocks.getBlockNumbersWithTransaction(transactionHelper.getBlockStream(from, to));
    }


    public List<TransactionStoring> getTransactionsFromBlock(BigInteger blockNumber) throws ErisClientException {
        try {
            Block block = transactionHelper.getBlock(blockNumber);
            return transactionService.getTransactionStoring(block);
        } catch (Exception e) {
            throw new ErisClientException(e);
        }
    }

    public List<TransactionStoring> getTransactionsFromBlocks(List<BigInteger> blockNumbers) throws ErisClientException {
        return blockNumbers.stream()
                .map(this::getTransactionsFromBlock)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
