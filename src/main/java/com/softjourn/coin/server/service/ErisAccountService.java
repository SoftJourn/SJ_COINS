package com.softjourn.coin.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.ErisType;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by volodymyr on 8/30/16.
 */
@Service
public class ErisAccountService {


    private AccountsService accountsService;

    private AccountRepository accountRepository;

    private ErisAccountRepository repository;

    private RestTemplate restTemplate;

    private ResourceLoader resourceLoader;

    private static final String CHAIN="mychain";
    private static final String CHAIN_PARTICIPANT=CHAIN.toLowerCase()+"_participant_.*";
    private static final String CHAIN_ROOT=CHAIN.toLowerCase()+"_root_.*";

    @Autowired
    public ErisAccountService(ErisAccountRepository accountRepository, AccountsService accountsService,RestTemplate restTemplate, ResourceLoader resourceLoader) throws IOException {
        this.repository = accountRepository;
        this.restTemplate = restTemplate;
        this.accountsService=accountsService;
        this.resourceLoader=resourceLoader;
        this.restTemplate=restTemplate;
        //init();
    }

    @PostConstruct
    private void init() throws IOException{
        File initFile = resourceLoader.getResource("classpath:accounts.json").getFile();
        ObjectMapper mapper =new ObjectMapper();

        Map<String,ErisAccount> accountMap;
        accountMap = mapper.readValue(initFile, new TypeReference<Map<String,ErisAccount>>() {});
        TreeMap<String,ErisAccount> erisAccountMap=new TreeMap<>();

        accountMap.forEach((k,v)-> {
            if(k.matches(CHAIN_ROOT)) {
                v.setType(ErisType.ROOT);

            }else {
                if (k.matches(CHAIN_PARTICIPANT)) {
                    v.setType(ErisType.PARTICIPANT);
                }
            }
            erisAccountMap.put(v.getAddress(),v);

        });

        LinkedList<ErisAccount> newAssignedErisAccounts=shareAccounts(erisAccountMap);;
        repository.save(newAssignedErisAccounts);
        repository.save(erisAccountMap.values());
    }
    private LinkedList<ErisAccount> shareAccounts(TreeMap<String,ErisAccount> accountCollection){


        LinkedList<Account> linkedAccounts=new LinkedList<>(accountsService.getAll());
        LinkedList<ErisAccount> newAssignedErisAccounts=new LinkedList<>();

        linkedAccounts.stream().filter(account -> account.getErisAccount()!=null).forEach(account -> {
            accountCollection.remove(account.getErisAccount().getAddress());
        });
        linkedAccounts.stream().filter(account -> account.getErisAccount()==null).forEach(account -> {
            ErisAccount newEris=accountCollection.pollFirstEntry().getValue();
            newEris.setAccount(account);
            newAssignedErisAccounts.add(newEris);
        });

        return newAssignedErisAccounts;

    }
    public List<ErisAccount> getAll() {
        return StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }


}
