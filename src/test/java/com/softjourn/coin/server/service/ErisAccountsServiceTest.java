package com.softjourn.coin.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by volodymyr on 8/30/16.
 */


@RunWith(MockitoJUnitRunner.class)
public class ErisAccountsServiceTest {

    private final String JSON_PATH="./src/main/resources/accounts.json";
    private final String CHAIN_PARTICIPANT="mychain_participant_";
    private final String CHAIN_ROOT="mychain_participant_";

    @Mock
    ErisAccountRepository repository;


    @Test
    public void checkErisAccountsFile() throws IOException{



        File initFile=new File(JSON_PATH);
        ObjectMapper mapper =new ObjectMapper();

        Map<String,ErisAccount> accountMap=mapper.readValue(initFile, new TypeReference<Map<String,ErisAccount>>() {});

        System.out.println(String.format("%03d",100));

        System.out.println(accountMap.get("mychain_participant_001").getAddress());
        System.out.println(accountMap.size());

        repository.save(accountMap.values());

        System.out.println("mychain_participant_001".matches("mychain_participant_.*"));

    }

}
