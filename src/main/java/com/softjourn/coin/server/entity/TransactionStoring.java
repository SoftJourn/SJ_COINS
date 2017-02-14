package com.softjourn.coin.server.entity;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.eris.transaction.pojo.ErisTransaction;
import com.softjourn.eris.transaction.pojo.Header;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * TransactionStoring is an object that will have all valuable information
 * about transaction. It should contain information that can be checked via blockchain
 * Created by vromanchuk on 17.01.17.
 */
@Data
@Entity
@Table(name = "transaction_history")
@NoArgsConstructor
public class TransactionStoring implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "BIGINT")
    private Long blockNumber;
    private LocalDateTime time;
    private String functionName;
    private String chainId;
    @Column(unique = true)
    private String txId;

    @Embedded
    private ErisTransactionDAO transaction;

    @ElementCollection
    @JoinTable(name = "tx_calling_data", joinColumns = @JoinColumn(name = "tx_id"))
    @MapKeyColumn(name = "function_name")
    private Map<String, String> callingValue;

    public TransactionStoring(Header blockHeader, String functionName, @NonNull ErisTransactionDAO transaction, Map<String, String> callingValue, String txId) {
        this.blockNumber = blockHeader.getHeight();
        this.time = blockHeader.getDateTime();
        this.functionName = functionName;
        this.chainId = blockHeader.getChainId();
        this.transaction = transaction;
        this.callingValue = callingValue;
        this.txId = txId;
    }

    public void setTransaction(ErisTransaction transaction) {
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
