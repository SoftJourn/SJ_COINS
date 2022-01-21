package com.softjourn.coin.server.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchTransferDTO {

  private String userId;
  private BigDecimal amount;
}
