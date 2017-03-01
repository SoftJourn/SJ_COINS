package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.softjourn.coin.server.service.GenericFilter.Condition.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase
public class TransactionsRepositoryFilterTest {

    @Autowired
    private TransactionRepository repository;

    @Mock
    private PageRequest defaultPageable;

    @Before
    public void setUp() throws Exception {
        when(defaultPageable.getPageNumber()).thenReturn(0);
        when(defaultPageable.getPageSize()).thenReturn(Integer.MAX_VALUE);
    }

    @Test
    public void filteringTest_timeCondition_after() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        List<GenericFilter.Condition> conditions = new ArrayList<>();

        GenericFilter.Condition after_16_11_2016 = new GenericFilter.Condition();
        after_16_11_2016.setComparison(GenericFilter.Comparison.gt);
        after_16_11_2016.setField("created");
        Instant thresholdTime = LocalDate.of(2016, 11, 16).atStartOfDay().toInstant(ZoneOffset.UTC);
        after_16_11_2016.setValue(thresholdTime);

        conditions.add(after_16_11_2016);

        filter.setConditions(conditions);
        filter.setInnerPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getInnerPageable()).getContent();
        assertEquals(5, result.size());
        assertTrue(result.stream().allMatch(transaction -> transaction.getCreated().isAfter(thresholdTime)));
    }

    @Test
    public void filteringTest_timeCondition_before() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        List<GenericFilter.Condition> conditions = new ArrayList<>();

        GenericFilter.Condition before_16_11_2016 = new GenericFilter.Condition();
        before_16_11_2016.setComparison(GenericFilter.Comparison.lt);
        before_16_11_2016.setField("created");
        Instant thresholdTime = LocalDate.of(2016, 11, 16).atStartOfDay().toInstant(ZoneOffset.UTC);
        before_16_11_2016.setValue(thresholdTime);

        conditions.add(before_16_11_2016);

        filter.setConditions(conditions);
        filter.setInnerPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getInnerPageable()).getContent();
        assertEquals(5, result.size());
        assertTrue(result.stream().map(Transaction::getCreated).allMatch(time -> time.isBefore(thresholdTime)));
    }

    @Test
    public void filteringTest_timeCondition_between() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        List<GenericFilter.Condition> conditions = new ArrayList<>();

        GenericFilter.Condition after_16_11_2016 = new GenericFilter.Condition();
        after_16_11_2016.setComparison(GenericFilter.Comparison.gt);
        after_16_11_2016.setField("created");
        Instant lowerThresholdTime = ZonedDateTime.parse("2016-11-16T09:00:00+04:00").toInstant();
        after_16_11_2016.setValue("2016-11-16T05:00:00Z");

        GenericFilter.Condition before_18_11_2016 = new GenericFilter.Condition();
        before_18_11_2016.setComparison(GenericFilter.Comparison.lt);
        before_18_11_2016.setField("created");
        Instant upperThresholdTime = LocalDate.of(2016, 11, 18).atStartOfDay().toInstant(ZoneOffset.UTC);
        before_18_11_2016.setValue(upperThresholdTime);

        conditions.add(before_18_11_2016);
        conditions.add(after_16_11_2016);

        filter.setConditions(conditions);
        filter.setInnerPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getInnerPageable()).getContent();
        assertEquals(2, result.size());
        assertTrue(result.stream()
                .map(Transaction::getCreated)
                .allMatch(time -> time.isBefore(upperThresholdTime) && time.isAfter(lowerThresholdTime)));
    }

    @Test
    public void filteringTest_account_EqCondition() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        List<GenericFilter.Condition> conditions = new ArrayList<>();

        GenericFilter.Condition eqAccountCondition = new GenericFilter.Condition();
        eqAccountCondition.setComparison(GenericFilter.Comparison.eq);
        eqAccountCondition.setField("account");
        eqAccountCondition.setValue("vdanyliuk");

        conditions.add(eqAccountCondition);

        filter.setConditions(conditions);
        filter.setInnerPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getInnerPageable()).getContent();
        assertEquals(3, result.size());

        assertTrue(result.stream()
                .map(Transaction::getAccount)
                .map(Account::getLdapId)
                .allMatch(id -> id.equals("vdanyliuk")));
    }

    @Test
    public void filteringTest_account_InCondition() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        List<GenericFilter.Condition> conditions = new ArrayList<>();

        GenericFilter.Condition eqAccountCondition = new GenericFilter.Condition();
        eqAccountCondition.setComparison(GenericFilter.Comparison.in);
        eqAccountCondition.setField("account");
        eqAccountCondition.setValue(Arrays.asList("vdanyliuk", "ovovchuk"));

        conditions.add(eqAccountCondition);

        filter.setConditions(conditions);
        filter.setInnerPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getInnerPageable()).getContent();
        assertEquals(8, result.size());

        assertTrue(result.stream()
                .map(Transaction::getAccount)
                .map(Account::getLdapId)
                .allMatch(id -> id.equals("vdanyliuk") || id.equals("ovovchuk")));
    }

    @Test
    public void filteringTest_bySubField_inCondition() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        List<GenericFilter.Condition> conditions = new ArrayList<>();

        GenericFilter.Condition eqAccountCondition = new GenericFilter.Condition();
        eqAccountCondition.setComparison(GenericFilter.Comparison.in);
        eqAccountCondition.setField("transactionStoring.blockNumber");
        eqAccountCondition.setValue(Arrays.asList(3788719, 3699719));

        conditions.add(eqAccountCondition);

        filter.setConditions(conditions);
        filter.setInnerPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getInnerPageable()).getContent();
        assertEquals(2, result.size());

        assertTrue(result.stream()
                .map(Transaction::getTransactionStoring)
                .map(TransactionStoring::getBlockNumber)
                .allMatch(id -> id.equals(3788719L) || id.equals(3699719L)));
    }

    @Test
    public void filteringTest_castableType() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        List<GenericFilter.Condition> conditions = new ArrayList<>();

        GenericFilter.Condition eqAccountCondition = new GenericFilter.Condition();
        eqAccountCondition.setComparison(GenericFilter.Comparison.eq);
        eqAccountCondition.setField("amount");
        eqAccountCondition.setValue(1000000);

        conditions.add(eqAccountCondition);

        filter.setConditions(conditions);
        filter.setInnerPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getInnerPageable()).getContent();
        assertEquals(2, result.size());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filteringTest_nonCastableValue() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        List<GenericFilter.Condition> conditions = new ArrayList<>();

        GenericFilter.Condition eqAccountCondition = new GenericFilter.Condition();
        eqAccountCondition.setComparison(GenericFilter.Comparison.eq);
        eqAccountCondition.setField("amount");
        eqAccountCondition.setValue("value");

        conditions.add(eqAccountCondition);

        filter.setConditions(conditions);
        filter.setInnerPageable(defaultPageable);

        repository.findAll(filter, filter.getInnerPageable()).getContent();
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filteringTest_nonExistingField() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        List<GenericFilter.Condition> conditions = new ArrayList<>();

        GenericFilter.Condition eqAccountCondition = new GenericFilter.Condition();
        eqAccountCondition.setComparison(GenericFilter.Comparison.eq);
        eqAccountCondition.setField("nonExistingField");
        eqAccountCondition.setValue("value");

        conditions.add(eqAccountCondition);

        filter.setConditions(conditions);
        filter.setInnerPageable(defaultPageable);

        repository.findAll(filter, filter.getInnerPageable()).getContent();
    }

    @Test
    public void filteringTest_emptyFilter() {
        GenericFilter<Transaction> filter = new GenericFilter<>();

        List<Transaction> result = repository.findAll(filter, filter.getInnerPageable()).getContent();
        assertEquals(10, result.size());
    }

    @Test
    public void filteringTest_emptyFilter_limitPageSize() {
        GenericFilter<Transaction> filter = new GenericFilter<>();
        filter.setInnerPageable(new PageRequest(0, 5));

        List<Transaction> result = repository.findAll(filter, filter.getInnerPageable()).getContent();
        assertEquals(5, result.size());
    }

    @Test
    public void getForUserTest() throws Exception {
        GenericFilter<Transaction> fromFilter = GenericFilter.or(eq("account", "omartynets"), eq("destination", "omartynets"));

        List<Transaction> result = repository.findAll(fromFilter, fromFilter.getInnerPageable()).getContent();
        assertEquals(4, result.size());
    }
}