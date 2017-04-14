package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.CheckDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.exceptions.CouldNotReadFileException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInTreasuryException;
import com.softjourn.coin.server.exceptions.WrongMimeTypeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FillAccountsServiceTest {

    @Mock(name = "transactionResultMap")
    private Map<String, List<Future<Transaction>>> map;

    @Mock
    private CoinService coinService;

    @Mock
    private AccountsService accountsService;

    FillAccountsService fillAccountsService;

    MultipartFile wrongMimeType;

    StringBuilder wrongCSVStructure;
    StringBuilder negativeCSVData;
    StringBuilder rightCSVData;

    MultipartFile wrongContentStructure;

    MultipartFile negativeContentData;

    MultipartFile rightContentData;

    @Before
    public void setUp() {
        wrongMimeType = prepareMultipartFile("text/plain", null);
        wrongCSVStructure = new StringBuilder();
        wrongCSVStructure.append("columnheader1,columnheader2\r\n");
        wrongCSVStructure.append("cell1,cell2\r\n");

        negativeCSVData = new StringBuilder();
        negativeCSVData.append("Account,Full Name, Amount\r\n");
        negativeCSVData.append("me,me,-100\r\n");

        rightCSVData = new StringBuilder();
        rightCSVData.append("Account,Full Name, Amount\r\n");
        rightCSVData.append("me,me,100\r\n");

        wrongContentStructure = prepareMultipartFile("text/csv", wrongCSVStructure.toString().getBytes());
        negativeContentData = prepareMultipartFile("text/csv", negativeCSVData.toString().getBytes());
        rightContentData = prepareMultipartFile("text/csv", rightCSVData.toString().getBytes());

        when(coinService.getTreasuryAmount()).thenReturn(BigDecimal.valueOf(0));
        when(map.get(anyString())).thenReturn(new ArrayList<Future<Transaction>>() {{
            add(prepareFutureTransaction(new Transaction("12345")));
            add(prepareFutureTransaction(new Transaction("12345")));
        }});

        fillAccountsService = new FillAccountsService(map, coinService, accountsService);
    }

    @Test(expected = WrongMimeTypeException.class)
    public void fillAccountsTestWrongMimeType() throws IOException {
        fillAccountsService.fillAccounts(wrongMimeType);
    }

    @Test(expected = CouldNotReadFileException.class)
    public void fillAccountsTestWrongContentStructure() throws IOException {
        fillAccountsService.fillAccounts(wrongContentStructure);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fillAccountsTestNegativeValues() throws IOException {
        fillAccountsService.fillAccounts(negativeContentData
        );
    }

    @Test(expected = NotEnoughAmountInTreasuryException.class)
    public void fillAccountsTestNotEnoughCoins() throws IOException {
        fillAccountsService.fillAccounts(rightContentData);
    }

    @Test
    public void checkProgressTest() throws IOException {
        CheckDTO result = fillAccountsService.checkProcessing("something");
        assertEquals(Long.valueOf(2), result.getTotal());
        assertEquals(Long.valueOf(2), result.getIsDone());
    }


    private MultipartFile prepareMultipartFile(String mimeType, byte[] bytes) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getOriginalFilename() {
                return null;
            }

            @Override
            public String getContentType() {
                return mimeType;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return bytes;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {

            }
        };
    }

    private Future<Transaction> prepareFutureTransaction(Transaction transaction) {
        return new Future<Transaction>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Transaction get() throws InterruptedException, ExecutionException {
                return transaction;
            }

            @Override
            public Transaction get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
    }
}
