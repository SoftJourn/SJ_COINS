package com.softjourn.coin.server.dto;

import com.softjourn.coin.server.entity.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class CheckDTO {

    private Long total;

    private Long isDone;

    private List<Transaction> transactions;

}
