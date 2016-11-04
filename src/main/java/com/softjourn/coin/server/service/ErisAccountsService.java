package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.ErisAccountType;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.eris.ErisAccountData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class ErisAccountsService {

    private AccountRepository accountRepository;

    private ErisAccountRepository repository;

    private ResourceLoader resourceLoader;

    private com.softjourn.eris.accounts.AccountsService accountsService;


    @Autowired
    public ErisAccountsService(ErisAccountRepository repository,
                               ResourceLoader resourceLoader,
                               AccountRepository accountRepository,
                               com.softjourn.eris.accounts.AccountsService accountsService) throws IOException {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.resourceLoader = resourceLoader;
        this.accountsService = accountsService;
    }

    public ErisAccount bindFreeAccount() {
        return repository
                .getFree()
                .findFirst()
                .orElse(createNew());
    }

    private ErisAccount createNew() {
        ErisAccountData accountData = accountsService.createAccount();

        ErisAccount newAccount = new ErisAccount();
        newAccount.setAddress(accountData.getAddress());
        newAccount.setPubKey(accountData.getPubKey());
        newAccount.setPrivKey(accountData.getPrivKey());
        newAccount.setType(ErisAccountType.PARTICIPANT);

        repository.save(newAccount);

        return newAccount;
    }

    public ErisAccount getByName(String ldapId){
        return accountRepository.findOne(ldapId).getErisAccount();
    }

}
