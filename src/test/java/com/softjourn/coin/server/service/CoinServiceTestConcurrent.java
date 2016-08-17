package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoinServiceTestConcurrent {

    Account account;

    @Mock
    Principal principal;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    AccountsService accountsService;

    CoinService coinService;

    @Before
    public void setUp() throws Exception {

        account = new Account("user", new BigDecimal(200000));
        when(principal.getName()).thenReturn("user");

        when(accountsService.getAccount(anyString())).thenReturn(account);

        coinService = new CoinService(accountsService);
    }

    /*
     * Test adding and getting money in concurrent environment
     */
    @Test
    public void test() throws Exception {
        ExecutorService executorService = new ForkJoinPool(10);
        Collection<Callable<Object>> tasks = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            tasks.add(() -> coinService.fillAccount("user", new BigDecimal(100), ""));
            tasks.add(() -> coinService.spent(principal.getName(), new BigDecimal(200), ""));
        }

        executorService.invokeAll(tasks);
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        assertEquals(new BigDecimal(100000), account.getAmount());
    }
}