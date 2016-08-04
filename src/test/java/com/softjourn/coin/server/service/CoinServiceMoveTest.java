package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.security.Principal;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CoinServiceMoveTest {

    Account account1;
    Account account2;

    @Mock
    Principal principal;

    @Mock
    AccountRepository accountRepository;

    @Mock
    AccountsService accountsService;

    @Mock
    TransactionRepository transactionRepository;

    CoinService coinService;

    @Before
    public void setUp() throws Exception {

        account1 = new Account("account1", new BigDecimal(100));
        account2 = new Account("account2", new BigDecimal(200));

        when(principal.getName()).thenReturn("account1");

        when(accountRepository.findOne("account1")).thenReturn(account1);
        when(accountRepository.findOne("account2")).thenReturn(account2);

        coinService = new CoinService(accountRepository, transactionRepository, accountsService);
    }

    @Test
    public void testMove() throws Exception {
        coinService.move(principal.getName(), "account2", new BigDecimal(50), "");

        assertEquals(new BigDecimal(50), account1.getAmount());
        assertEquals(new BigDecimal(250), account2.getAmount());

        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test(expected = NotEnoughAmountInAccountException.class)
    public void testMoveTooMuch() throws Exception {
        assertEquals(TransactionStatus.FAILED, coinService.move(principal.getName(), "account2", new BigDecimal(500), "").getStatus());

        assertEquals(new BigDecimal(100), account1.getAmount());
        assertEquals(new BigDecimal(200), account2.getAmount());

        verify(accountRepository, times(0)).save(any(Account.class));
    }

    @Test(expected = AccountNotFoundException.class)
    public void testMoveToWrongAccount() throws Exception {
        assertEquals(TransactionStatus.FAILED, coinService.move(principal.getName(), "account3", new BigDecimal(50), "").getStatus());

        assertEquals(new BigDecimal(100), account1.getAmount());
        assertEquals(new BigDecimal(200), account2.getAmount());
    }

    @Test
    public void testMoveExceptionInTheMiddle() throws Exception {
        when(accountRepository.findOne("account2")).thenThrow(new EntityNotFoundException());

        try {
            assertEquals(TransactionStatus.FAILED, coinService.move(principal.getName(), "account2", new BigDecimal(50), "").getStatus());
        } catch (EntityNotFoundException ignored) {

        }

        assertEquals(new BigDecimal(100), account1.getAmount());
        assertEquals(new BigDecimal(200), account2.getAmount());
    }

}