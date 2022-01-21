package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.dto.AccountDTO;
import com.softjourn.coin.server.dto.MerchantDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.service.AccountsService;
import com.softjourn.coin.server.service.CoinService;
import com.softjourn.coin.server.util.JsonViews;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class AccountsController {

  private final AccountsService accountsService;
  private final CoinService coinService;

  // GET

  @GetMapping("/account")
  @PreAuthorize("authenticated")
  @JsonView(JsonViews.REGULAR.class)
  public Account getAccount(Principal principal) {
    Account account = accountsService.getAccount(principal.getName());
    account.setAmount(coinService.getAmount(account.getEmail()));
    return account;
  }

  @GetMapping("/account/{accountName}/{imageName:.+\\..+}")
  @PreAuthorize("authenticated")
  @JsonView(JsonViews.REGULAR.class)
  public byte[] getImage(@PathVariable String accountName, @PathVariable String imageName) {
    String uri = String.format("/account/%s/%s", accountName, imageName);
    return accountsService.getImage(uri);
  }

  @GetMapping("/account/default")
  @PreAuthorize("permitAll")
  public byte[] getDefaultImage() {
    return accountsService.getDefaultImage();
  }

  @GetMapping("/accounts/all")
  @PreAuthorize("authenticated")
  public List<AccountDTO> getAccounts() {
    return accountsService.getAll().stream()
        .filter(account -> account.getAccountType() == AccountType.REGULAR)
        .map(account ->
            new AccountDTO(account.getLdapId(), account.getEmail()))
        .collect(Collectors.toList());
  }

  @GetMapping("/accounts")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  @JsonView(JsonViews.COINS_MANAGER.class)
  public List<Account> getAllAccounts() {
    return accountsService.getAmounts(accountsService.getAll());
  }

  @GetMapping("/accounts/{accountType}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  @JsonView(JsonViews.COINS_MANAGER.class)
  public List<Account> getAccountsByType(@PathVariable String accountType) {
    return accountsService
        .getAmounts(accountsService.getAll(AccountType.valueOf(accountType.toUpperCase())));
  }

  // POST

  @PostMapping("/account/merchant")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','INVENTORY')")
  @JsonView(JsonViews.ADMIN.class)
  public Account addMerchant(@RequestBody MerchantDTO merchantDTO) {
    return accountsService.addMerchant(merchantDTO, AccountType.MERCHANT);
  }

  @PostMapping(value = "/account/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("authenticated")
  public void loadAccountImage(@NotNull @RequestParam MultipartFile file, Principal user) {
    accountsService.loadAccountImage(file, user.getName());
  }

  // DELETE

  @DeleteMapping("/account/{ldapId}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','INVENTORY')")
  @JsonView(JsonViews.ADMIN.class)
  public Map<String, Boolean> deleteAccount(@PathVariable String ldapId) {
    return Collections.singletonMap("deleted", accountsService.delete(ldapId));
  }

  @DeleteMapping("/reset")
  @PreAuthorize("authenticated")
  public void reset() {
    accountsService.reset();
  }
}
