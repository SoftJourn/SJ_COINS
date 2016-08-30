package com.softjourn.coin.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.ErisAccount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by volodymyr on 8/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ErisAccountsServiceTest {
    @Test
    public void checkErisAccountsFile() throws IOException{

        String CHAIN="mychain_participant_";

        File initFile=new File("./src/main/resources/accounts.json");
        ObjectMapper mapper =new ObjectMapper();

        Map<String,ErisAccount> accountMap=mapper.readValue(initFile, new TypeReference<Map<String,ErisAccount>>() {});


        System.out.println(String.format("%03d",100));
        System.out.println(accountMap.get("mychain_participant_001").getAddress());
        System.out.println(accountMap.size());

    }
}
