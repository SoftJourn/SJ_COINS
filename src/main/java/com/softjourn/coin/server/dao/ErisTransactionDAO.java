package com.softjourn.coin.server.dao;

import com.softjourn.eris.transaction.type.ErisTransaction;
import lombok.experimental.Delegate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

/**
 * ErisTransactionDAO created to map hibernate entity to ErisTransaction
 * Created by vromanchuk on 18.01.17.
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class ErisTransactionDAO {

    @Delegate
    private ErisTransaction transaction;

    public ErisTransactionDAO(ErisTransaction transaction) {
        this.transaction = transaction;
    }

    public ErisTransactionDAO() {
    }

    @Override
    public String toString() {
        return transaction.toString();
    }
}
