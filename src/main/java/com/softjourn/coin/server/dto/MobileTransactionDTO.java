package com.softjourn.coin.server.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.util.InstantJsonSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Data
public class MobileTransactionDTO {

    private Long id;

    private String account;

    private String destination;

    private BigDecimal amount;

    private String comment;

    @JsonSerialize(using = InstantJsonSerializer.class)
    private Instant created;

    private TransactionStatus status;

    private BigDecimal remain;

    private String error;

    public MobileTransactionDTO(Transaction transaction) {
        id = transaction.getId();
        account = Optional.ofNullable(transaction.getAccount()).map(Account::getFullName).orElse("Replenishing.");
        destination = Optional.ofNullable(transaction.getAccount()).map(Account::getFullName).orElse("Withdrawing.");
        amount = transaction.getAmount();
        comment = transaction.getComment();
        created = transaction.getCreated();
        status = transaction.getStatus();
        remain = transaction.getRemain();
        error = transaction.getError();
    }
}
