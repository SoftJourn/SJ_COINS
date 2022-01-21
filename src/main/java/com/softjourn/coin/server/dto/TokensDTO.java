package com.softjourn.coin.server.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokensDTO {

  private String address;
  private BigDecimal amount;
}
