package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.FoundationProjectDTO;
import com.softjourn.coin.server.service.DonationsService;
import com.softjourn.coin.server.service.FoundationService;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.BasicUserPrincipal;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/foundations")
@EnableScheduling
public class FoundationController {

  private final FoundationService foundationService;

  @PostMapping("/create")
  @PreAuthorize("authenticated")
  public String create(
      @RequestBody @Valid FoundationProjectDTO project, Principal principal
  ) {
    return foundationService.create(principal.getName(), project);
  }

  @GetMapping
  @PreAuthorize("authenticated")
  public List<String> getAll(Principal principal) {
    return foundationService.getAll(principal.getName());
  }

  @GetMapping("/{name}")
  @PreAuthorize("authenticated")
  public FoundationProjectDTO getOneByName(@PathVariable("name") String name, Principal principal) {
    return foundationService.getOneByName(principal.getName(), name);
  }

  @PutMapping("/{name}")
  @PreAuthorize("authenticated")
  public Integer close(@PathVariable("name") String name, Principal principal) {
    return foundationService.close(principal.getName(), name);
  }

  @Scheduled(fixedDelay = 300000, initialDelay = 10000)
  public void scheduleFixedRateWithInitialDelayTask() {
    final String projectName = "TestProject";
    final String accountName = "vzaichuk@softjourn.com";
    FoundationProjectDTO project = new FoundationProjectDTO();
    project.setName(projectName);
    project.setCreatorId("vzaichuk@softjourn.com");
    project.setAdminId("vzaichuk@softjourn.com");
    project.setFundingGoal(1000);
    project.setCloseOnGoalReached(true);

    project.setDeadline(60);
    project.setWithdrawAllowed(true);
    project.setMainCurrency("coin");

    Map<String, Boolean> currencyMap = new HashMap<>();
    currencyMap.put("coin", true);
    project.setAcceptCurrencies(currencyMap);

    Principal principal = new BasicUserPrincipal(accountName);
    String txid = create(project, principal);
    System.out.println("TXID: " + txid);
    System.out.println(getAll(principal));
    System.out.println(getOneByName(projectName, principal));
    System.out.println(close(projectName, principal));
  }
}
