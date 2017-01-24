package com.softjourn.coin.server.entity;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.eris.transaction.type.ErisTransaction;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
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
public class TransactionStoring {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private Long id;
    private BigInteger blockNumber;
    private LocalDateTime time;
    private String functionName;

    @Embedded
    private ErisTransactionDAO transaction;

    @ElementCollection
    @JoinTable(name = "tx_calling_data", joinColumns = @JoinColumn(name = "tx_id"))
    @MapKeyColumn(name = "function_name")
    private Map<String, String> callingValue;

    public TransactionStoring(BigInteger blockNumber, LocalDateTime time, ErisTransactionDAO transaction) {
        this.blockNumber = blockNumber;
        this.time = time;
        this.transaction = transaction;
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
