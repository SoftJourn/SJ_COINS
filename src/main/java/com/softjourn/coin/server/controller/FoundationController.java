package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.FoundationProjectDTO;
import com.softjourn.coin.server.service.DonationsService;
import com.softjourn.coin.server.service.FoundationService;
import java.security.Principal;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/foundations")
public class FoundationController {

  private final DonationsService donationsService;
  private final FoundationService foundationService;

  @PostMapping("/create")
  @PreAuthorize("authenticated")
  public FoundationProjectDTO create(
      @RequestBody @Valid FoundationProjectDTO project,
      Principal principal
  ) {
    return foundationService.create(principal.getName(), project);
  }
}
