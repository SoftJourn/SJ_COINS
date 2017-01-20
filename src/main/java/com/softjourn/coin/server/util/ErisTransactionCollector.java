package com.softjourn.coin.server.util;

import com.softjourn.coin.server.exceptions.ErisClientException;
import com.softjourn.eris.transaction.TransactionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ErisTransactionHelper
 * Created by vromanchuk on 12.01.17.
 */
@Component
public class ErisTransactionCollector implements Runnable {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private TransactionHelper transactionHelper;
    private BigInteger latestBlockHeight = BigInteger.ZERO;


    @Autowired
    public ErisTransactionCollector(@Value("${eris.chain.url}") String host
            , @Value("${eris.transaction.collector.interval}") Long interval) {
        this.transactionHelper = new TransactionHelper(host);
        scheduledExecutorService.schedule(this, interval, TimeUnit.SECONDS);
        scheduledExecutorService.submit(this);
    }

    @Override
    public void run() {
        System.out.println("Hello");
    }

    public List<Object> getMissedTransactions(BigInteger from, BigInteger to) throws ErisClientException {
        try {
//            if(to.add(transactionHelper.MAX_BLOCKS_PER_REQUEST).compareTo(to)>0)
            transactionHelper.getBlocks(from, to);
        } catch (IOException e) {
            throw new ErisClientException(e.getMessage());
        }
        return new ArrayList<>();
    }

    public BigInteger getDifference() throws ErisClientException {
        try {
            return this.transactionHelper.getLatestBlockNumber().subtract(latestBlockHeight);
        } catch (IOException e) {
            throw new ErisClientException(e.getMessage());
        }
    }

    public List<BigInteger> getBlockNumbersWithTransaction(BigInteger from, BigInteger to) throws ErisClientException {
        try {
            if (to.add(TransactionHelper.MAX_BLOCKS_PER_REQUEST).compareTo(to) > 0) {
//                System.out.println(
                transactionHelper.getBlocks(from, to)//.getBlockMetas());
                        .getBlockNumbersWithTransaction();
            }
        } catch (IOException e) {
            throw new ErisClientException(e.getMessage());
        }
        return new ArrayList<>();
    }
}
