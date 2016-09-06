package com.softjourn.coin.server.entity;


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
    @JoinColumn(name = "account_id")
    @JsonSerialize(using = TransactionAccountJSONSerializer.class)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "destination_id")
    @JsonSerialize(using = TransactionAccountJSONSerializer.class)
    private Account destination;

    @Column
    private BigDecimal amount;

    @Column
    private String comment;

    @Column
    @JsonSerialize(using = InstantJsonSerializer.class)
    private Instant created;

    @Column
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column
    private BigDecimal remain;

    @Column
    private String error;
}
