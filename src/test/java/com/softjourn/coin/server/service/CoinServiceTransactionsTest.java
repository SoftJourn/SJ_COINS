package com.softjourn.coin.server.service;


import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.response.Response;
import com.softjourn.eris.contract.response.TxParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Principal;
import java.util.Collections;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyVararg;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = com.softjourn.coin.server.config.CoinServiceTransactionsTestContextConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Rollback
@Transactional
public class CoinServiceTransactionsTest {

    Principal principal;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    ErisContractService contractService;

    ErisAccountRepository erisAccountRepository;

    Contract contract;

    AccountsService accountsService;

    CoinService coinService;

    @Before
    public void setUp() throws Exception {

        principal = Mockito.mock(Principal.class);

        when(principal.getName()).thenReturn("account1");

        accountsService = mock(AccountsService.class);

        erisAccountRepository = mock(ErisAccountRepository.class);

        ReflectionTestUtils.setField(accountsService, "accountRepository", accountRepository);

        when(accountsService.getAccount(anyString())).thenCallRealMethod();
        when(accountsService.createAccount("account23")).thenThrow(AccountNotFoundException.class);

        contract = mock(Contract.class);
        contractService = mock(ErisContractService.class);

        coinService = new CoinService(accountsService, contractService, erisAccountRepository);

        when(contractService.getTokenContractForAccount(any())).thenReturn(contract);

        Response getResp = new Response("",
                Collections.singletonList(BigInteger.valueOf(100)),
                null,
                null);

        Response sendResp = new Response("",
                Collections.singletonList(true),
                null,
                new TxParams("address", "txId"));

        when(contract.call(eq("balanceOf"), anyVararg()))
                .thenReturn(getResp);

        when(contract.call(eq("transfer"), org.mockito.Matchers.anyVararg()))
                .thenReturn(sendResp);

        when(contract.call(eq("mint"), org.mockito.Matchers.anyVararg()))
                .thenReturn(sendResp);
    }

     @Test
    public void testSpent() throws Exception {
        BigDecimal spentAmount = new BigDecimal("10");

        coinService.buy("VM1", "account1", spentAmount, "Buying Pepsi.");

        verify(contract, times(1)).call(eq("balanceOf"), anyVararg());
        verify(contract, times(1)).call(eq("transfer"), anyVararg());
    }

    @Test
    public void testMove() throws Exception {
        try {
            coinService.move(principal.getName(), "account2", new BigDecimal(50), "");
        } catch (PersistenceException ignored) {}

        verify(contract, times(1)).call(eq("balanceOf"), anyVararg());
        verify(contract, times(1)).call(eq("transfer"), anyVararg());
    }

    @Test(expected = AccountNotFoundException.class)
    public void testMoveWrongToName() throws Exception {
        try {
            coinService.move(principal.getName(), "account23", new BigDecimal(50), "");
        } catch (PersistenceException ignored) {}

        assertEquals(new BigDecimal(100), accountRepository.findOne("account1").getAmount());
        assertEquals(new BigDecimal(200), accountRepository.findOne("account2").getAmount());
    }
}