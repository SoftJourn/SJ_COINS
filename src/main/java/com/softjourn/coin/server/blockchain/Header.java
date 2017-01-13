package com.softjourn.coin.server.blockchain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private LocalDateTime dateTime;

    public String getChainId() {
        return chainId;
    }

    public String getHeight() {
        return height;
    }

    public String getTime() {
        return time;
    }

    @SuppressWarnings("unused")
    private void setTime(String time) {
        if (this.time == null) {
            this.time = time;
            this.dateTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
