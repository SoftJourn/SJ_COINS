package com.softjourn.coin.server.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Data in Eris block
 * Created by vromanchuk on 12.01.17.
 */
public class Data {

    @JsonProperty(value = "txs")
    private List<String> transactions;

    public List<String> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }
}
