package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.exceptions.ErisClientException;
import com.softjourn.eris.transaction.ErisTransactionService;
import com.softjourn.eris.transaction.pojo.Block;
import com.softjourn.eris.transaction.pojo.Blocks;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
    private ErisTransactionService transactionHelper;
    private ErisTransactionHistoryService transactionService;
    private AtomicLong lastCheckedBlockNumber = new AtomicLong(0);
    private int errorInSequenceCount;


    @Autowired
    public ErisTransactionCollector(@Value("${eris.chain.url}") String host,
                                    @Value("${eris.transaction.collector.interval}") Long interval,
                                    ErisTransactionHistoryService transactionService) {
        this.transactionHelper = new ErisTransactionService(host);
        this.transactionService = transactionService;
        scheduledExecutorService.scheduleWithFixedDelay(this, 20, interval, TimeUnit.SECONDS);
        //lastCheckedBlockNumber = new AtomicLong(transactionService.getHeightLastStored());
    }

    @Override
    public void run() {
        try {
            Long lastProduced = transactionHelper.getLatestBlockNumber();
            if (!lastProduced.equals(lastCheckedBlockNumber.get())) {
                log.trace(marker, "Calling blocks from " + lastCheckedBlockNumber + " to " + lastProduced);

                getBlockNumbersWithTransaction(lastCheckedBlockNumber.get() + 1, lastProduced + 1)
                        .flatMap(this::getTransactionsFromBlock)
                        .forEach(transactionService::storeTransaction);
                errorInSequenceCount = 0;
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


    Stream<Long> getBlockNumbersWithTransaction(Long from, Long to) throws ErisClientException {
        return Blocks.getBlockNumbersWithTransaction(transactionHelper.getBlockStream(from, to));
    }


    Stream<TransactionStoring> getTransactionsFromBlock(Long blockNumber) {
        try {
            long newValue = blockNumber > lastCheckedBlockNumber.get() ? blockNumber : lastCheckedBlockNumber.get();
            lastCheckedBlockNumber.set(newValue);
            Block block = transactionHelper.getBlock(blockNumber);
            return transactionService.getTransactionStoring(block);
        } catch (Exception e) {
            log.warn("Block can't parse transactions", e);
            return Stream.empty();
        }
    }

}
