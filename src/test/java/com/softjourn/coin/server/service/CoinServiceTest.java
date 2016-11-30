package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.*;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
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

    @InjectMocks
    CoinService coinService;

    @Captor
    ArgumentCaptor<Object> captor;

    @Captor
    ArgumentCaptor<ErisAccount> erisAccountCaptor;

    @Before
    public void setUp() throws Exception {

        account = new Account("user", new BigDecimal(100));

        ErisAccount erisAccount1 = new ErisAccount();
        erisAccount1.setAddress("address1");
        erisAccount1.setType(ErisAccountType.ROOT);

        account.setErisAccount(erisAccount1);

        Account account2 = new Account("VM1", new BigDecimal(100));
        account2.setAccountType(AccountType.MERCHANT);
        account2.setErisAccount(erisAccount1);

        Account account3 = new Account("VM2", new BigDecimal(100));
        account3.setAccountType(AccountType.MERCHANT);
        account3.setErisAccount(erisAccount1);

        List<Account> accounts = Arrays.asList(account, account2, account3);

        ReflectionTestUtils.setField(coinService, "treasuryErisAccount", erisAccount1, ErisAccount.class);

        when(principal.getName()).thenReturn("user");

        when(accountsService.getAccount(anyString())).thenReturn(account);

        when(accountsService.getAll()).thenReturn(accounts);
        when(accountsService.getAll(AccountType.MERCHANT)).thenReturn(Arrays.asList(account2, account3));
        when(accountsService.getAll(AccountType.REGULAR)).thenReturn(Collections.emptyList());

        when(contractService.getForAccount(any())).thenReturn(contract);
        when(contractService.getForAccount(null)).thenReturn(contract);

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

        when(contract.call(eq("send"), anyVararg()))
                .thenReturn(sendResp);

        when(contract.call(eq("mint"), anyVararg()))
                .thenReturn(sendResp);
    }

    @Test
    public void testFillAccount() throws Exception {
        coinService.fillAccount("user1", new BigDecimal(100), "");

        verify(accountsService, atMost(2)).getAccount("user1");
        verify(contract, times(1)).call(eq("send"), anyVararg());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFillAccountNegative() throws Exception {
        coinService.fillAccount("user1", new BigDecimal(-100), "");

        verify(accountsService, times(0)).getAccount(anyString());
        verify(accountsService, times(0)).update(account);
    }

    @Test
    public void testSpent() throws Exception {
        coinService.buy(principal.getName(),"VM1", new BigDecimal(50), "");

        verify(accountsService, atMost(5)).getAccount(anyString());
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

    @Test
    public void testGetTreasuryAmount() throws Exception {
        BigDecimal treasuryAmount = coinService.getTreasuryAmount();
        BigDecimal expectedAmount = new BigDecimal(100);

        assertEquals(expectedAmount, treasuryAmount);

        verify(contract).call(eq("queryBalance"), anyVararg());
    }

    @Test
    public void testGetMerchantsAmount() throws Exception {
        BigDecimal merchantsAmount = coinService.getAmountByAccountType(AccountType.MERCHANT);
        BigDecimal accountAmount = coinService.getAmountByAccountType(AccountType.REGULAR);

        assertEquals(new BigDecimal(200), merchantsAmount);
        assertEquals(BigDecimal.ZERO, accountAmount);

        verify(accountsService).getAll(AccountType.MERCHANT);
        verify(accountsService).getAll(AccountType.REGULAR);
    }

    @Test
    public void moveToTreasury() throws Exception {
        BigDecimal amount = new BigDecimal(70);
        coinService.moveToTreasury("account", amount, "Test msg");

        verify(contract).call(eq("send"), captor.capture());
        verify(contractService, times(2)).getForAccount(erisAccountCaptor.capture());

        assertEquals(captor.getAllValues().get(1), amount.toBigInteger());
        assertEquals(erisAccountCaptor.getValue(), account.getErisAccount());
    }
}