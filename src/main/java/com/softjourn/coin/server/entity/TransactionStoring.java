package com.softjourn.coin.server.entity;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDateTime;

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

    @Embedded
    private ErisTransactionDAO transaction;

    public TransactionStoring(BigInteger blockNumber, LocalDateTime time, ErisTransactionDAO transaction) {
        this.blockNumber = blockNumber;
        this.time = time;
        this.transaction = transaction;
    }
}
