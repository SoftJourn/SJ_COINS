package com.softjourn.coin.server.aop.aspects;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Aspect
@Order(value=100)
@Service
public class TransactionSavingAspect {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Around("@annotation(com.softjourn.coin.server.aop.annotations.SaveTransaction)")
    public Transaction saveTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        Transaction transaction = prepareTransaction(joinPoint);
        try {
            joinPoint.proceed();
            transaction.setStatus(TransactionStatus.SUCCESS);
            return transaction;
        } catch (Throwable e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setError(e.getLocalizedMessage());
            throw e;
        } finally {
            transactionRepository.save(transaction);
        }
    }

    private Transaction prepareTransaction(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return fillTransaction(signature, joinPoint.getArgs());
    }

    private Transaction fillTransaction(MethodSignature signature, Object[] arguments) {
        Transaction transaction = new Transaction();
        transaction.setAccount(getAccount(signature, arguments, "accountName"));
        transaction.setDestination(getAccount(signature, arguments, "destinationName"));
        transaction.setAmount(getArg(signature, arguments, "amount", BigDecimal.class));
        BigDecimal remain;
        if(transaction.getAccount()==null)
            remain=transaction.getDestination().getAmount().add(transaction.getAmount());
        else
            remain= transaction.getAccount().getAmount().subtract(transaction.getAmount());
        transaction.setRemain(remain);

        transaction.setComment(getArg(signature, arguments, "comment", String.class));
        return transaction;
    }

    private Account getAccount(MethodSignature signature, Object[] arguments, String argName) {
        String accountName = getArg(signature, arguments, argName, String.class);
        return accountName == null ? null : accountRepository.findOne(accountName);
    }


    private <T> T getArg(MethodSignature signature, Object[] arguments, String name, Class<? extends T> clazz) {
        String[] names = signature.getParameterNames();
        for (int i = 0; i < arguments.length; i++) {
            if (names[i].equalsIgnoreCase(name)) {
                Object res = arguments[i];
                if (clazz.isInstance(res)) {
                    return (T) res;
                } else {
                    return null;
                }
            }
        }
        return null;
    }
}
