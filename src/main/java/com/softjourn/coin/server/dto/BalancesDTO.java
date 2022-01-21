package com.softjourn.coin.server.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BalancesDTO {

  private String userId;
  private BigDecimal balance;
}
