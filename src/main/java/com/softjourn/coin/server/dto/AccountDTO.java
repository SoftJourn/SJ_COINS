package com.softjourn.coin.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonPropertyOrder(value = {"account", "fullName", "coins"})
public class AccountDTO {

    @JsonProperty(value = "Account")
    private String account;

    @JsonProperty(value = "Full Name")
    private String fullName;

    @JsonProperty(value = "Coins")
    private BigDecimal coins;

}
