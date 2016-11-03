package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.AccountWasDeletedException;
import com.softjourn.coin.server.exceptions.ErisAccountNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountsService {

    private static final String DEFAULT_IMAGE_NAME = "images/default.png";

    private AccountRepository accountRepository;

    @Autowired
    private ErisAccountsService erisAccountsService;

    private ErisAccountRepository erisAccountRepository;

    private RestTemplate restTemplate;

    public AccountsService(AccountRepository accountRepository, ErisAccountsService erisAccountsService, ErisAccountRepository erisAccountRepository, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.erisAccountsService = erisAccountsService;
        this.erisAccountRepository = erisAccountRepository;
        this.restTemplate = restTemplate;
    }

    @Autowired
    public AccountsService(AccountRepository accountRepository, ErisAccountRepository erisAccountRepository, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.erisAccountRepository = erisAccountRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${auth.server.url}")
    private String authServerUrl;

    Account getAccountIfExistInLdapBase(String ldapId) {
        try {
            return restTemplate.getForEntity(authServerUrl + "/users/" + ldapId, Account.class).getBody();
        } catch (RestClientException rce) {
            return null;
        }
    }

    public List<Account> getAll() {
        return accountRepository.findAllUndeleted();
    }

    /**
     * Get all accounts of particular type
     *
     * @param accountType type of account
     * @return list of accounts
     */
    public List<Account> getAll(AccountType accountType) {
        return accountRepository.getAccountsByType(accountType);
    }

    public Account getAccount(String ldapId) {
        return Optional
                .ofNullable(accountRepository.findOneUndeleted(ldapId))
                .orElseGet(() -> createAccount(ldapId));
    }

    public Account add(String ldapId) {
        Account account = accountRepository.findOne(ldapId);
        if (account == null) {
            return createAccount(ldapId);
        } else if (account.isDeleted()) {
            throw new AccountWasDeletedException("Account \"" + ldapId + "\" was deleted. Contact administrators.");
        } else return account;
    }

    Account update(Account account) {
        return accountRepository.save(account);
    }

    @Transactional
    Account createAccount(String ldapId) {
        ErisAccount erisAccount = getNewErisAccount();
        Account account = buildAccount(ldapId, erisAccount);
        return accountRepository.save(account);
    }

    private Account buildAccount(String ldapId, ErisAccount erisAccount) {
        Account account = getAccountIfExistInLdapBase(ldapId);
        if (account != null) {
            account.setAmount(new BigDecimal(0));
            account.setImage(DEFAULT_IMAGE_NAME);
            account.setAccountType(AccountType.REGULAR);
            account.setErisAccount(erisAccount);
            return account;
        } else {
            throw new AccountNotFoundException(ldapId);
        }
    }

    private ErisAccount getNewErisAccount() {
        ErisAccount erisAccount = erisAccountsService.bindFreeAccount();
        if (erisAccount == null) throw new ErisAccountNotFoundException();
        return erisAccount;
    }

    @Transactional
    public Account addMerchant(String name) {
        Account newMerchantAccount = new Account(name, BigDecimal.ZERO);
        newMerchantAccount.setFullName(name);
        newMerchantAccount.setAccountType(AccountType.MERCHANT);
        ErisAccount erisAccount = erisAccountsService.bindFreeAccount();
        if (erisAccount == null) throw new ErisAccountNotFoundException();
        newMerchantAccount = accountRepository.save(newMerchantAccount);
        erisAccount.setAccount(newMerchantAccount);
        erisAccountRepository.save(erisAccount);
        return newMerchantAccount;
    }
}
