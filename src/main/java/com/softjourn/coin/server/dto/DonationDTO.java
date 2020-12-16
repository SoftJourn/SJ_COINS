package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationDTO {

  private String userId;
  private String userAccountType;
  private String currency;
  private Integer amount;
}
