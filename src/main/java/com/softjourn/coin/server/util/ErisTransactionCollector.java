package com.softjourn.coin.server.util;

import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.exceptions.ErisClientException;
import com.softjourn.coin.server.service.ErisTransactionService;
import com.softjourn.eris.transaction.TransactionHelper;
import com.softjourn.eris.transaction.type.Block;
import com.softjourn.eris.transaction.type.Blocks;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    private static final Marker marker = MarkerFactory.getMarker("TRANSACTION_MARKER");
    private static final int MAX_ERRORS_IN_SEQUENCE = 10;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private TransactionHelper transactionHelper;
    private ErisTransactionService transactionService;
    private Long lastCheckedBlockNumber = 0L;
    private int errorInSequenceCount;


    @Autowired
    public ErisTransactionCollector(@Value("${eris.chain.url}") String host
            , @Value("${eris.transaction.collector.interval}") Long interval
            , ErisTransactionService transactionService) {
        this.transactionHelper = new TransactionHelper(host);
        this.transactionService = transactionService;
        scheduledExecutorService.scheduleAtFixedRate(this, interval, interval, TimeUnit.SECONDS);
        scheduledExecutorService.submit(this);
        lastCheckedBlockNumber = transactionService.getHeightLastStored();
    }

    @Override
    public void run() {
        try {
            Long lastProduced = transactionHelper.getLatestBlockNumber();
            if (!lastProduced.equals(lastCheckedBlockNumber)) {
                log.trace(marker, "Calling blocks from "
                        + lastCheckedBlockNumber
                        + " to " + lastProduced);
                Stream<Long> blocksWithTx = this
                        .getBlockNumbersWithTransaction(lastCheckedBlockNumber + 1, lastProduced + 1);
                Stream<TransactionStoring> transactions = this.getTransactionsFromBlocks(blocksWithTx);
                this.transactionService.storeTransaction(transactions);
                errorInSequenceCount = 0;
                lastCheckedBlockNumber = lastProduced;
            }
        } catch (Exception e) {
            errorInSequenceCount++;
            log.warn("Cyclic error in collector scheduler. Please fix me ...", e);
            if (errorInSequenceCount > MAX_ERRORS_IN_SEQUENCE) {
                log.error("Scheduler stopped due to a lot of errors in a sequence");
                scheduledExecutorService.shutdown();
            }
        }
    }


    public Stream<Long> getBlockNumbersWithTransaction(Long from, Long to) throws ErisClientException {
        return Blocks.getBlockNumbersWithTransaction(transactionHelper.getBlockStream(from, to));
    }


    public List<TransactionStoring> getTransactionsFromBlock(Long blockNumber) {
        try {
            Block block = transactionHelper.getBlock(blockNumber);
            return transactionService.getTransactionStoring(block);
        } catch (Exception e) {
            log.warn("Block can't parse transactions", e);
            return null;
        }
    }

    public List<TransactionStoring> getTransactionsFromBlocks(List<Long> blockNumbers) throws ErisClientException {
        return getTransactionsFromBlocks(blockNumbers.stream()).collect(Collectors.toList());
    }

    public Stream<TransactionStoring> getTransactionsFromBlocks(Stream<Long> blockNumbers) throws ErisClientException {
        return blockNumbers
                .map(this::getTransactionsFromBlock)
                .flatMap(List::stream);
    }

}
