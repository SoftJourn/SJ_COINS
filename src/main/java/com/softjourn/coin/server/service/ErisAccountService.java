package com.softjourn.coin.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by volodymyr on 8/30/16.
 */
@Service
public class ErisAccountService {


    private AccountsService accountsService;

    private ErisAccountRepository repository;

    private RestTemplate restTemplate;

    private ResourceLoader resourceLoader;

    private final String CHAIN_PARTICIPANT="mychain_participant_.*";
    private final String CHAIN_ROOT="mychain_root_.*";

    @Autowired
    public ErisAccountService(ErisAccountRepository accountRepository, AccountsService accountsService,RestTemplate restTemplate, ResourceLoader resourceLoader) throws IOException {
        this.repository = accountRepository;
        this.restTemplate = restTemplate;
        this.accountsService=accountsService;

        File initFile = resourceLoader.getResource("classpath:accounts.json").getFile();
        ObjectMapper mapper =new ObjectMapper();

        Map<String,ErisAccount> accountMap= null;
        accountMap = mapper.readValue(initFile, new TypeReference<Map<String,ErisAccount>>() {});


        accountMap.forEach((k,v)-> {
            if(k.matches(CHAIN_ROOT))
                v.setType("ROOT");
            if(k.matches(CHAIN_PARTICIPANT))
                v.setType("PARTICIPANT");

            v.setAccount(accountsService.getAccount("vromanchuk"));
        });

        repository.save(accountMap.values());

    }

}
