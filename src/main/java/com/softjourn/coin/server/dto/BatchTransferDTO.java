package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BatchTransferDTO {

    private String userId;

    private BigDecimal amount;

}
