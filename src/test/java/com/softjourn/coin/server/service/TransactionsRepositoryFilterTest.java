package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@DataJpaTest
@RunWith(SpringRunner.class)
@Sql({"classpath:transactions-data.sql"})
public class TransactionsRepositoryFilterTest {

    @Autowired
    private TransactionRepository repository;

    @Mock
    private Pageable defaultPageable;

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
        filter.setPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getPageable()).getContent();
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
        filter.setPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getPageable()).getContent();
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
        Instant lowerThresholdTime = LocalDate.of(2016, 11, 16).atStartOfDay().toInstant(ZoneOffset.UTC);
        after_16_11_2016.setValue(lowerThresholdTime);

        GenericFilter.Condition before_18_11_2016 = new GenericFilter.Condition();
        before_18_11_2016.setComparison(GenericFilter.Comparison.lt);
        before_18_11_2016.setField("created");
        Instant upperThresholdTime = LocalDate.of(2016, 11, 18).atStartOfDay().toInstant(ZoneOffset.UTC);
        before_18_11_2016.setValue(upperThresholdTime);

        conditions.add(before_18_11_2016);
        conditions.add(after_16_11_2016);

        filter.setConditions(conditions);
        filter.setPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getPageable()).getContent();
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
        filter.setPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getPageable()).getContent();
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
        filter.setPageable(defaultPageable);


        List<Transaction> result = repository.findAll(filter, filter.getPageable()).getContent();
        assertEquals(8, result.size());

        assertTrue(result.stream()
                .map(Transaction::getAccount)
                .map(Account::getLdapId)
                .allMatch(id -> id.equals("vdanyliuk") || id.equals("ovovchuk")));
    }
}