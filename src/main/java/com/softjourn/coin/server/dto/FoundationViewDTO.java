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
public class FoundationViewDTO {

  private String name;
  private String image;
  private String adminId;
  private String creatorId;
  private Integer fundingGoal;
  private Integer collectedAmount;
  private Integer remainsAmount;
  private String deadline;
  private boolean closeOnGoalReached;
  private boolean withdrawAllowed;
  private String mainCurrency;
  private boolean fundingGoalReached;
  private boolean isContractClosed;
  private boolean isDonationReturned;
  private Map<String, Boolean> acceptCurrencies;
  private Map<String, Integer> allowanceMap;
  private Integer categoryId;
  private Integer status;

  @Override
  public String toString() {
    return "[" +
        "name=" + getName() + ", " +
        "adminId=" + getAdminId() + ", " +
        "creatorId=" + getCreatorId() + ", " +
        "fundingGoal=" + getFundingGoal() + ", " +
        "closeOnGoalReached=" + isCloseOnGoalReached() + ", " +
        "withdrawAllowed=" + isWithdrawAllowed() + ", " +
        "mainCurrency=" + getMainCurrency() + ", " +
        "acceptCurrencies=" + getAcceptCurrencies() +
        "]";
  }
}
