package com.softjourn.coin.server.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DonateDTO {

    private String coinAddress;

    private String spenderAddress;

    private BigDecimal amount;

}
