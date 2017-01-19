package com.softjourn.coin.server.util;

import com.softjourn.eris.transaction.TransactionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ErisTransactionHelper
 * Created by vromanchuk on 12.01.17.
 */
@Component
public class ErisTransactionCollector implements Runnable {

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private String host;
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
        this.getMissedTransactions();
    }

    private void getMissedTransactions() {

    }
}
