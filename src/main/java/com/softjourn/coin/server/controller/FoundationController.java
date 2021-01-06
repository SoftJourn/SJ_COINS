package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.FoundationProjectDTO;
import com.softjourn.coin.server.service.FoundationService;
import java.security.Principal;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/foundations")
@EnableScheduling
public class FoundationController {

  private final FoundationService foundationService;

  @PostMapping("/create")
//  @PreAuthorize("authenticated")
  public String create(
      @RequestBody @Valid FoundationProjectDTO project, Principal principal
  ) {
    return foundationService.create(principal.getName(), project);
  }

  @GetMapping("projects/byUser")
//  @PreAuthorize("authenticated")
  public List<String> getAll(@RequestParam Integer userId, Principal principal) {
    principal = new BasicUserPrincipal("vzaichuk@softjourn.com");
    return foundationService.getAll(principal.getName());
  }

  @GetMapping("/{name}")
//  @PreAuthorize("authenticated")
  public FoundationProjectDTO getOneByName(@PathVariable("name") String name, Principal principal) {
    return foundationService.getOneByName(principal.getName(), name);
  }

  @PutMapping("/{name}")
//  @PreAuthorize("authenticated")
  public Integer close(@PathVariable("name") String name, Principal principal) {
    return foundationService.close(principal.getName(), name);
  }

//  @Scheduled(fixedDelay = 300000, initialDelay = 10000)
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
    System.out.println("TXID: " + create(project, principal));
//    System.out.println(getAll(principal));
//    System.out.println(getOneByName(projectName, principal));
//    System.out.println(close(projectName, principal));
  }
}
