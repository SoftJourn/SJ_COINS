package com.softjourn.coin.server.service;


import com.softjourn.coin.server.eris.contract.Contract;
import com.softjourn.coin.server.eris.contract.response.Response;
import com.softjourn.coin.server.eris.contract.response.ReturnValue;
import com.softjourn.coin.server.eris.contract.response.TxParams;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
import java.security.Principal;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoinServiceTransactionsTestContextConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Rollback
@Transactional
public class CoinServiceTransactionsTest {

    Principal principal;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    ErisContractService contractService;

    Contract contract;

    AccountsService accountsService;

    CoinService coinService;

    @Before
    public void setUp() throws Exception {

        principal = Mockito.mock(Principal.class);

        when(principal.getName()).thenReturn("account1");

        accountsService = mock(AccountsService.class);

        ReflectionTestUtils.setField(accountsService, "accountRepository", accountRepository);

        when(accountsService.getAccount(anyString())).thenCallRealMethod();
        contract = mock(Contract.class);
        contractService = mock(ErisContractService.class);

        coinService = new CoinService(accountsService, contractService);

        when(contractService.getForAccount(any())).thenReturn(contract);

        Response<Object> getResp = new Response<>("",
                new ReturnValue<>(Object.class, new BigDecimal(100)),
                null,
                null);

        Response<Object> sendResp = new Response<>("",
                null,
                null,
                new TxParams("address", "txId"));

        when(contract.call("queryBalance"))
                .thenReturn(getResp);

        when(contract.call(eq("send"), org.mockito.Matchers.anyVararg()))
                .thenReturn(sendResp);

        when(contract.call(eq("mint"), org.mockito.Matchers.anyVararg()))
                .thenReturn(sendResp);
    }

     @Test
    public void testSpent() throws Exception {
        BigDecimal spentAmount = new BigDecimal("10");

        coinService.spent("address", "account1", spentAmount, "Buying Pepsi.");

        verify(contract, times(1)).call(eq("queryBalance"));
        verify(contract, times(1)).call(eq("send"), anyVararg());
    }

    @Test
    public void testMove() throws Exception {
        try {
            coinService.move(principal.getName(), "account2", new BigDecimal(50), "");
        } catch (PersistenceException ignored) {}

        verify(contract, times(1)).call(eq("queryBalance"));
        verify(contract, times(1)).call(eq("send"), anyVararg());
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