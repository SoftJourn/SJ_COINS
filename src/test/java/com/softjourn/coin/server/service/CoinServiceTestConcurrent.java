package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.ErisAccountType;
import com.softjourn.coin.server.eris.contract.Contract;
import com.softjourn.coin.server.eris.contract.response.Response;
import com.softjourn.coin.server.eris.contract.response.ReturnValue;
import com.softjourn.coin.server.eris.contract.response.TxParams;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoinServiceTestConcurrent {

    Account account;

    @Mock
    Principal principal;

    @Mock
    ErisContractService contractService;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    AccountsService accountsService;

    @Mock
    Contract contract;

    CoinService coinService;

    private AtomicInteger amount = new AtomicInteger(200000);

    @Before
    public void setUp() throws Exception {

        account = new Account("user", new BigDecimal(200000));

        ErisAccount erisAccount1 = new ErisAccount();
        erisAccount1.setAddress("address1");
        erisAccount1.setType(ErisAccountType.ROOT);

        account.setErisAccount(erisAccount1);

        when(principal.getName()).thenReturn("user");

        when(accountsService.getAccount(anyString())).thenReturn(account);

        coinService = new CoinService(accountsService, contractService);

        when(contractService.getForAccount(any())).thenReturn(contract);

        Response<Object> getResp = new Response<>("",
                new ReturnValue<>(Object.class, new BigDecimal(100000)),
                null,
                null);

        Response<Object> sendResp = new Response<>("",
                null,
                null,
                new TxParams("address", "txId"));

        when(contract.call(eq("queryBalance"), anyVararg()))
                .thenReturn(getResp);

        when(contract.call(eq("send"), org.mockito.Matchers.anyVararg()))
                .then((InvocationOnMock invocation) -> {
                        amount.addAndGet(-200);
                        return sendResp;
                    }
                );

        when(contract.call(eq("mint"), org.mockito.Matchers.anyVararg()))
                .then(inv -> {
                    amount.addAndGet(100);
                    return sendResp;
                });
    }

    /*
     * Test adding and getting money in concurrent environment
     */
    @Test
    public void test() throws Exception {
        ExecutorService executorService = new ForkJoinPool(10);
        Collection<Callable<Object>> tasks = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            tasks.add(() -> coinService.fillAccount("user1", "user", new BigDecimal(100), ""));
            tasks.add(() -> coinService.spent(principal.getName(), "address", new BigDecimal(200), ""));
        }

        executorService.invokeAll(tasks);
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        assertEquals(new AtomicInteger(100000).get(), amount.get());
    }
}