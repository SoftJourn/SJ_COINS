package com.softjourn.coin.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.entity.ErisAccount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

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


    @Autowired
    AccountsService accountsService;

    @Autowired
    ErisAccountsService erisAccountsService;

//    @Before
//    public void init(){
//        List<Account> accounts=new ArrayList<>();
//        accounts.add(new Account("vromanchuk",new BigDecimal("200")));
//        accounts.add(new Account("vdanyliuk",new BigDecimal("300")));
//        accounts.add(new Account("ovovchuk",new BigDecimal("400")));
//        when(accountsService.getAll()).thenReturn(accounts);
//    }

    @Test
    public void testErisAccountsJsonFile() throws IOException{

        File initFile=new File(JSON_PATH);
        ObjectMapper mapper =new ObjectMapper();

        Map<String,ErisAccount> accountMap=mapper.readValue(initFile, new TypeReference<Map<String,ErisAccount>>() {});

        assertEquals(accountMap.get(TEST_PARTICIPANT).toString(),accountMap.get(TEST_PARTICIPANT).toString().isEmpty(),false);
        assertEquals(accountMap.get(TEST_ROOT).toString(),accountMap.get(TEST_ROOT).toString().isEmpty(),false);
        assertEquals("Size > 0",accountMap.size()>0,true);

    }
    @Test
    public void testErisAccountFill(){
        accountsService.getAll().forEach(account -> {
            assertNotEquals("Eris account object is null for "+account.getLdapId(),account.getErisAccount(),null);
            System.out.println(account.getErisAccount().toString());
        });
    }
    @Test
    public void testValidity() throws IOException{
        File erisJsonFile=new File(JSON_PATH);
        TreeMap<String,ErisAccount> map=erisAccountsService.erisAccountMapping(erisJsonFile);

        accountsService.getAll().forEach(account -> {
            assertNotEquals("Eris account object",account.getErisAccount(),null);
            assertEquals("JSON account differs from existing in DB",map.get(account.getErisAccount().getAddress()),account.getErisAccount());
        });
    }


}
