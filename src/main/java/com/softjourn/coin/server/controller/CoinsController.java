package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.AmountDTO;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.AccountsService;
import com.softjourn.coin.server.service.CoinService;
import com.softjourn.coin.server.service.FillAccountsService;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class CoinsController {

  private final CoinService coinService;
  private final FillAccountsService fillAccountsService;
  private final AccountsService accountsService;

  @GetMapping("/amount")
  @PreAuthorize("authenticated")
  public Map<String, BigDecimal> getAmount(Principal principal) {
    Map<String, BigDecimal> responseBody = new HashMap<>();
    responseBody.put(
        "amount",
        coinService.getAmount(accountsService.getAccount(principal.getName()).getEmail()));
    return responseBody;
  }

  @PostMapping("/buy/{merchantLdapId}")
  @PreAuthorize("authenticated")
  public Transaction spentAmount(
      Principal principal, @RequestBody AmountDTO amountDto, @PathVariable String merchantLdapId) {
    return coinService.buy(principal.getName(), merchantLdapId, amountDto.getAmount());
  }

  @PostMapping("/move/{accountLdapId}")
  @PreAuthorize("authenticated")
  public Transaction moveAmount(
      Principal principal, @RequestBody AmountDTO amountDTO, @PathVariable String accountLdapId) {
    return coinService.move(principal.getName(), accountLdapId, amountDTO.getAmount());
  }

  @PostMapping("/rollback/{txId}")
  @PreAuthorize("#oauth2.hasScope('rollback')")
  public Transaction rollback(@PathVariable Long txId) {
    return coinService.rollback(txId);
  }

  @PostMapping("/move/{account}/treasury")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  public Transaction moveAmountToTreasury(@PathVariable String account,
      @RequestBody AmountDTO amountDTO
  ) {
    return coinService.moveToTreasury(account, amountDTO.getAmount(), amountDTO.getComment());
  }

  @PostMapping("/add/{account}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  public Transaction addAmount(@RequestBody AmountDTO amount,
      @PathVariable String account) {
    return coinService.fillAccount(account, amount.getAmount(), amount.getComment());
  }

  @PostMapping("/add/")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  public void addAmounts(@RequestParam MultipartFile file) {
    fillAccountsService.fillAccounts(file);
  }

  @GetMapping("/template")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  public ResponseEntity<Void> getTemplate(HttpServletResponse response) throws IOException {
    String contentDisposition = "attachment; filename=\"template.csv\"";

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    response.setContentType("application/csv");
    response.setCharacterEncoding("UTF-8");

    fillAccountsService.getAccountDTOTemplate(response.getWriter());

    return new ResponseEntity<>(headers, HttpStatus.OK);
  }

  @PostMapping("/distribute")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  public void distribute(@RequestBody AmountDTO amount) {
    coinService.distribute(amount.getAmount(), "Distribute money for all accounts.");
  }

  @GetMapping("/amount/treasury")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  public Map<String, BigDecimal> getTreasuryAmount() {
    HashMap<String, BigDecimal> responseBody = new HashMap<>();
    responseBody.put("amount", coinService.getTreasuryAmount());
    return responseBody;
  }

  @GetMapping("/amount/{accountType}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
  public Map<String, BigDecimal> getAmountByAccountType(@PathVariable String accountType) {
    HashMap<String, BigDecimal> responseBody = new HashMap<>();
    responseBody.put(
        "amount",
        coinService.getAmountByAccountType(AccountType.valueOf(accountType.toUpperCase())));

    return responseBody;
  }
}
