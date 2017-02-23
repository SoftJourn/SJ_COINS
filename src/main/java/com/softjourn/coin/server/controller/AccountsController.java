package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.dto.AccountDTO;
import com.softjourn.coin.server.dto.MerchantDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.service.AccountsService;
import com.softjourn.coin.server.service.CoinService;
import com.softjourn.coin.server.util.JsonViews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AccountsController {

    private AccountsService accountsService;
    private CoinService coinService;

    @Autowired
    public AccountsController(AccountsService accountsService, CoinService coinService) {
        this.accountsService = accountsService;
        this.coinService = coinService;
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/account", method = RequestMethod.GET)
    @JsonView(JsonViews.REGULAR.class)
    public Account getAccount(Principal principal) {
        Account account = accountsService.getAccount(principal.getName());
        account.setAmount(coinService.getAmount(account.getLdapId()));
        return account;
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/eris/account", method = RequestMethod.GET)
    public AccountDTO getErisAccount(Principal principal) {
        return new AccountDTO(principal.getName(), accountsService.getAccount(principal.getName()).getErisAccount().getAddress());
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/accounts/all", method = RequestMethod.GET)
    public List<AccountDTO> getAccounts() {
        return accountsService.getAll().stream()
                .filter(account -> account.getAccountType() == AccountType.REGULAR)
                .map(account ->
                        new AccountDTO(account.getLdapId(), account.getErisAccount().getAddress()))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','INVENTORY')")
    @RequestMapping(value = "/account/merchant", method = RequestMethod.POST)
    @JsonView(JsonViews.ADMIN.class)
    public Account addMerchant(@RequestBody MerchantDTO merchantDTO) {
        return accountsService.addMerchant(merchantDTO, AccountType.MERCHANT);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','INVENTORY')")
    @RequestMapping(value = "/account/crowdsale", method = RequestMethod.POST)
    @JsonView(JsonViews.ADMIN.class)
    public Account addCrowdSaleAccount(@RequestBody MerchantDTO merchantDTO) {
        return accountsService.addMerchant(merchantDTO, AccountType.CROWDSALE);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','INVENTORY')")
    @RequestMapping(value = "/account/{ldapId}", method = RequestMethod.DELETE)
    @JsonView(JsonViews.ADMIN.class)
    public Map<String, Boolean> deleteAccount(@PathVariable String ldapId) {
        return Collections.singletonMap("deleted", accountsService.delete(ldapId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    @JsonView(JsonViews.COINS_MANAGER.class)
    public List<Account> getAllAccounts() {
        return accountsService.getAll().stream()
                .peek(account -> account.setAmount(coinService.getAmount(account.getLdapId())))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/accounts/{accountType}", method = RequestMethod.GET)
    @JsonView(JsonViews.COINS_MANAGER.class)
    public List<Account> getAccountsByType(@PathVariable String accountType) {
        return accountsService.getAll(AccountType.valueOf(accountType.toUpperCase())).stream()
                .peek(account -> account.setAmount(coinService.getAmount(account.getLdapId())))
                .collect(Collectors.toList());
    }
}
