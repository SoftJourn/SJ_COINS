package com.softjourn.coin.server.service;

import com.softjourn.eris.block.BlockChainService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ErisTransactionHelper
 * Created by vromanchuk on 12.01.17.
 */
//@Component
@Slf4j
public class ErisTransactionCollector implements Runnable {

    private static final Marker marker = MarkerFactory.getMarker("TRANSACTION_MARKER");
    private static final int MAX_ERRORS_IN_SEQUENCE = 100;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private BlockChainService blockChainService;
    private ErisTransactionHistoryService transactionService;
    private AtomicLong lastCheckedBlockNumber = new AtomicLong(0);
    private int errorInSequenceCount;


    @Autowired
    public ErisTransactionCollector(BlockChainService blockChainService,
                                    @Value("${eris.transaction.collector.interval}") Long interval,
                                    ErisTransactionHistoryService transactionService) {
        this.blockChainService = blockChainService;
        this.transactionService = transactionService;
        scheduledExecutorService.scheduleWithFixedDelay(this, 20, interval, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            lastCheckedBlockNumber.set(transactionService.getHeightLastStored());
            Long lastProduced = blockChainService.getLatestBlockNumber();
            if (!lastProduced.equals(lastCheckedBlockNumber.get())) {
                log.trace(marker, "Calling blocks from " + lastCheckedBlockNumber + " to " + lastProduced);

                blockChainService.visitTransactionsFromBlocks(lastCheckedBlockNumber.get() + 1, lastProduced + 1);
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

    @PreDestroy
    private void close() {
        scheduledExecutorService.shutdownNow();
    }

}
