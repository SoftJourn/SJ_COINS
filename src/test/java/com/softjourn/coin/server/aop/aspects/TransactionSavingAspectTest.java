package com.softjourn.coin.server.aop.aspects;

import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionType;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import com.softjourn.coin.server.service.CoinService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static com.softjourn.coin.server.entity.TransactionType.UNKNOWN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionSavingAspectTest {

    private TransactionSavingAspect transactionSavingAspect;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CoinService coinService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @Mock
    private Account account;
    @Mock
    private Account destination;

    @Mock
    private Account annotationalAccount;
    @Mock
    private Account annotationalDestination;

    private static final String ACCOUNT_NAME = "testAccount";
    private static final String DESTINATION_NAME = "testDestination";
    private static final String COMMENT = "comment";
    private static final BigDecimal AMOUNT = new BigDecimal(100);

    private static final String ANNOTATION_ACCOUNT_NAME = "annotationAestAccount";
    private static final String ANNOTATION_DESTINATION_NAME = "annotationTestDestination";
    private static final String ANNOTATION_COMMENT = "annotationComment";

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Throwable {
        transactionSavingAspect = new TransactionSavingAspect(transactionRepository, accountRepository, coinService);

        when(transactionRepository.save(any(Transaction.class))).then(invocation -> invocation.getArguments()[0]);
        when(accountRepository.findOne(eq(ACCOUNT_NAME))).thenReturn(account);
        when(accountRepository.findOne(eq(DESTINATION_NAME))).thenReturn(destination);

        when(accountRepository.findOne(eq(ANNOTATION_ACCOUNT_NAME))).thenReturn(annotationalAccount);
        when(accountRepository.findOne(eq(ANNOTATION_DESTINATION_NAME))).thenReturn(annotationalDestination);

        Method method = this.getClass().getMethod("testMethod", String.class, String.class, BigDecimal.class, String.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[]{"accountName", "destinationName", "amount", "comment", "type"});

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenReturn(new Transaction<>("testTxId"));
    }

    @Test
    public void saveTransaction_expectTransactionAsReturnValue() throws Throwable {
        Object returnValue = transactionSavingAspect.saveTransaction(joinPoint);

        assertEquals(Transaction.class, returnValue.getClass());
    }


    @Test
    public void saveTransaction_expectTxParamsGottenFromArguments() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{ACCOUNT_NAME, DESTINATION_NAME, AMOUNT, COMMENT, UNKNOWN});
        when(joinPoint.proceed()).thenReturn(new Transaction<>("testTxId"));

        Transaction returnValue = (Transaction) transactionSavingAspect.saveTransaction(joinPoint);

        assertEquals(account, returnValue.getAccount());
        assertEquals(destination, returnValue.getDestination());
        assertEquals(COMMENT, returnValue.getComment());
        assertEquals(AMOUNT, returnValue.getAmount());
        assertEquals(UNKNOWN, returnValue.getType());
        assertNull(returnValue.getError());
    }

    @Test
    public void saveTransaction_expectTxParamsGottenFromAnnotationIfNotPresentInArguments() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{null, null, AMOUNT, null, UNKNOWN});
        when(joinPoint.proceed()).thenReturn(new Transaction<>("testTxId"));

        Transaction returnValue = (Transaction) transactionSavingAspect.saveTransaction(joinPoint);

        assertEquals(annotationalAccount, returnValue.getAccount());
        assertEquals(annotationalDestination, returnValue.getDestination());
        assertEquals(ANNOTATION_COMMENT, returnValue.getComment());
        assertEquals(AMOUNT, returnValue.getAmount());
        assertEquals(UNKNOWN, returnValue.getType());
        assertNull(returnValue.getError());
    }

    @Test
    public void saveTransaction_expectTxParamsGottenFromAnnotationIfNotPresentInArguments_onException() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{null, null, AMOUNT, null, UNKNOWN});
        when(joinPoint.proceed()).thenReturn(new Transaction<>("testTxId"));

        when(joinPoint.proceed()).thenThrow(new RuntimeException("Exception"));
        try {
            transactionSavingAspect.saveTransaction(joinPoint);
        } catch (Exception e) {
            //do nothing
        }

        verify(transactionRepository).save(argThat(new ArgumentMatcher<Transaction>() {
            @Override
            public boolean matches(Object argument) {
                Transaction transaction = (Transaction) argument;
                assertEquals(annotationalAccount, transaction.getAccount());
                assertEquals(annotationalDestination, transaction.getDestination());
                assertEquals(ANNOTATION_COMMENT, transaction.getComment());
                assertEquals(AMOUNT, transaction.getAmount());
                assertEquals(UNKNOWN, transaction.getType());
                assertNotNull(transaction.getError());
                return true;
            }
        }));

    }

    @SaveTransaction(accountName = ANNOTATION_ACCOUNT_NAME, destinationName = ANNOTATION_DESTINATION_NAME, comment = ANNOTATION_COMMENT, type = UNKNOWN)
    public Transaction testMethod(String accountName, String destinationName, BigDecimal amount, String comment) {
        return null;
    }

}