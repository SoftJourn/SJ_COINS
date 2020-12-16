package com.softjourn.coin.server.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FoundationProjectDTO {

  private String name;
  private String adminId;
  private String creatorId;
  private Integer fundingGoal;
  private Integer deadline;
  private boolean closeOnGoalReached;
  private boolean withdrawAllowed;
  private String mainCurrency;
  private Map<String, Boolean> acceptCurrencies;
}
