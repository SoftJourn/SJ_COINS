package com.softjourn.coin.server.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.softjourn.coin.server.service.FilterIgnore;
import com.softjourn.coin.server.util.InstantJsonSerializer;
import com.softjourn.coin.server.util.JsonViews;
import com.softjourn.coin.server.util.TransactionAccountJSONSerializer;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "transactions")
public class Transaction<T> implements Serializable {

    @FilterIgnore
    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    @ManyToOne
    @JsonSerialize(using = TransactionAccountJSONSerializer.class)
    private Account account;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    @ManyToOne
    @JsonSerialize(using = TransactionAccountJSONSerializer.class)
    private Account destination;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    private BigDecimal amount;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    @Column(columnDefinition = "text")
    private String comment;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    @JsonSerialize(using = InstantJsonSerializer.class)
    private Instant created;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @JsonIgnore
    @FilterIgnore
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @FilterIgnore
    private BigDecimal remain;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    @Column(columnDefinition = "text")
    private String error;

    @JsonIgnore
    @FilterIgnore
    private transient T value;

    @JsonIgnore
    private String erisTransactionId;

    @JsonView(JsonViews.DETAILED.class)
    @OneToOne
    @JoinColumn(name = "erisTransactionId", referencedColumnName = "txId", insertable = false, updatable = false)
    private TransactionStoring transactionStoring;

    public Transaction() {
    }

    public Transaction(String txId) {
        erisTransactionId = txId;
    }
}
