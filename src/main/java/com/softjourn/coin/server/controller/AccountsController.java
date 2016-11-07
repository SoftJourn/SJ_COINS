package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.service.AccountsService;
import com.softjourn.coin.server.service.CoinService;
import com.softjourn.coin.server.util.JsonViews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
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

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ROLE_INVENTORY')")
    @RequestMapping(value = "/account/{merchantName}", method = RequestMethod.POST)
    @JsonView(JsonViews.ADMIN.class)
    public Account addMerchant(@PathVariable String merchantName) {
        return accountsService.addMerchant(merchantName);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ROLE_BILLING')")
    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    @JsonView(JsonViews.COINS_MANAGER.class)
    public List<Account> getAllAccounts() {
        return accountsService.getAll().stream()
                .peek(account -> account.setAmount(coinService.getAmount(account.getLdapId())))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ROLE_BILLING')")
    @RequestMapping(value = "/accounts/{accountType}", method = RequestMethod.GET)
    @JsonView(JsonViews.COINS_MANAGER.class)
    public List<Account> getAccountsByType(@PathVariable String accountType) {
        return accountsService.getAll(AccountType.valueOf(accountType.toUpperCase())).stream()
                .peek(account -> account.setAmount(coinService.getAmount(account.getLdapId())))
                .collect(Collectors.toList());
    }
}
