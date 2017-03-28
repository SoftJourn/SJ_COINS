package com.softjourn.coin.server.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.coin.server.service.FilterIgnore;
import com.softjourn.coin.server.util.InstantJsonSerializer;
import com.softjourn.eris.transaction.pojo.ErisCallTransaction;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * TransactionStoring is an object that will have all valuable information
 * about body. It should contain information that can be checked via blockchain
 * Created by vromanchuk on 17.01.17.
 */
@Data
@Entity
@Table(name = "transaction_history",
        uniqueConstraints = @UniqueConstraint(name = "eris_tx_id_unique_index", columnNames = {"txId"}))
@NoArgsConstructor
public class TransactionStoring implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @FilterIgnore
    private Long id;

    @Column(columnDefinition = "BIGINT")
    private Long blockNumber;
    @JsonSerialize(using = InstantJsonSerializer.class)
    private Instant time;
    private String chainId;
    private String txId;

    @Embedded
    @FilterIgnore
    private ErisTransactionDAO transaction;

    @ElementCollection
    @JoinTable(name = "tx_calling_data", joinColumns = @JoinColumn(name = "tx_id"))
    @MapKeyColumn(name = "function_name")
    @FilterIgnore
    private Map<String, String> callingValue;

    public TransactionStoring(@NonNull ErisTransactionDAO transaction) {
        if(transaction.getBlockHeader() != null) {
            this.blockNumber = transaction.getBlockHeader().getBlockNumber();
            this.time = transaction.getBlockHeader().getTimeCreated().toInstant(ZoneOffset.UTC);
            this.chainId = transaction.getBlockHeader().getChainName();
        }
        this.transaction = transaction;
        this.callingValue = transaction.getFunctionArguments();
        this.txId = this.transaction.getTxId();
    }

    public void setTransaction(ErisCallTransaction transaction) {
        if (this.transaction == null) {
            this.transaction = new ErisTransactionDAO(transaction);
        } else {
            this.transaction.setTransaction(transaction);
        }
    }

    public void setTransaction(ErisTransactionDAO transaction) {
        this.transaction = transaction;
    }
}
