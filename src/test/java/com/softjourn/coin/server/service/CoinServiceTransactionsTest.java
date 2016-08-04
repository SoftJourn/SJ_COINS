package com.softjourn.coin.server.service;


import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.security.Principal;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    AccountsService accountsService;

    CoinService coinService;

    @Before
    public void setUp() throws Exception {
        principal = Mockito.mock(Principal.class);

        when(principal.getName()).thenReturn("account1");

        accountsService = mock(AccountsService.class);

        coinService = new CoinService(accountRepository, transactionRepository, accountsService);
    }

    @Test
    public void testFillAccount() throws Exception {
        BigDecimal initialAmount = accountRepository.findOne("account1").getAmount();
        BigDecimal bonusAmount = new BigDecimal("10");

        coinService.fillAccount("account1", bonusAmount, "Monthly bonus.");

        assertEquals(initialAmount.add(bonusAmount), accountRepository.findOne("account1").getAmount());
    }

    @Test
    public void testSpent() throws Exception {
        BigDecimal initialAmount = accountRepository.findOne("account1").getAmount();
        BigDecimal spentAmount = new BigDecimal("10");

        coinService.spent("account1", spentAmount, "Buying Pepsi.");

        assertEquals(initialAmount.subtract(spentAmount), accountRepository.findOne("account1").getAmount());
    }

    @Test
    public void testMove() throws Exception {
        try {
            coinService.move(principal.getName(), "account2", new BigDecimal(50), "");
        } catch (PersistenceException ignored) {}

        assertEquals(new BigDecimal(50), accountRepository.findOne("account1").getAmount());
        assertEquals(new BigDecimal(250), accountRepository.findOne("account2").getAmount());
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