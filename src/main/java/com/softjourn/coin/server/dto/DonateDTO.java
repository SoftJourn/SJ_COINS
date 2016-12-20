package com.softjourn.coin.server.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class DonateDTO {

    private String contractAddress;

    private String spenderAddress;

    private BigInteger amount;

}
