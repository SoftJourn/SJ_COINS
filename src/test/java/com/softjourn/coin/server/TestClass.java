package com.softjourn.coin.server;

import com.softjourn.coin.server.repository.ErisTransactionRepository;
import com.softjourn.eris.transaction.type.ErisTransaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by vromanchuk on 18.01.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestClass {

    @Autowired
    ErisTransactionRepository transactionRepository;

    @Test
    public void name() throws Exception {
        ErisTransaction transaction = new ErisTransaction();
        transactionRepository.save(transaction);
    }
}
