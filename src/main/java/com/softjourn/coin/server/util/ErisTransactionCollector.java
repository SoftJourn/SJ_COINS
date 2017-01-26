package com.softjourn.coin.server.util;

import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.exceptions.ErisClientException;
import com.softjourn.coin.server.service.ErisTransactionService;
import com.softjourn.eris.transaction.TransactionHelper;
import com.softjourn.eris.transaction.type.Block;
import com.softjourn.eris.transaction.type.Blocks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ErisTransactionHelper
 * Created by vromanchuk on 12.01.17.
 */
@Component
@Slf4j

public class ErisTransactionCollector implements Runnable {

    private static int MAX_ERRORS_IN_SEQUENCE = 10;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private TransactionHelper transactionHelper;
    private ErisTransactionService transactionService;
    private BigInteger lastSavedBlockHeightWithTx;
    private int errorInSequenceCount;


    @Autowired
    public ErisTransactionCollector(@Value("${eris.chain.url}") String host
            , @Value("${eris.transaction.collector.interval}") Long interval
            , ErisTransactionService transactionService) {
        this.transactionHelper = new TransactionHelper(host);
        this.transactionService = transactionService;
        scheduledExecutorService.scheduleAtFixedRate(this, interval, 3, TimeUnit.SECONDS);
        scheduledExecutorService.submit(this);
    }

    @Override
    public void run() {
        try {
            BigInteger lastProduced = transactionHelper.getLatestBlockNumber().add(BigInteger.ONE);
            lastSavedBlockHeightWithTx = transactionService.getHeightLastStored().add(BigInteger.ONE);
            Stream<BigInteger> blocksWithTx = this.getBlockNumbersWithTransaction(this.lastSavedBlockHeightWithTx, lastProduced);
            Stream<TransactionStoring> transactions = this.getTransactionsFromBlocks(blocksWithTx);
            this.transactionService.storeTransaction(transactions);
            errorInSequenceCount = 0;
        } catch (Exception e) {
            errorInSequenceCount++;
            log.warn("Cyclic error in collector scheduler. Please fix me ...", e);
            if(errorInSequenceCount > MAX_ERRORS_IN_SEQUENCE) {
                log.error("Scheduler stopped due to a lot of errors in a sequence");
                scheduledExecutorService.shutdown();
            }
        }
    }


    public Stream<BigInteger> getBlockNumbersWithTransaction(BigInteger from, BigInteger to) throws ErisClientException {
        return Blocks.getBlockNumbersWithTransaction(transactionHelper.getBlockStream(from, to));
    }


    public List<TransactionStoring> getTransactionsFromBlock(BigInteger blockNumber) {
        try {
            Block block = transactionHelper.getBlock(blockNumber);
            return transactionService.getTransactionStoring(block);
        } catch (Exception e) {
            log.warn("Block can't parse transactions", e);
            return null;
        }
    }

    public List<TransactionStoring> getTransactionsFromBlocks(List<BigInteger> blockNumbers) throws ErisClientException {
        return getTransactionsFromBlocks(blockNumbers.stream()).collect(Collectors.toList());
    }

    public Stream<TransactionStoring> getTransactionsFromBlocks(Stream<BigInteger> blockNumbers) throws ErisClientException {
        return blockNumbers
                .map(this::getTransactionsFromBlock)
                .flatMap(List::stream);
    }

}
