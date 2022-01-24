package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.AllowanceRequestDTO;
import com.softjourn.coin.server.dto.CreateFoundationProjectDTO;
import com.softjourn.coin.server.dto.FoundationDonationDTO;
import com.softjourn.coin.server.dto.FoundationViewDTO;
import com.softjourn.coin.server.dto.UpdateFoundationDTO;
import com.softjourn.coin.server.dto.WithdrawRequestDTO;
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
import org.springframework.scheduling.annotation.EnableScheduling;
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
  @PreAuthorize("authenticated")
  public String create(
      @RequestBody @Valid CreateFoundationProjectDTO project, Principal principal
  ) {
    return foundationService.create(principal.getName(), project);
  }

  @PutMapping("/projects")
  @PreAuthorize("authenticated")
  public String update(@RequestBody @Valid UpdateFoundationDTO updateDTO, Principal principal) {
    return foundationService.update(principal.getName(), updateDTO);
  }

  @GetMapping("/projects")
  public List<FoundationViewDTO> getAll() {
    return foundationService.getAll();
  }

  @GetMapping("/projects/byUser")
  @PreAuthorize("authenticated")
  public List<FoundationViewDTO> getAllByUser(@RequestParam String userId, Principal principal) {
    return foundationService.getAllByUser(principal.getName());
  }

  @GetMapping("/projects/my")
  @PreAuthorize("authenticated")
  public List<FoundationViewDTO> getMy(Principal principal) {
    return foundationService.getMy(principal.getName());
  }

  @GetMapping("/projects/{name}")
  @PreAuthorize("authenticated")
  public FoundationViewDTO getOneByName(@PathVariable("name") String name, Principal principal) {
    return foundationService.getOneByName(principal.getName(), name);
  }

  @PostMapping("/wallet/donation")
  @PreAuthorize("authenticated")
  public String donate(@RequestBody FoundationDonationDTO donation, Principal principal) {
    return foundationService.donate(principal.getName(), donation);
  }

  @PostMapping("/projects/withdraw")
  @PreAuthorize("authenticated")
  public String withdraw(@RequestBody WithdrawRequestDTO request, Principal principal) {
    return foundationService.withdraw(principal.getName(), request);
  }

  @PostMapping("/projects/setAllowance")
  @PreAuthorize("authenticated")
  public String setAllowance(@RequestBody AllowanceRequestDTO request, Principal principal) {
    return foundationService.setAllowance(principal.getName(), request);
  }

  @PostMapping("/project/{name}/close")
  @PreAuthorize("authenticated")
  public Integer close(@PathVariable("name") String name, Principal principal) {
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

  @GetMapping("/images/{image:.+\\..+}")
  public byte[] getImage(@PathVariable String image) {
    return foundationService.getImage(image);
  }
}
