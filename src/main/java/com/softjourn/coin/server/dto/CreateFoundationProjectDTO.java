package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateFoundationProjectDTO {

  private String name;
  private String image;
  private Integer fundingGoal;
  private Long deadline;
  private boolean closeOnGoalReached;
  private Integer categoryId;
}
