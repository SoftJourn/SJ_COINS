package com.softjourn.coin.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.ErisAccountType;
import com.softjourn.coin.server.exceptions.ErisRootAccountOverFlow;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.eris.ErisAccountData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
public class ErisAccountsService {

    private AccountRepository accountRepository;

    private ErisAccountRepository repository;

    private ResourceLoader resourceLoader;

    private com.softjourn.eris.accounts.AccountsService accountsService;

    @Value("${eris.accounts.json.path}")
    private String accountsJsonPath;


    private static final String CHAIN_PARTICIPANT = ".*_participant_.*";
    private static final String CHAIN_ROOT = ".*_root_.*";
    private static final String CHAIN_FULL = ".*_full_.*";
    private static final String CHAIN_DEVELOPER = ".*_developer_.*";
    private static final String CHAIN_VALIDATOR = ".*_validator_.*";

    @Value(value="#{'${root:}'.split(',')}")
    private List<String> rootUsers;


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

    public List<ErisAccount> getAll() {
        return StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public ErisAccount getByName(String ldapId){
        return accountRepository.findOne(ldapId).getErisAccount();
    }

}
