package com.softjourn.coin.server.dao;

import com.softjourn.eris.transaction.type.ErisTransaction;
import com.softjourn.eris.transaction.type.NotValidTransactionException;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;

import javax.persistence.*;

/**
 * ErisTransactionDAO created to map hibernate entity to ErisTransaction
 * Created by vromanchuk on 18.01.17.
 */
@Embeddable
@Access(AccessType.PROPERTY)
@EqualsAndHashCode
public class ErisTransactionDAO {

    @Delegate(excludes = ICallingData.class)
    private ErisTransaction transaction = new ErisTransaction();

    public ErisTransactionDAO(ErisTransaction transaction) {
        this.transaction = transaction;
    }

    public ErisTransactionDAO() {
    }

    @Transient
    public void setTransaction(ErisTransaction transaction) {
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

    interface ICallingData {
        String getCallingData();
        Long getAmountLongValue() throws NotValidTransactionException;
        Long getSequenceLongValue() throws NotValidTransactionException;
        Long getGasLimitLongValue() throws NotValidTransactionException;
        Long getFeeLongValue() throws NotValidTransactionException;
    }

    @Transient
    public Long getAmountLongValue() throws NotValidTransactionException {
        return transaction.getAmountLongValue();
    }
    @Transient
    public Long getSequenceLongValue() throws NotValidTransactionException {
        return transaction.getSequenceLongValue();
    }

    @Transient
    public Long getGasLimitLongValue() throws NotValidTransactionException {
        return transaction.getGasLimitLongValue();
    }

    @Transient
    public Long getFeeLongValue() throws NotValidTransactionException {
        return transaction.getFeeLongValue();
    }
}
