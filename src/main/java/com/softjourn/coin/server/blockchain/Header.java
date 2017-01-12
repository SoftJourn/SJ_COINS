package com.softjourn.coin.server.blockchain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Header of block in blockchain
 * Created by vromanchuk on 12.01.17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Header {

    @JsonProperty(value = "chain_id")
    private String chainId;

    @JsonProperty(value = "num_txs")
    private Integer numTxs;

    private String height;
    private String time;

    public String getChainId() {
        return chainId;
    }

    public String getHeight() {
        return height;
    }

    public String getTime() {
        return time;
    }

    public Integer getNumTxs() {
        return numTxs;
    }

    @Override
    public String toString() {
        return "Header{" +
                "chainId='" + chainId + '\'' +
                ", numTxs=" + numTxs +
                ", height='" + height + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
