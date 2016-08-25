package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.service.AccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/account")
public class AccountsController {

    private AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Account getAccount(Principal principal) {
        return accountsService.getAccount(principal.getName());
    }

}
