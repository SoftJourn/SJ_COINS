package com.softjourn.coin.server.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data in Eris block
 * Created by vromanchuk on 12.01.17.
 */
public class Data {

    @JsonProperty(value = "txs")
    private List<String> transactionsBites;

    private List<Transaction> transactions;

    public List<String> getTransactionsBites() {
        return transactionsBites;
    }

    @SuppressWarnings("unused")
    @JsonSetter(value = "txs")
    private void setTransactionsBites(List<String> transactionsBites) {
        if (this.transactions == null) {
            this.transactionsBites = transactionsBites;
            this.transactions = transactionsBites.stream().map(Transaction::new).collect(Collectors.toList());
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
