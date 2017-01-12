package com.softjourn.coin.server.blockchain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Block from eris block chain
 * Created by vromanchuk on 12.01.17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Block {

    private Data data;
    private Object header;
    @JsonProperty("last_commit")
    private Object lastCommit;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Object getHeader() {
        return header;
    }

    public void setHeader(Object header) {
        this.header = header;
    }

    public Object getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(Object lastCommit) {
        this.lastCommit = lastCommit;
    }
}
