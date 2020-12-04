package com.softjourn.coin.server.dto;

import java.util.List;
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
  private String adminAccount;
  private String creatorAccount;
  private Integer goal;
  private Integer deadlineInMinutes;
  private boolean closeOnGoalReached;
  private boolean withdrawAllowed;
  private List<String> currencies;
}
