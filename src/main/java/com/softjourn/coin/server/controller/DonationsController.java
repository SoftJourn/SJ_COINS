package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.AmountDTO;
import com.softjourn.coin.server.dto.BatchTransferDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.DonationsService;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/donations")
public class DonationsController {

  private final DonationsService donationsService;

  @GetMapping("/{projectId}/amount")
  @PreAuthorize("authenticated")
  public Map<String, BigDecimal> getAmount(@PathVariable("projectId") String projectId) {
    Map<String, BigDecimal> responseBody = new HashMap<>();
    responseBody.put("amount", donationsService.getAmount(projectId));
    return responseBody;
  }

  @PostMapping("/{projectId}/donate")
  @PreAuthorize("authenticated")
  public Transaction donateToProject(Principal principal,
      @RequestBody AmountDTO amountDto,
      @PathVariable("projectId") String projectId
  ) {
    return donationsService.donateToProject(
        principal.getName(),
        projectId,
        amountDto.getAmount(),
        amountDto.getComment());
  }

  @PostMapping("/{projectId}/refund")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  public Transaction refundProject(@PathVariable("projectId") String projectId,
      @RequestBody List<BatchTransferDTO> transfers
  ) {
    return donationsService.refundProject(projectId, transfers);
  }

  @PostMapping("/{projectId}/close")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  public Transaction closeProject(@PathVariable("projectId") String projectId) {
    return donationsService.closeProject(projectId);
  }
}
