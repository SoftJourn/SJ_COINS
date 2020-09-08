package com.softjourn.coin.server.controller;


import com.softjourn.coin.server.dto.AmountDTO;
import com.softjourn.coin.server.dto.BatchTransferDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.DonationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/donations")
public class DonationsController {

    private final DonationsService donationsService;

    @Autowired
    public DonationsController(DonationsService donationsService) {
        this.donationsService = donationsService;
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/{projectId}/amount", method = RequestMethod.GET)
    public Map<String, BigDecimal> getAmount(@PathVariable("projectId") String projectId) {
        Map<String, BigDecimal> responseBody = new HashMap<>();
        responseBody.put("amount", donationsService.getAmount(projectId));
        return responseBody;
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/{projectId}/donate", method = RequestMethod.POST)
    public Transaction donateToProject(Principal principal,
                                   @RequestBody AmountDTO amountDto,
                                   @PathVariable("projectId") String projectId) {
        return donationsService.donateToProject(principal.getName(), projectId, amountDto.getAmount(), amountDto.getComment());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/{projectId}/refund", method = RequestMethod.POST)
    public Transaction closeProject(@PathVariable("projectId") String projectId,
                                    @RequestBody List<BatchTransferDTO> transfers) {
        return donationsService.refundProject(projectId, transfers);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/{projectId}/close", method = RequestMethod.POST)
    public Transaction rollbackProject(@PathVariable("projectId") String projectId) {
        return donationsService.closeProject(projectId);
    }
}
