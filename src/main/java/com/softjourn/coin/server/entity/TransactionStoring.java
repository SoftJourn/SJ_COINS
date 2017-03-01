package com.softjourn.coin.server.entity;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.eris.transaction.pojo.ErisCallTransaction;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * TransactionStoring is an object that will have all valuable information
 * about body. It should contain information that can be checked via blockchain
 * Created by vromanchuk on 17.01.17.
 */
@Data
@Entity
@Table(name = "transaction_history",
        uniqueConstraints = @UniqueConstraint(name = "eris_tx_id_unique_index", columnNames = {"tx_id"}))
@NoArgsConstructor
public class TransactionStoring implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "BIGINT")
    private Long blockNumber;
    private LocalDateTime time;
    private String chainId;
    private String txId;

    @Embedded
    private ErisTransactionDAO transaction;

    @ElementCollection
    @JoinTable(name = "tx_calling_data", joinColumns = @JoinColumn(name = "tx_id"))
    @MapKeyColumn(name = "function_name")
    private Map<String, String> callingValue;

    public TransactionStoring(@NonNull ErisTransactionDAO transaction) {
        if(transaction.getBlockHeader() != null) {
            this.blockNumber = transaction.getBlockHeader().getBlockNumber();
            this.time = transaction.getBlockHeader().getTimeCreated();
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
