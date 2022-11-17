package com.softjourn.coin.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.softjourn.coin.server.service.FilterIgnore;
import com.softjourn.coin.server.util.InstantJsonSerializer;
import com.softjourn.coin.server.util.JsonViews;
import com.softjourn.coin.server.util.TransactionAccountJSONSerializer;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(name = "transactions")
@AllArgsConstructor
public class Transaction implements Serializable {

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

  @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
  @Enumerated(EnumType.STRING)
  private TransactionType type;

  @FilterIgnore
  private BigDecimal remain;

  @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
  @Column(columnDefinition = "text")
  private String error;

  @JsonIgnore
  @FilterIgnore
  private transient Object value;

  @JsonView(JsonViews.DETAILED.class)
  @FilterIgnore
  private String transactionId;

  public Transaction() {
  }

  public Transaction(String txId) {
    transactionId = txId;
  }
}
