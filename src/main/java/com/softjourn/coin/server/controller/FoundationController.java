package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.FoundationProjectDTO;
import com.softjourn.coin.server.dto.FoundationViewDTO;
import com.softjourn.coin.server.service.FoundationService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
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

  @PostMapping("/projects")
//  @PreAuthorize("authenticated")
  public String create(@RequestBody @Valid FoundationProjectDTO project, Principal principal) {
    principal = new BasicUserPrincipal("vzaichuk@softjourn.com");
    return foundationService.create(principal.getName(), project);
  }

  @GetMapping("/projects/byUser")
//  @PreAuthorize("authenticated")
  public List<FoundationViewDTO> getAll(@RequestParam Integer userId, Principal principal) {
    principal = new BasicUserPrincipal("vzaichuk@softjourn.com");
    return foundationService.getAll(principal.getName());
  }

  @GetMapping("/{name}")
//  @PreAuthorize("authenticated")
  public FoundationProjectDTO getOneByName(@PathVariable("name") String name, Principal principal) {
    principal = new BasicUserPrincipal("vzaichuk@softjourn.com");
    return foundationService.getOneByName(principal.getName(), name);
  }

  @PutMapping("/{name}")
//  @PreAuthorize("authenticated")
  public Integer close(@PathVariable("name") String name, Principal principal) {
    principal = new BasicUserPrincipal("vzaichuk@softjourn.com");
    return foundationService.close(principal.getName(), name);
  }

  @GetMapping("/categories")
  public List<Map<String, Object>> getCategories() {
    List<Map<String, Object>> list = new ArrayList<>();
    list.add(new HashMap<String, Object>() {{ put("id", 1); put("name", "Technology"); }});
    list.add(new HashMap<String, Object>() {{ put("id", 2); put("name", "Health"); }});
    list.add(new HashMap<String, Object>() {{ put("id", 3); put("name", "Science"); }});

    return list;
  }

  @GetMapping("/wallet/donations")
  public List<Object> getDonations() {
    return Collections.emptyList();
  }

  @GetMapping("/admin/getUserRoles")
  public List<Object> getUserRoles() {
    return Collections.emptyList();
  }
}
