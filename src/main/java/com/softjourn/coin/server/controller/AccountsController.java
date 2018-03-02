package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.dto.AccountDTO;
import com.softjourn.coin.server.dto.MerchantDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.service.AccountsService;
import com.softjourn.coin.server.service.CoinService;
import com.softjourn.coin.server.util.JsonViews;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
public class AccountsController {

    private AccountsService accountsService;
    private CoinService coinService;

    @Autowired
    public AccountsController(AccountsService accountsService, CoinService coinService) {
        this.accountsService = accountsService;
        this.coinService = coinService;
    }

    // GET

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/account", method = RequestMethod.GET)
    @JsonView(JsonViews.REGULAR.class)
    public Account getAccount(Principal principal) {
        Account account = accountsService.getAccount(principal.getName());
        account.setAmount(coinService.getAmount(account.getEmail()));
        return account;
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/account/{accountName}/{imageName:.+\\..+}", method = RequestMethod.GET)
    @JsonView(JsonViews.REGULAR.class)
    public byte[] getImage(@PathVariable String accountName, @PathVariable String imageName) {
        String uri = String.format("/account/%s/%s", accountName, imageName);
        return accountsService.getImage(uri);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/account/default", method = RequestMethod.GET)
    public byte[] getDefaultImage() {
        return accountsService.getDefaultImage();
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/accounts/all", method = RequestMethod.GET)
    public List<AccountDTO> getAccounts() {
        return accountsService.getAll().stream()
                .filter(account -> account.getAccountType() == AccountType.REGULAR)
                .map(account ->
                        new AccountDTO(account.getLdapId(), account.getEmail()))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    @JsonView(JsonViews.COINS_MANAGER.class)
    public List<Account> getAllAccounts() throws IOException {
        return accountsService.getAmounts(accountsService.getAll());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/accounts/pages", method = RequestMethod.GET)
    @JsonView(JsonViews.COINS_MANAGER.class)
    public Page<Account> getAccountsByPage(Pageable pageable) {
        return accountsService.getByPage(pageable);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/accounts/search", method = RequestMethod.GET)
    @JsonView(JsonViews.COINS_MANAGER.class)
    public Page<Account> findAccounts(@NotEmpty @NotBlank @RequestParam("value") String value, Pageable pageable) {
        return accountsService.search(value, pageable);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/accounts/{accountType}", method = RequestMethod.GET)
    @JsonView(JsonViews.COINS_MANAGER.class)
    public List<Account> getAccountsByType(@PathVariable String accountType) throws IOException {
        return accountsService.getAmounts(accountsService.getAll(AccountType.valueOf(accountType.toUpperCase())));
    }

    // POST

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','INVENTORY')")
    @RequestMapping(value = "/account/merchant", method = RequestMethod.POST)
    @JsonView(JsonViews.ADMIN.class)
    public Account addMerchant(@RequestBody MerchantDTO merchantDTO) {
        return accountsService.addMerchant(merchantDTO, AccountType.MERCHANT);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/account/image", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void loadAccountImage(@NotNull @RequestParam MultipartFile file, Principal user) {
        accountsService.loadAccountImage(file, user.getName());
    }

    // DELETE

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/account/{ldapId}", method = RequestMethod.DELETE)
    @JsonView(JsonViews.ADMIN.class)
    public Map<String, Boolean> deleteAccount(@PathVariable String ldapId) {
        return Collections.singletonMap("deleted", accountsService.delete(ldapId));
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    public void reset() {
        accountsService.reset();
    }
}
