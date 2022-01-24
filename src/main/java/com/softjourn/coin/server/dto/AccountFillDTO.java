package com.softjourn.coin.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import lombok.Data;

@Data
@JsonPropertyOrder(value = {"account", "fullName", "coins"})
public class AccountFillDTO {

  @JsonProperty(value = "Account")
  private String account;

  @JsonProperty(value = "Full Name")
  private String fullName;

  @JsonProperty(value = "Coins")
  private BigDecimal coins;
}
