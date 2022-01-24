package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalDTO {

  private Integer id;
  private String userId;
  private Integer amount;
  private String createdAt;
  private String note;
}
