package com.softjourn.coin.server.dao;

import com.softjourn.eris.transaction.type.ErisTransaction;
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
        public String getCallingData();
    }
}
