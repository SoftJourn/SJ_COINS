package com.softjourn.coin.server.dto;

import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFoundationDTO {

  @NotEmpty
  private String id;
  private String name;
  private String image;

  @Min(value = 1)
  private Integer fundingGoal;
  private Long deadline;
  private boolean closeOnGoalReached;

  @Min(value = 1)
  private Integer categoryId;

  @Length(min = 50)
  private String description;
}
