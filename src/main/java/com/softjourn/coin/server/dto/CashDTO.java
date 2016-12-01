package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashDTO {

    private String tokenContractAddress;

    private String offlineContractAddress;

    private String chequeHash;

    private BigInteger amount;

}
