package com.softjourn.coin.server.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.softjourn.eris.block.pojo.BlockHeader;
import com.softjourn.eris.transaction.pojo.ErisCallTransaction;
import com.softjourn.eris.transaction.pojo.ErisTransactionType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

import javax.persistence.*;
import java.util.Map;

/**
 * ErisTransactionDAO created to map hibernate entity to ErisTransaction
 * Created by vromanchuk on 18.01.17.
 */
@Embeddable
@Access(AccessType.PROPERTY)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ErisTransactionDAO {

    @Delegate(excludes = ICallingData.class)
    private ErisCallTransaction transaction = ErisCallTransaction.builder().build();

    @Transient
    @JsonIgnore
    public void setTransaction(ErisCallTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public String toString() {
        return transaction.toString();
    }

    @Column(columnDefinition = "TEXT")
    public String getCallingData() {
        return transaction.getCallingData();
    }

    @Transient
    @JsonIgnore
    public String getTxId() {
        return transaction.getTxId();
    }

    @Transient
    @JsonIgnore
    public BlockHeader getBlockHeader(){
        return transaction.getBlockHeader();
    }

    @Transient
    @JsonIgnore
    public Map<String, String> getFunctionArguments() {
        return transaction.getFunctionArguments();
    }

    @SuppressWarnings("unused")
    interface ICallingData {
        String getCallingData();
        ErisTransactionType getTransactionType();
        String getTxId();
        Map getFunctionArguments();
        BlockHeader getBlockHeader();
    }
}

