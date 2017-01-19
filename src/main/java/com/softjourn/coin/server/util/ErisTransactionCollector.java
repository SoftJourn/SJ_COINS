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

    private String host;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private TransactionHelper transactionHelper;
    private BigInteger latestBlockHeight = BigInteger.ZERO;


    @Autowired
    public ErisTransactionCollector(@Value("${eris.chain.url}") String host) {
        this.host = host;
        this.transactionHelper = new TransactionHelper(host);
        scheduledExecutorService.scheduleAtFixedRate(this, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
//        this.getMissedTransactions();
    }

    public List<Object> getMissedTransactions(BigInteger from, BigInteger to) {
        transactionHelper.getBlocks();
        return new ArrayList<>();
    }

    public BigInteger getDifference() throws ErisClientException {
        try {
            return this.transactionHelper.getLatestBlockNumber().subtract(latestBlockHeight);
        } catch (IOException ex) {
            throw new ErisClientException(ex.getMessage());
        }
    }
}
