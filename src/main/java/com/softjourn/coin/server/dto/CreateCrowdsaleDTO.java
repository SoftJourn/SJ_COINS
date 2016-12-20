package com.softjourn.coin.server.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateCrowdsaleDTO {

    private String ifSuccessfulSendTo;

    private BigDecimal fundingGoalInTokens;

    private BigDecimal durationInMinutes;

    private Boolean onGoalReached;

    private List<TokensDTO> addressOfTokensAccumulated;

}
