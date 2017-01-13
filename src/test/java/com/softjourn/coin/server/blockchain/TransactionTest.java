package com.softjourn.coin.server.blockchain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * TransactionTest
 * Created by vromanchuk on 13.01.17.
 */
@RunWith(SpringRunner.class)
public class TransactionTest {

    private String transactionBinary = "0201011490CCB0132FA9287AB3C3283978C0E523FA1450A0000000000000000101070177F493FA8F938E09C077BAD480B77420DB6B4E82DA2BFF69C468CA3A8667DF6A6D49DE8BD2AD9FB19235A15D80238E47C08DCA693DBC5CA5DDC61195E8262B0A01CE92BABD1B4BEED36B5314DA468B2C16BE0E0380948C67ED2C352ADD8099E67301143E5C5EBAEAA66C24785D04F33F2B62667001474A00000000000F42400000000000000000014440C10F1900000000000000000000000090CCB0132FA9287AB3C3283978C0E523FA1450A0000000000000000000000000000000000000000000000000000000000000006E";

    @Test
    public void newTransaction_() throws Exception {
        assertNotNull(new Transaction(transactionBinary));
        System.out.println(new Transaction(transactionBinary));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void newTransaction_fakeString_StringIndexOutOfBoundsException() throws Exception {
        new Transaction("fake transaction");
    }

    @Test
    public void parseCallingData() throws Exception {
        Transaction transaction = new Transaction(transactionBinary);
        Transaction.parseCallingData(transaction.getCallingData());
//        String binaryData


    }
}
