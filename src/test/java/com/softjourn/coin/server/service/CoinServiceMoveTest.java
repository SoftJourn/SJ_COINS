package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.response.Response;
import com.softjourn.eris.contract.response.TxParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Principal;
import java.util.Collections;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CoinServiceMoveTest {

    Account account1;
    Account account2;

    @Mock
    Principal principal;

    @Mock
    AccountsService accountsService;

    @Mock
    ErisContractService contractService;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    ErisAccountRepository erisAccountRepository;

    @Mock
    Contract contract;

    CoinService coinService;

    @Before
    public void setUp() throws Exception {

        account1 = new Account("account1", new BigDecimal(100));
        account2 = new Account("account2", new BigDecimal(200));

        ErisAccount erisAccount1 = new ErisAccount();
        erisAccount1.setAddress("address1");
        ErisAccount erisAccount2 = new ErisAccount();
        erisAccount2.setAddress("address1");

        account1.setErisAccount(erisAccount1);
        account2.setErisAccount(erisAccount2);



        when(principal.getName()).thenReturn("account1");

        TransactionMapper mapper = mock(TransactionMapper.class);

        coinService = new CoinService(accountsService, contractService, erisAccountRepository, transactionRepository, mapper);

        when(accountsService.getAccount("account1")).thenReturn(account1);
        when(accountsService.getAccount("account2")).thenReturn(account2);
        when(accountsService.getAccount(not(or(eq("account1"), eq("account2"))))).thenThrow(new AccountNotFoundException(""));

        when(contractService.getTokenContractForAccount(any())).thenReturn(contract);

        Response getResp = new Response("",
                Collections.singletonList(BigInteger.valueOf(100L)),
                null,
                null);

        Response sendResp = new Response("",
                Collections.singletonList(true),
                null,
                new TxParams("address", "txId"));

        when(contract.call(eq("balanceOf"), org.mockito.Matchers.anyVararg()))
                .thenReturn(getResp);

        when(contract.call(eq("transfer"), org.mockito.Matchers.anyVararg()))
                .thenReturn(sendResp);
    }

    @Test
    public void testMove() throws Exception {
        coinService.move(principal.getName(), "account2", new BigDecimal(50), "");

        verify(contract, times(1)).call(eq("balanceOf"), anyVararg());
        verify(contract, times(1)).call(eq("transfer"), anyVararg());
    }

    @Test(expected = NotEnoughAmountInAccountException.class)
    public void testMoveTooMuch() throws Exception {
        assertEquals(TransactionStatus.FAILED, coinService.move(principal.getName(), "account2", new BigDecimal(500), "").getStatus());

        assertEquals(new BigDecimal(100), account1.getAmount());
        assertEquals(new BigDecimal(200), account2.getAmount());

        verify(accountsService, times(0)).update(any(Account.class));
    }

    @Test(expected = AccountNotFoundException.class)
    public void testMoveToWrongAccount() throws Exception {
        assertEquals(TransactionStatus.FAILED, coinService.move(principal.getName(), "account3", new BigDecimal(50), "").getStatus());

        assertEquals(new BigDecimal(100), account1.getAmount());
        assertEquals(new BigDecimal(200), account2.getAmount());
    }

    @Test
    public void testMoveExceptionInTheMiddle() throws Exception {
        when(accountsService.getAccount("account2")).thenThrow(new AccountNotFoundException(""));

        try {
            assertEquals(TransactionStatus.FAILED, coinService.move(principal.getName(), "account2", new BigDecimal(50), "").getStatus());
        } catch (AccountNotFoundException ignored) {

        }

        assertEquals(new BigDecimal(100), account1.getAmount());
        assertEquals(new BigDecimal(200), account2.getAmount());
    }

}