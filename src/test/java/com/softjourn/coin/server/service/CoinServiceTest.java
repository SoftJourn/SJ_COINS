package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.ErisAccountType;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.response.Response;
import com.softjourn.eris.contract.response.ReturnValue;
import com.softjourn.eris.contract.response.TxParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Principal;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CoinServiceTest {

    Account account;

    @Mock
    Principal principal;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    Contract contract;

    @Mock
    ErisContractService contractService;

    @Mock
    ErisAccountRepository erisAccountRepository;

    @Mock
    AccountsService accountsService;

    CoinService coinService;

    @Before
    public void setUp() throws Exception {

        account = new Account("user", new BigDecimal(100));

        ErisAccount erisAccount1 = new ErisAccount();
        erisAccount1.setAddress("address1");
        erisAccount1.setType(ErisAccountType.ROOT);

        account.setErisAccount(erisAccount1);


        when(principal.getName()).thenReturn("user");

        when(accountsService.getAccount(anyString())).thenReturn(account);

        coinService = new CoinService(accountsService, contractService, erisAccountRepository);

        when(contractService.getForAccount(any())).thenReturn(contract);

        Response<Object> getResp = new Response<>("",
                new ReturnValue<>(Object.class, BigInteger.valueOf(100)),
                null,
                null);

        Response<Object> sendResp = new Response<>("",
                new ReturnValue<>(Object.class, true),
                null,
                new TxParams("address", "txId"));

        when(contract.call(eq("queryBalance"), anyVararg()))
                .thenReturn(getResp);

        when(contract.call(eq("send"), org.mockito.Matchers.anyVararg()))
                .thenReturn(sendResp);

        when(contract.call(eq("mint"), org.mockito.Matchers.anyVararg()))
                .thenReturn(sendResp);
    }

    @Test
    public void testFillAccount() throws Exception {
        coinService.fillAccount("user", "user1", new BigDecimal(100), "");

        verify(accountsService, times(1)).getAccount("user");
        verify(accountsService, times(1)).getAccount("user1");
        verify(contract, times(1)).call(eq("mint"), anyVararg());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFillAccountNegative() throws Exception {
        assertEquals(new BigDecimal(200), coinService.fillAccount("user", "user1", new BigDecimal(-100), ""));

        verify(accountsService, times(0)).getAccount(anyString());
        verify(accountsService, times(0)).update(account);
    }

    @Test
    public void testSpent() throws Exception {
        coinService.buy(principal.getName(),"VM1", new BigDecimal(50), "");

        verify(accountsService, times(3)).getAccount(anyString());
        verify(contract, times(1)).call(eq("send"), anyVararg());
    }

    @Test(expected = NotEnoughAmountInAccountException.class)
    public void testSpentTooMach() throws Exception {
        assertTrue(coinService.buy(principal.getName(), "address", new BigDecimal(150), "").getStatus().equals(TransactionStatus.FAILED));
        assertEquals(new BigDecimal(100), account.getAmount());

        verify(accountsService, times(1)).getAccount(anyString());
        verify(accountsService, times(0)).update(account);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpentNegative() throws Exception {
        assertTrue(coinService.buy(principal.getName(),"address",  new BigDecimal(-150), "").getStatus().equals(TransactionStatus.FAILED));
        assertEquals(new BigDecimal(100), account.getAmount());

        verify(accountsService, times(0)).getAccount(anyString());
        verify(accountsService, times(0)).update(account);
    }

}