package com.softjourn.coin.server.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Calling params at eris transactions
 * Created by vromanchuk on 23.01.17.
 */
@Entity
@Data
public class ErisCallingData {

    @ManyToOne(fetch = FetchType.LAZY)
    TransactionStoring transactionStoring;
    @Id
    private String id;
    private String paramName;
    private String value;
}
