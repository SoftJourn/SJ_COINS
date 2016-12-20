package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.CreateCrowdsaleDTO;
import com.softjourn.coin.server.dto.CreateCrowdsaleResponseDTO;
import com.softjourn.coin.server.dto.CrowdsaleDTO;
import com.softjourn.coin.server.dto.CrowdsaleTransactionResultDTO;
import com.softjourn.coin.server.dto.DonateDTO;
import com.softjourn.coin.server.dto.TokensDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/crowdsale/")
public class CrowdsaleController {

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public CreateCrowdsaleResponseDTO createCrowdsale(@RequestBody CreateCrowdsaleDTO dto) {
        return new CreateCrowdsaleResponseDTO("0000000000000000000000000000");
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/donate", method = RequestMethod.POST)
    public CrowdsaleTransactionResultDTO donate(@RequestBody DonateDTO dto) {
        return new CrowdsaleTransactionResultDTO(true);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/withdraw/{address}", method = RequestMethod.POST)
    public CrowdsaleTransactionResultDTO withdraw(@PathVariable String address) {
        return new CrowdsaleTransactionResultDTO(true);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/{address}", method = RequestMethod.GET)
    public CrowdsaleDTO getInfo(@PathVariable String address) {
        CrowdsaleDTO dto = new CrowdsaleDTO();
        dto.setIfSuccessfulSendTo("333333333333333333333333");
        dto.setCreator("111111111111111111111");
        dto.setFundingGoalInTokens(BigDecimal.valueOf(1000));
        dto.setAmountRaised(BigDecimal.valueOf(1000));
        dto.setDurationInMinutes(BigDecimal.valueOf(1000));
        dto.setOnGoalReached(true);
        dto.setAddressOfTokensAccumulated(new ArrayList<TokensDTO>() {{
            add(new TokensDTO("4444444444444444444", BigDecimal.valueOf(1000)));
        }});
        return dto;
    }

}
