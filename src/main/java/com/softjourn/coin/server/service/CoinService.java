package com.softjourn.coin.server.service;


import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
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

    private AccountRepository accountRepository;

    private TransactionRepository transactionRepository;

    private AccountsService accountsService;

    private Map<String, String> monitors = new HashMap<>();

    @Autowired
    public CoinService(AccountRepository accountRepository,
                       TransactionRepository transactionRepository,
                       AccountsService accountsService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountsService = accountsService;
    }

    @SaveTransaction
    @Transactional
    public Transaction fillAccount(@NonNull String destinationName, @NonNull BigDecimal amount, String comment) {
        synchronized (getMonitor(destinationName)) {
            checkAmountIsPositive(amount);

            Account account = getAccount(destinationName);

            BigDecimal currentAmount = account.getAmount();
            account.setAmount(currentAmount.add(amount));

            accountRepository.save(account);
            return null;
        }

    }

    @SaveTransaction
    @Transactional
    public Transaction spent(@NonNull String accountName, @NonNull BigDecimal amount, String comment) {
        synchronized (getMonitor(accountName)) {
            checkAmountIsPositive(amount);

            Account account = getAccount(accountName);

            BigDecimal currentAmount = account.getAmount();

            if (currentAmount.compareTo(amount) < 0) {
                throw new NotEnoughAmountInAccountException();
            }

            account.setAmount(currentAmount.add(amount.negate()));
            accountRepository.save(account);

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

        Account donor = getAccount(accountName);
        Account acceptor = getAccount(destinationName);

        if (!isEnoughAmount(accountName, amount)) {
            throw new NotEnoughAmountInAccountException();
        }

        BigDecimal newDonorAmount = donor.getAmount().add(amount.negate());
        BigDecimal newAcceptorAmount = acceptor.getAmount().add(amount);

        donor.setAmount(newDonorAmount);
        acceptor.setAmount(newAcceptorAmount);

        accountRepository.save(donor);
        accountRepository.save(acceptor);

        return null;
    }

    public BigDecimal getAmount(Principal principal) {
        return getAccount(principal.getName()).getAmount();
    }

    private boolean isEnoughAmount(@NonNull String from, BigDecimal amount) {
        return getAccount(from).getAmount().compareTo(amount) > 0;
    }

    private void checkAmountIsPositive(@NonNull BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    private Account getAccount(String accountId) {
        Account account = accountRepository.findOne(accountId);
        if (account == null) {
            if (accountsService.isAccountExistInLdapBase(accountId)) {
                Account newAccount = new Account(accountId, new BigDecimal(0));
                accountRepository.save(newAccount);
                return newAccount;
            } else {
                throw new AccountNotFoundException(accountId);
            }
        }
        return account;
    }


    private synchronized String getMonitor(String accountId) {
        if (monitors.containsKey(accountId)) {
            return monitors.get(accountId);
        }

        monitors.put(accountId, accountId);

        return accountId;
    }


}
