package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
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
    AccountRepository accountRepository;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    AccountsService accountsService;

    CoinService coinService;

    @Before
    public void setUp() throws Exception {

        account = new Account("user", new BigDecimal(100));
        when(principal.getName()).thenReturn("user");

        when(accountRepository.findOne(anyString())).thenReturn(account);

        coinService = new CoinService(accountRepository, transactionRepository, accountsService);
    }

    @Test
    public void testFillAccount() throws Exception {
        coinService.fillAccount("user", new BigDecimal(100), "");

        verify(accountRepository, times(1)).findOne(anyString());
        verify(accountRepository, times(1)).save(account);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFillAccountNegative() throws Exception {
        assertEquals(new BigDecimal(200), coinService.fillAccount("user", new BigDecimal(-100), ""));

        verify(accountRepository, times(0)).findOne(anyString());
        verify(accountRepository, times(0)).save(account);
    }

    @Test
    public void testSpent() throws Exception {
        coinService.spent(principal.getName(), new BigDecimal(50), "");
        assertEquals(new BigDecimal(50), account.getAmount());

        verify(accountRepository, times(1)).findOne(anyString());
        verify(accountRepository, times(1)).save(account);
    }

    @Test(expected = NotEnoughAmountInAccountException.class)
    public void testSpentTooMach() throws Exception {
        assertTrue(coinService.spent(principal.getName(), new BigDecimal(150), "").getStatus().equals(TransactionStatus.FAILED));
        assertEquals(new BigDecimal(100), account.getAmount());

        verify(accountRepository, times(1)).findOne(anyString());
        verify(accountRepository, times(0)).save(account);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpentNegative() throws Exception {
        assertTrue(coinService.spent(principal.getName(), new BigDecimal(-150), "").getStatus().equals(TransactionStatus.FAILED));
        assertEquals(new BigDecimal(100), account.getAmount());

        verify(accountRepository, times(0)).findOne(anyString());
        verify(accountRepository, times(0)).save(account);
    }

}