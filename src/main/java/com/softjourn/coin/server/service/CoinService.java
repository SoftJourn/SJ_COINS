package com.softjourn.coin.server.service;


import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Service
public class CoinService {


    private AccountsService accountsService;

    private Map<String, String> monitors = new HashMap<>();

    @Autowired
    public CoinService(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @SaveTransaction
    @Transactional
    public Transaction fillAccount(@NonNull String destinationName, @NonNull BigDecimal amount, String comment) {
        synchronized (getMonitor(destinationName)) {
            checkAmountIsPositive(amount);

            Account account = accountsService.getAccount(destinationName);

            BigDecimal currentAmount = account.getAmount();
            account.setAmount(currentAmount.add(amount));

            accountsService.update(account);
            return null;
        }

    }

    @SaveTransaction
    @Transactional
    public Transaction spent(@NonNull String accountName, @NonNull BigDecimal amount, String comment) {
        synchronized (getMonitor(accountName)) {
            checkAmountIsPositive(amount);

            Account account = accountsService.getAccount(accountName);

            BigDecimal currentAmount = account.getAmount();

            if (currentAmount.compareTo(amount) < 0) {
                throw new NotEnoughAmountInAccountException();
            }

            account.setAmount(currentAmount.add(amount.negate()));
            accountsService.update(account);

            return null;
        }
    }

    @SaveTransaction
    @Transactional
    public synchronized Transaction move(@NonNull String accountName,
                                         @NonNull String destinationName,
                                         @NonNull BigDecimal amount,
                                         String comment) {
        checkAmountIsPositive(amount);

        Account donor = accountsService.getAccount(accountName);
        Account acceptor = accountsService.getAccount(destinationName);

        if (!isEnoughAmount(accountName, amount)) {
            throw new NotEnoughAmountInAccountException();
        }

        BigDecimal newDonorAmount = donor.getAmount().add(amount.negate());
        BigDecimal newAcceptorAmount = acceptor.getAmount().add(amount);

        donor.setAmount(newDonorAmount);
        acceptor.setAmount(newAcceptorAmount);

        accountsService.update(donor);
        accountsService.update(acceptor);

        return null;
    }

    public BigDecimal getAmount(Principal principal) {
        return accountsService.getAccount(principal.getName()).getAmount();
    }

    private boolean isEnoughAmount(@NonNull String from, BigDecimal amount) {
        return accountsService.getAccount(from).getAmount().compareTo(amount) > 0;
    }

    private void checkAmountIsPositive(@NonNull BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    private synchronized String getMonitor(String accountId) {
        if (monitors.containsKey(accountId)) {
            return monitors.get(accountId);
        }

        monitors.put(accountId, accountId);

        return accountId;
    }


}
