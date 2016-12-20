package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrowdsaleDTO extends CreateCrowdsaleDTO {

    private String creator;

    private BigDecimal amountRaised;

}
