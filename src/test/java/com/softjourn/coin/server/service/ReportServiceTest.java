package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.ReportDefiner;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.entity.TransactionType;
import lombok.Data;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {

    ReportService reportService;

    List<ReportDefiner> definers;

    List<ReportDefiner> primitives;

    List<Transaction> entities;

    @Before
    public void setUp() {
        definers = new ArrayList<>();
        ReportDefiner account = new ReportDefiner("account", null);
        account.getDefiners().add(new ReportDefiner("fullName", "Account"));

        ReportDefiner destination = new ReportDefiner("destination", null);
        destination.getDefiners().add(new ReportDefiner("fullName", "Destination"));

        definers.add(account);
        definers.add(new ReportDefiner("amount", "Amount"));
        definers.add(new ReportDefiner("comment", "Comment"));
        definers.add(new ReportDefiner("created", "Created"));
        definers.add(destination);
        definers.add(new ReportDefiner("error", "Error"));
        definers.add(new ReportDefiner("status", "Status"));
        definers.add(new ReportDefiner("type", "Type"));

        primitives = new ArrayList<>();

        primitives.add(new ReportDefiner("byteP", "Byte"));
        primitives.add(new ReportDefiner("charP", "Character"));
        primitives.add(new ReportDefiner("shortP", "Short"));
        primitives.add(new ReportDefiner("intP", "Integer"));
        primitives.add(new ReportDefiner("longP", "Long"));
        primitives.add(new ReportDefiner("booleanP", "Boolean"));

        entities = new ArrayList<>();

        Transaction transaction = new Transaction();

        Account account1 = new Account();
        account1.setFullName("full name");

        transaction.setAccount(account1);
        transaction.setDestination(account1);
        transaction.setComment("comment");
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(new BigDecimal(100));
        transaction.setError("error");
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreated(LocalDateTime.of(2017, 4, 28, 17, 30, 30).toInstant(ZoneOffset.UTC));

        entities.add(transaction);

        reportService = new ReportServiceImpl();
    }

    @Test
    public void checkHeadersTest() throws NoSuchFieldException, IllegalAccessException {
        int headersCount = 8;

        Workbook workbook = reportService.toReport("some", null, definers);

        assertEquals("some", workbook.getSheetName(0));
        assertEquals(headersCount, workbook.getSheetAt(0).getRow(0).getLastCellNum());

        assertEquals("Account", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        assertEquals("Amount", workbook.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
        assertEquals("Comment", workbook.getSheetAt(0).getRow(0).getCell(2).getStringCellValue());
        assertEquals("Created", workbook.getSheetAt(0).getRow(0).getCell(3).getStringCellValue());
        assertEquals("Destination", workbook.getSheetAt(0).getRow(0).getCell(4).getStringCellValue());
        assertEquals("Error", workbook.getSheetAt(0).getRow(0).getCell(5).getStringCellValue());
        assertEquals("Status", workbook.getSheetAt(0).getRow(0).getCell(6).getStringCellValue());
        assertEquals("Type", workbook.getSheetAt(0).getRow(0).getCell(7).getStringCellValue());
    }

    @Test
    public void checkContentTest() throws NoSuchFieldException, IllegalAccessException {
        int headersCount = 8;

        Workbook workbook = reportService.toReport("some", entities, definers);

        assertEquals("some", workbook.getSheetName(0));
        assertEquals(headersCount, workbook.getSheetAt(0).getRow(0).getLastCellNum());

        assertEquals("full name", workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
        assertEquals(100, workbook.getSheetAt(0).getRow(1).getCell(1).getNumericCellValue(), 0);
        assertEquals("comment", workbook.getSheetAt(0).getRow(1).getCell(2).getStringCellValue());
        assertEquals("Fri, 28 Apr 2017 17:30:30 GMT", workbook.getSheetAt(0).getRow(1).getCell(3).getStringCellValue());
        assertEquals("full name", workbook.getSheetAt(0).getRow(1).getCell(4).getStringCellValue());
        assertEquals("error", workbook.getSheetAt(0).getRow(1).getCell(5).getStringCellValue());
        assertEquals("SUCCESS", workbook.getSheetAt(0).getRow(1).getCell(6).getStringCellValue());
        assertEquals("TRANSFER", workbook.getSheetAt(0).getRow(1).getCell(7).getStringCellValue());
    }

    @Test
    public void checkPrimitivesTest() throws NoSuchFieldException, IllegalAccessException {
        int headersCount = primitives.size();

        Workbook workbook = reportService.toReport("some", new ArrayList<Primitives>() {{
            add(new Primitives());
        }}, primitives);

        assertEquals("some", workbook.getSheetName(0));
        assertEquals(headersCount, workbook.getSheetAt(0).getRow(0).getLastCellNum());

        assertEquals(0, workbook.getSheetAt(0).getRow(1).getCell(0).getNumericCellValue(), 0);
        assertEquals("a", workbook.getSheetAt(0).getRow(1).getCell(1).getStringCellValue());
        assertEquals(0, workbook.getSheetAt(0).getRow(1).getCell(2).getNumericCellValue(), 0);
        assertEquals(0, workbook.getSheetAt(0).getRow(1).getCell(3).getNumericCellValue(), 0);
        assertEquals(0, workbook.getSheetAt(0).getRow(1).getCell(4).getNumericCellValue(), 0);
        assertEquals(true, workbook.getSheetAt(0).getRow(1).getCell(5).getBooleanCellValue());
    }

    @Data
    class Primitives {

        private byte byteP = 0;
        private char charP = 'a';
        private short shortP = 0;
        private int intP = 0;
        private long longP = 0;
        private boolean booleanP = true;

    }

}
