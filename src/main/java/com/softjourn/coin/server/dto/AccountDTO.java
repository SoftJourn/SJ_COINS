package com.softjourn.coin.server.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDTO {

    private String account;

    private BigDecimal coins;

}
