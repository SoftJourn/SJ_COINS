package com.softjourn.coin.server.dao;

import com.softjourn.eris.block.pojo.BlockHeader;
import com.softjourn.eris.transaction.pojo.ErisCallTransaction;
import com.softjourn.eris.transaction.pojo.ErisTransaction;
import com.softjourn.eris.transaction.pojo.ErisTransactionType;
import lombok.EqualsAndHashCode;
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
public class ErisTransactionDAO {

    @Delegate(excludes = ICallingData.class)
    private ErisCallTransaction transaction = ErisCallTransaction.builder().build();

    public ErisTransactionDAO(ErisCallTransaction transaction) {
        this.transaction = transaction;
    }

    public ErisTransactionDAO() {
    }

    @Transient
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

    @SuppressWarnings("unused")
    interface ICallingData {
        String getCallingData();
        BlockHeader getBlockHeader();
        Map getFunctionArguments();
        ErisTransactionType getTransactionType();
    }
}
