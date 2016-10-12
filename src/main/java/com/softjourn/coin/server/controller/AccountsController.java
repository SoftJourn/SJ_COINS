package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.service.AccountsService;
import com.softjourn.coin.server.service.CoinService;
import com.softjourn.coin.server.util.JsonViews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/account")
public class AccountsController {

    private AccountsService accountsService;
    private CoinService coinService;

    @Autowired
    public AccountsController(AccountsService accountsService, CoinService coinService) {
        this.accountsService = accountsService;
        this.coinService = coinService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @JsonView(JsonViews.REGULAR.class)
    public Account getAccount(Principal principal) {
        Account account = accountsService.getAccount(principal.getName());
        account.setAmount(coinService.getAmount(account.getLdapId()));
        return account;
    }

    @RequestMapping(value = "/{merchantName}", method = RequestMethod.POST)
    @JsonView(JsonViews.ADMIN.class)
    public Account addMerchant(@PathVariable String merchantName) {
        return accountsService.addMerchant(merchantName);
    }

}
