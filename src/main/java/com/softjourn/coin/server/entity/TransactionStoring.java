package com.softjourn.coin.server.entity;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import lombok.Data;

import javax.persistence.*;
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
public class TransactionStoring {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private BigInteger blockNumber;
    private LocalDateTime time;

    @Embedded
    private ErisTransactionDAO transaction;
}
