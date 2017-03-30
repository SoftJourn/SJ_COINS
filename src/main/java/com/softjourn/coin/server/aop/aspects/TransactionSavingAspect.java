package com.softjourn.coin.server.aop.aspects;


import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import com.softjourn.coin.server.service.CoinService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Aspect
@Order(value = 100)
@Service
public class TransactionSavingAspect {

    private TransactionRepository transactionRepository;

    private AccountRepository accountRepository;

    private CoinService coinService;

    @Autowired
    public TransactionSavingAspect(TransactionRepository transactionRepository, AccountRepository accountRepository, CoinService coinService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.coinService = coinService;
    }

    @Around("@annotation(com.softjourn.coin.server.aop.annotations.SaveTransaction)")
    public Object saveTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        Transaction transaction = new Transaction();
        try {
            Object callingResult = joinPoint.proceed();
            if (callingResult instanceof Transaction) {
                transaction = (Transaction) callingResult;
            }
            fillTransaction(transaction, joinPoint);
            transaction.setStatus(TransactionStatus.SUCCESS);
            setRemainAmount(joinPoint, transaction);
            return callingResult instanceof Transaction ? transactionRepository.save(transaction) : callingResult;
        } catch (Throwable e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setError(e.getLocalizedMessage());
            fillTransaction(transaction, joinPoint);
            throw e;
        } finally {
            transactionRepository.save(transaction);
        }
    }

    private void fillTransaction(Transaction transaction, ProceedingJoinPoint joinPoint) {
        Account accountName = getArgOrAnnotationValue(joinPoint, "accountName", SaveTransaction::accountName, accountRepository::findOne);
        replaceIfNull(transaction::getAccount, transaction::setAccount, accountName);
        Account destinationName = getArgOrAnnotationValue(joinPoint, "destinationName", SaveTransaction::destinationName, accountRepository::findOne);
        replaceIfNull(transaction::getDestination, transaction::setDestination, destinationName);
        BigDecimal amount = getArg(joinPoint, "amount", BigDecimal.class);
        replaceIfNull(transaction::getAmount, transaction::setAmount, amount);
        String comment = getArgOrAnnotationValue(joinPoint, "comment", SaveTransaction::comment, Function.identity());
        replaceIfNull(transaction::getComment, transaction::setComment, comment);
        transaction.setCreated(Instant.now());
    }

    private <T> void replaceIfNull(Supplier<T> getter, Consumer<T> setter, T value) {
        if (getter.get() == null) {
            setter.accept(value);
        }
    }

    private void setRemainAmount(ProceedingJoinPoint joinPoint, Transaction transaction) {
        String accName = Optional.ofNullable(transaction.getAccount())
                .map(Account::getLdapId)
                .orElseGet(() -> getArg(joinPoint, "accountName", String.class));
        if (accName != null) {
            transaction.setRemain(coinService.getAmount(accName));
        }
    }

    private < R> R getArgOrAnnotationValue(ProceedingJoinPoint joinPoint, String argName,
                                             Function<SaveTransaction, String> transactionGetter, Function<String, R> converter) {
        SaveTransaction annotation = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(SaveTransaction.class);
        String argValue = getArg(joinPoint, argName, String.class);
        String annotationValue = transactionGetter.apply(annotation);
        return converter.apply(argValue == null ? annotationValue : argValue);
    }

    @SuppressWarnings("unchecked")
    private <T> T getArg(ProceedingJoinPoint joinPoint, String name, Class<? extends T> clazz) {
        MethodSignature signature = ((MethodSignature) joinPoint.getSignature());
        String[] names = signature.getParameterNames();
        Object[] arguments = joinPoint.getArgs();
        for (int i = 0; i < arguments.length; i++) {
            if (names[i].equalsIgnoreCase(name) && clazz.isInstance(arguments[i])) {
                return (T) arguments[i];
            }
        }
        return null;
    }
}
