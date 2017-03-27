package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.CrowdsaleInfoDTO;
import com.softjourn.coin.server.dto.CrowdsaleTransactionResultDTO;
import com.softjourn.coin.server.dto.DonateDTO;
import com.softjourn.coin.server.service.CrowdsaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/v1/crowdsale/")
public class CrowdsaleController {

    private CrowdsaleService crowdsaleService;

    @Autowired
    public CrowdsaleController(CrowdsaleService crowdsaleService) {
        this.crowdsaleService = crowdsaleService;
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/donate", method = RequestMethod.POST)
    public CrowdsaleTransactionResultDTO donate(@RequestBody DonateDTO dto, Principal principal) throws IOException {
        return (CrowdsaleTransactionResultDTO) crowdsaleService.donate(dto, principal).getValue();
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/withdraw/{address}", method = RequestMethod.POST)
    public CrowdsaleTransactionResultDTO withdraw(@PathVariable String address) throws IOException {
        return (CrowdsaleTransactionResultDTO) crowdsaleService.withdraw(address).getValue();
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/{address}", method = RequestMethod.GET)
    public CrowdsaleInfoDTO getInfo(@PathVariable String address) throws IOException {
        return crowdsaleService.getInfo(address);
    }

}
