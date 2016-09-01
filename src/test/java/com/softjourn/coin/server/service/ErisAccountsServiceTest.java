package com.softjourn.coin.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.config.CoinServerApplication;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

/**
 * Created by volodymyr on 8/30/16.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoinServiceTransactionsTestContextConfiguration.class, loader=AnnotationConfigContextLoader.class)
public class ErisAccountsServiceTest {


    private static final int PARTICIPANT_NUM=1;
    private static final int ROOT_NUM=0;
    private final String JSON_PATH="./src/main/resources/accounts.json";
    private static final String CHAIN="mychain";
    private static final String CHAIN_PARTICIPANT=CHAIN.toLowerCase()+"_participant_";
    private static final String CHAIN_ROOT=CHAIN.toLowerCase()+"_root_";
    private static final String TEST_PARTICIPANT=CHAIN_PARTICIPANT+String.format("%03d",PARTICIPANT_NUM);
    private static final String TEST_ROOT=CHAIN_ROOT+String.format("%03d",ROOT_NUM);


    private static final String NOT_EXISTING_LDAP_ID = "notExist";
    private static final String ID_EXISTING_IN_DB = "existsInDB";
    private static final String EXISTING_LDAP_ID = "ldapId";

//    @Mock
//    ErisAccountRepository erisAccountRepository;

    @Autowired
    AccountsService accountsService;

    @Mock


    private RestTemplate restTemplate;

    @Test
    public void testErisAccountsJsonFile() throws IOException{

        File initFile=new File(JSON_PATH);
        ObjectMapper mapper =new ObjectMapper();


        Map<String,ErisAccount> accountMap=mapper.readValue(initFile, new TypeReference<Map<String,ErisAccount>>() {});

        assertEquals(accountMap.get(TEST_PARTICIPANT).toString(),accountMap.get(TEST_PARTICIPANT).toString().isEmpty(),false);
        assertEquals(accountMap.get(TEST_ROOT).toString(),accountMap.get(TEST_ROOT).toString().isEmpty(),false);
        assertEquals("Size > 0",accountMap.size()>0,true);

        //erisAccountRepository.save(accountMap.values());

    }

    @Test
    public void testErisAccountFill(){
        accountsService.getAll().forEach(account -> {
            assertNotEquals("Eris account object",account.getErisAccount(),null);
            System.out.println(account.getErisAccount().toString());
        });
    }

}
