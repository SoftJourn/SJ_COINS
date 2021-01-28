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
  private String image;
  private String adminId;
  private String creatorId;
  private Integer fundingGoal;
  private Long deadline;
  private boolean closeOnGoalReached;
  private boolean withdrawAllowed;
  private String mainCurrency;
  private Map<String, Boolean> acceptCurrencies;

  @Override
  public String toString() {
    return "[" +
        "name=" + getName() + ", " +
        "adminId=" + getAdminId() + ", " +
        "creatorId=" + getCreatorId() + ", " +
        "fundingGoal=" + getFundingGoal() + ", " +
        "deadline=" + getDeadline() + ", " +
        "closeOnGoalReached=" + isCloseOnGoalReached() + ", " +
        "withdrawAllowed=" + isWithdrawAllowed() + ", " +
        "mainCurrency=" + getMainCurrency() + ", " +
        "acceptCurrencies=" + getAcceptCurrencies() +
        "]";
  }
}
