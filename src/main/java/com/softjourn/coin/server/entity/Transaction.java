package com.softjourn.coin.server.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.softjourn.coin.server.util.InstantJsonSerializer;
import com.softjourn.coin.server.util.TransactionAccountJSONSerializer;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonSerialize(using = TransactionAccountJSONSerializer.class)
    private Account account;

    @ManyToOne
    @JsonSerialize(using = TransactionAccountJSONSerializer.class)
    private Account destination;

    private BigDecimal amount;

    @Column(columnDefinition = "text")
    private String comment;

    @JsonSerialize(using = InstantJsonSerializer.class)
    private Instant created;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private BigDecimal remain;

    @Column(columnDefinition = "text")
    private String error;

    @JsonIgnore
    private String erisTransactionId;

    @OneToOne
    @JoinColumn(name = "erisTransactionId", referencedColumnName = "txId", insertable = false, updatable = false)
    private TransactionStoring transactionStoring;

    public Transaction() {
    }

    public Transaction(String txId) {
        erisTransactionId = txId;
    }
}
